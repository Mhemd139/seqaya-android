# Phase 3 — Auth + Home

**Status:** DRAFT — awaiting owner approval
**Author:** Claude Opus 4.7 (with Muhammed)
**Date:** 2026-04-19
**Depends on:** Phase 2 scaffold (merged in PR #1)
**Blocks:** Phase 4 (device detail), Phase 5 (NFC provisioning)

## Goal

After Phase 3, a user can:

1. Install the app, see the splash + sign-in screen from the design.
2. Sign in with Google (via Supabase Auth).
3. Land on Home: either the empty state (B·01) if they have no devices, or the populated list (B·02) showing their plants with live moisture readings.
4. Close the app, reopen it, skip sign-in (session persists).
5. Lose network: app still shows last-known state with an "offline, last updated X ago" badge.
6. Regain network: moisture updates stream in via Supabase Realtime.

## Non-goals (explicitly out of scope for this phase)

- Device detail screen (Phase 4)
- Add-device NFC provisioning (Phase 5)
- Scan camera (Phase 7)
- Library (Phase 7)
- Settings/profile/delete-account (Phase 8)
- Locate/Hold/Resume contextual sheets (Phase 5)
- Any change to firmware or the Backend repo

---

## Current state of the Supabase project (ground truth, 2026-04-19)

Project: `jybsouuydgstafqsxfbx` (name: `Seqaya`, region: `ap-northeast-1` — **Tokyo**, not `europe-west1` as CLAUDE.md stated; CLAUDE.md will be corrected).

**Tables that actually exist:**

| Table | State |
| :--- | :--- |
| `public.device_readings` | Exists. 10,544 rows. 33 columns. RLS **enabled**. Firmware writes. |

**Tables that do NOT exist yet** (despite CLAUDE.md claiming they did):

- `public.profiles`
- `public.devices`
- `public.plants`

**Real devices currently in the field writing readings:**

| device_serial | readings_count | first_seen | last_seen |
| :--- | ---: | :--- | :--- |
| `"1"` | 4,786 | 2026-04-14 | active now |
| `"2"` | 4,747 | 2026-04-14 | active now |
| `"9"` | 1,014 | 2026-04-18 | active now |

Serials are short strings (not the `SQ-F3A-2971` format CLAUDE.md's mock showed). The app must not hardcode a serial format.

**Existing RLS policies on `device_readings`:**

```
Allow inserts from anon — WITH CHECK: true
Allow reads from anon   — USING:      true
```

⚠️ **Security problem, but not one Phase 3 can fix alone.** These policies mean anyone with the anon key (which ships in the APK) can read or inject any reading. Fixing this is cross-repo: firmware auth has to change too. Flagged in "Known risks" below. Phase 3 **adds new policies for `authenticated`** without touching the existing `anon` policies — so firmware keeps working and we layer the proper user-scoped policies on top. Production-hardening is a separate ticket.

---

## Database migrations introduced by Phase 3

Three migrations, idempotent, applied in order.

### Migration 1 — `profiles`

One row per signed-in user. Created automatically via trigger when `auth.users` gets a new row (Google sign-in flow).

```sql
create table public.profiles (
  id          uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  avatar_url   text,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);

alter table public.profiles enable row level security;

-- owner can see and edit their own row
create policy "profiles_select_own" on public.profiles
  for select to authenticated using (id = auth.uid());
create policy "profiles_update_own" on public.profiles
  for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

-- trigger: create profile on signup
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.profiles (id, display_name, avatar_url)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'full_name', split_part(new.email, '@', 1)),
    new.raw_user_meta_data ->> 'avatar_url'
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();
```

### Migration 2 — `devices`

The ownership mapping. One row per registered device, linked to a user.

```sql
create table public.devices (
  id             uuid primary key default gen_random_uuid(),
  owner_id       uuid not null references auth.users(id) on delete cascade,
  serial         text not null unique,         -- matches device_readings.device_serial verbatim
  nickname       text,                          -- user-chosen, e.g. "Lucy"
  plant_id       uuid,                          -- nullable until plant library lands (Phase 7)
  target_moisture_percent integer,
  hold_mode_active boolean not null default false,
  registered_at  timestamptz not null default now(),
  last_renamed_at timestamptz
);

create index devices_owner_id_idx on public.devices (owner_id);
create index devices_serial_idx   on public.devices (serial);

alter table public.devices enable row level security;

create policy "devices_select_own" on public.devices
  for select to authenticated using (owner_id = auth.uid());
create policy "devices_insert_own" on public.devices
  for insert to authenticated with check (owner_id = auth.uid());
create policy "devices_update_own" on public.devices
  for update to authenticated using (owner_id = auth.uid()) with check (owner_id = auth.uid());
create policy "devices_delete_own" on public.devices
  for delete to authenticated using (owner_id = auth.uid());
```

**No FK from `device_readings.device_serial` to `devices.serial`.** Why: readings arrive from firmware *before* the user registers the device (firmware writes on boot; registration happens minutes/days later via the Add Device wizard in Phase 5). A FK would reject those early readings. Ownership is joined in application code, not enforced at the DB level.

### Migration 3 — `device_readings` RLS for authenticated users

Adds `authenticated`-role policies alongside the existing `anon` policies. Does NOT drop the `anon` policies — firmware would break.

```sql
-- Authenticated users can read readings for devices they own.
create policy "readings_select_own" on public.device_readings
  for select to authenticated
  using (
    exists (
      select 1 from public.devices d
      where d.serial = device_readings.device_serial
        and d.owner_id = auth.uid()
    )
  );

-- Realtime publication (enable device_readings on the realtime publication if not already)
alter publication supabase_realtime add table public.device_readings;
```

Note: Supabase Realtime respects RLS on read. Once the `authenticated` SELECT policy is in place, a signed-in user subscribing to `device_readings` only receives rows for devices they own.

---

## Android-side architecture

### Modules / packages introduced

```
app/src/main/java/com/seqaya/app/
├── BuildConfigExt.kt                 # SUPABASE_URL / SUPABASE_ANON_KEY accessors
├── data/
│   ├── local/
│   │   ├── SeqayaDatabase.kt         # Room
│   │   ├── DeviceEntity.kt
│   │   ├── ReadingEntity.kt
│   │   └── DeviceDao.kt, ReadingDao.kt
│   ├── remote/
│   │   ├── SupabaseClientProvider.kt # single Supabase instance
│   │   ├── dto/                      # @Serializable wire models
│   │   │   ├── DeviceDto.kt
│   │   │   └── DeviceReadingDto.kt
│   │   └── DeviceRemoteSource.kt, ReadingRemoteSource.kt
│   └── repository/
│       ├── AuthRepository.kt         # wraps Supabase Auth
│       ├── DeviceRepository.kt       # Flow<List<Device>> merging Room + Supabase
│       └── ReadingRepository.kt      # Flow<LatestReading> per device, realtime
├── domain/
│   ├── model/                        # pure Kotlin
│   │   ├── Device.kt, DeviceState.kt, Reading.kt, AuthState.kt
│   └── usecase/                      # thin — only where genuine composition
├── di/
│   ├── SupabaseModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
└── ui/
    ├── auth/
    │   ├── SplashScreen.kt           # A·01 — breathing leaf, routes on session check
    │   ├── SignInScreen.kt           # A·02 — "Care, quietly."
    │   └── AuthViewModel.kt
    └── home/
        ├── HomeScreen.kt             # B·01 empty + B·02 populated
        ├── HomeViewModel.kt
        └── components/
            ├── DeviceCard.kt         # nickname + species + moisture% + sparkline + timestamps
            ├── Sparkline.kt          # Canvas draw, 48 points + water-event dots
            ├── OfflineBanner.kt      # F·01
            └── AttentionBanner.kt    # "Basil is thirsty" row above the list
```

### Navigation graph changes

Replace the 3-tab-only nav with:

```
splash (start)
  └─ signed-in? ──> home_graph (3 tabs: home/scan/library)
  └─ signed-out? ─> sign_in ──> home_graph
```

Splash does a one-shot session check (no animation waiting — the breathing leaf runs for up to 800ms while we query `supabase.auth.currentSessionOrNull()`; if we get an answer sooner, we route immediately).

### Auth state machine

```
Starting → Loading (session restore)
  → Unauthenticated        (no session)
  → Authenticated(user)    (session valid)
  → Error(message)         (network / auth failure — shown as toast, retry)
```

`AuthRepository` exposes `val authState: StateFlow<AuthState>`. The nav graph observes this and auto-routes.

### Sign-in flow

Google ID Token flow via `supabase-auth-kt`:

1. User taps "Continue with Google" → `CredentialManager` (Jetpack `androidx.credentials`) requests a Google ID token.
2. Token → `supabase.auth.signInWith(IDToken) { idToken = ...; provider = Google }`.
3. On success, trigger in `auth.users` creates the `profiles` row.
4. `AuthRepository` flips to `Authenticated`; nav routes to home.

**OAuth client ID** needs to be configured in the Supabase dashboard (Authentication → Providers → Google) with the Android app's SHA-1 / package name. That's a dashboard step, not code. Documented in the PR description when Phase 3 is ready to test.

### Offline-first data flow

```
Home UI  ─observes──>  HomeViewModel.state: StateFlow<HomeState>
                                  │
                                  └─ DeviceRepository.devicesWithLatest(ownerId)
                                           │
                       ┌───────────────────┴──────────────────┐
                       ▼                                      ▼
                 Room DeviceDao.flow()               SupabaseClient queries
                 Room ReadingDao.latestFlow()        (one-shot list + realtime stream)
                       ▲                                      │
                       └──────── cache write ─────────────────┘
```

`Flow.combine` merges device rows with their latest reading row. The repository writes every remote response into Room, then emits from Room. UI never sees a mid-flight network call — it sees the Room state that gets continuously updated.

Online/offline status: `ConnectivityManager` NetworkCallback piped into a `ConnectivityRepository` exposing `StateFlow<Boolean>`. The HomeScreen reads it and renders the offline banner when false.

### UI implementation — sticking to the design

- **Splash (A·01):** centered Fraunces wordmark (opsz 144 at 38sp), breathing leaf mark (2.2s loop, reduced-motion honored), v2.0 caption. From `seqaya-screens-auth.jsx` lines 3–16.
- **Sign in (A·02):** left-aligned "Care, quietly." (Fraunces 42sp), lede, Google button (terracotta fill, 52dp tall), fine-print T&C, italic tagline footer. From `seqaya-screens-auth.jsx` lines 19–46.
- **Home empty (B·01):** "A quiet beginning" eyebrow, "Your first plant is waiting." headline, "Add a device" CTA (disabled in Phase 3 — will be wired in Phase 5), "Browse the plant library" text link (also disabled), PlantSketch monstera rotated 8° in the bottom corner. Adding a device is genuinely out of scope for this phase; the buttons show the design but toast "Coming in the next update" on tap.
- **Home populated (B·02):** attention banner if any device has low moisture, vertical list of `DeviceCard`s. From `seqaya-screens-home.jsx` lines 50–77.
- **DeviceCard:** nickname + species italic + status dot + moisture% (Fraunces 48sp) + 24h sparkline with watering-event dots + "Last watered X / Seen Y" footer. From `seqaya-screens-home.jsx` lines 3–21.

**Sparkline rendering** in Compose `Canvas`. 48 points, water events as terracotta dots. Path reuses the algorithm from `seqaya-icons.jsx` lines 229–249 but generated from real `device_readings` rather than a seed. Computation is a memoized list-of-Offsets — O(N) in point count, runs once per data update.

### Dependency injection

- `SupabaseModule` provides `@Singleton SupabaseClient` configured with URL + anon key from `BuildConfig` (populated from `local.properties`).
- `DatabaseModule` provides `@Singleton SeqayaDatabase` and the DAOs.
- `RepositoryModule` binds repositories; they're plain Kotlin classes with constructor injection — not interfaces, since there's only one implementation per repo and premature interface-ification is exactly what [CLAUDE.md](../../CLAUDE.md) warns against. If Phase 4 or later introduces a second impl (fake, test, alternate backend), we add the interface then.

---

## Design-source mapping

| Screen | Design source | Phase 3 Compose target |
| :--- | :--- | :--- |
| A·01 Splash | [seqaya-screens-auth.jsx#A1_Splash](../design-system/source/seqaya-screens-auth.jsx) | `ui/auth/SplashScreen.kt` |
| A·02 Sign in | [seqaya-screens-auth.jsx#A2_SignIn](../design-system/source/seqaya-screens-auth.jsx) | `ui/auth/SignInScreen.kt` |
| B·01 Home empty | [seqaya-screens-home.jsx#B1_HomeEmpty](../design-system/source/seqaya-screens-home.jsx) | `ui/home/HomeScreen.kt` (empty state) |
| B·02 Home populated | [seqaya-screens-home.jsx#B2_HomePop](../design-system/source/seqaya-screens-home.jsx) | `ui/home/HomeScreen.kt` (populated state) |
| F·01 Offline banner | [seqaya-screens-settings.jsx#F1_Offline](../design-system/source/seqaya-screens-settings.jsx) | `ui/home/components/OfflineBanner.kt` |

All other screens remain placeholder/out-of-scope.

---

## Tests

Unit tests (JUnit + MockK + Turbine):

- `AuthViewModelTest` — sign-in success, sign-in network failure, session restore on cold start.
- `DeviceRepositoryTest` — Room-only (offline), Supabase-only (first-install, no cache), cache-then-network merge.
- `HomeViewModelTest` — empty state vs populated, attention banner shows only when dot is brown.
- `SparklineTest` — 48 points map to expected Offsets within the canvas bounds; water events render as dots.

No instrumented tests in Phase 3. Compose UI tests arrive in Phase 4 or later once the screen inventory is larger.

---

## What I will need from you (Muhammed) during implementation

1. **Supabase URL + anon key** for `jybsouuydgstafqsxfbx`. Paste in chat or drop into `local.properties`:
   ```
   SUPABASE_URL=https://jybsouuydgstafqsxfbx.supabase.co
   SUPABASE_ANON_KEY=eyJ...
   ```
2. **Google OAuth configuration.** I'll draft the dashboard steps as a checklist in the PR. You'll need the app's debug SHA-1 (I'll show you how to print it). Release SHA-1 is for a later phase.
3. **Approval to run the three migrations.** I'll apply them via Supabase MCP after you say yes.
4. **Accept the security caveat.** The existing `anon`-wildcard RLS policies stay in place for firmware compatibility. A follow-up ticket will coordinate with the firmware owner to replace anon with per-device credentials. Until then, the anon key in the APK is a known leak risk; the app-side `authenticated` policies we add are the right direction but don't close the hole on their own.

---

## Known risks

| Risk | Mitigation |
| :--- | :--- |
| Latency: project is in `ap-northeast-1` (Tokyo), user is in Israel. ~250ms round-trip. | Acceptable for Phase 3 (home list isn't latency-sensitive). Flag for a region migration before public launch — moving data is a one-hour downtime window. |
| Wildcard `anon` RLS on `device_readings`. Firmware depends on this; fixing requires firmware coordination. | Add `authenticated` policies now; schedule anon hardening as cross-repo work. Not a Phase 3 blocker but a pre-launch blocker. |
| `device_readings.device_serial` has no FK to `devices.serial`. | Intentional. Readings precede registration. App joins in code; `devices_serial_idx` keeps the join fast. |
| Google Sign-In requires SHA-1 registration. Debug builds rotate SHA-1 per developer machine. | Register multiple SHA-1s in the Google console. Document in PR. |
| Realtime requires `device_readings` on `supabase_realtime` publication. Migration 3 adds it — if it's already there, the `alter publication ... add` is idempotent-ish (fails on duplicate). | Migration 3 wraps in `do $$ begin ... exception when duplicate_object then null; end $$;`. |

---

## Phase 3 exit criteria

- [ ] Three migrations applied; `profiles` + `devices` exist; `authenticated`-role policies on `device_readings` active.
- [ ] App builds, CI green on the PR.
- [ ] Cold start: splash → sign-in; completing Google flow lands on home.
- [ ] Warm start: splash → home (session persisted).
- [ ] Home empty state renders for a brand-new account.
- [ ] After manually inserting a `devices` row pointing at serial `"1"` / `"2"` / `"9"` (via SQL console during testing), that device shows up on home with live readings, sparkline, and status dot.
- [ ] Kill network mid-session: offline banner appears; last-known moisture still renders.
- [ ] Restore network: realtime picks up new readings without a restart.
- [ ] Unit tests green locally and in CI.
