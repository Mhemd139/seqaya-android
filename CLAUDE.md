# Seqaya Android App — CLAUDE.md

Native Android app for Seqaya plant-watering devices. v2 rewrite, Kotlin + Compose, Supabase-only backend.

## What This Repo Is

The consumer-facing Android app that:
- Signs users in via Google (Supabase Auth).
- Lists and manages Seqaya devices.
- Provisions new devices via NFC HCE.
- Identifies plants via camera (calls FastAPI backend).
- Shows moisture data in real time (Supabase Realtime subscription).

## What This Repo Is Not

- Not the firmware — `C:\Dev\Seqaya\Firmware`.
- Not the AI backend — `C:\Dev\Seqaya\Backend`.
- Not the v1 tutorial-fork app — that's frozen at `c:\Users\mhemd\Desktop\Projects\` and is read-only reference.

## Stack (Locked — Do Not Relitigate)

| Layer | Choice |
|---|---|
| Language | Kotlin only (no Java) |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + data/domain/ui clean layers |
| DI | Hilt |
| Async | Coroutines + Flow |
| Local cache | Room |
| Remote + auth | Supabase (Auth, Postgres, Realtime, Storage) |
| AI backend client | Retrofit (to Vercel FastAPI) |
| Charts | Vico (Compose-native) |
| Image loading | Coil |
| Build | Gradle Kotlin DSL + version catalog (`libs.versions.toml`) |
| Min SDK | 26 |
| Target SDK | 35 |
| Package | `com.seqaya.app` |

Kill Firebase. Do not introduce it. Supabase handles everything backend-side.

## Design System

The design is generated in **Claude Design** (Anthropic's tool, launched 2026-04-16) and exported to `docs/design-system/`. The design file is the source of truth — Compose implements, it does not reinvent.

- **Palette:** Anthropic tokens. `bg.cream #faf9f5`, `text.primary #141413`, `accent.green #788c5d`, `accent.brown #d97757`.
- **Typography:** Fraunces (display serif) + Inter (body sans).
- **Aesthetic:** editorial literary warmth. No glassmorphism, no gradients, no shadows, no emoji icons. Hand-drawn illustration set, slow eased motion.

## Structure

```
app/src/main/java/com/seqaya/app/
├── SeqayaApp.kt                # Application class (Hilt entry)
├── MainActivity.kt             # Single activity
├── data/
│   ├── local/                  # Room DB, entities, DAOs
│   ├── remote/                 # Supabase client, DTOs, Retrofit backend API
│   └── repository/             # DeviceRepository, PlantRepository, ReadingRepository
├── domain/                     # Pure Kotlin models + use cases
├── ui/
│   ├── theme/                  # Color, Typography, Shape, SeqayaTheme
│   ├── components/             # Reusable Compose components
│   ├── navigation/             # NavGraph, Destinations
│   ├── auth/                   # Sign In
│   ├── home/                   # Device list, today view
│   ├── device/                 # Device detail, charts
│   ├── provisioning/           # Add Device wizard (B4-B6)
│   ├── contextual/             # Locate, Hold, Resume sheets
│   ├── scan/                   # Camera, analyzing, result, error
│   ├── plants/                 # Library, detail, search
│   └── settings/               # Settings, profile, delete-account
├── nfc/                        # SeqayaHceService, ApduProtocol, ProvisioningPayload
├── wifi/                       # CurrentWifiProvider (SSID prefill)
└── di/                         # Hilt modules
```

## Critical Rules

### NFC Protocol
The AID is `F2 23 34 45 56 67`. APDU chunks are 10 bytes. Provisioning payload: `$SSID$Password$UserID$Serial$MoistureTarget$HoldMode$`. Future commands: `CMD_LOCATE`, `CMD_HOLD_TOGGLE`, `CMD_REPROGRAM` (see master plan Phase 4a).

**Never change the protocol without also changing it in `C:\Dev\Seqaya\Firmware`.**

### Supabase
- Project: `jybsouuydgstafqsxfbx` (region: ap-northeast-1 / Tokyo, existing).
- Tables: `profiles`, `plants`, `devices`, `device_readings` (existing — firmware writes).
- RLS: user owns their devices via `owner_id = auth.uid()`.
- Never `SELECT *`. Name every column in every query.
- Count-only queries use `{ count: 'exact', head: true }`.

### Code Quality
- No `any` — Kotlin has no `Any`-as-excuse; use generics or sealed types.
- Server-side errors: log full error, return safe message to UI.
- Client errors: set error state in ViewModel, render in Compose.
- Parallel independent async calls always. Never sequential `await` without a dependency reason.
- Debounce user input (300ms min) before hitting Supabase.
- Every query that could return many rows must be bounded: `.limit()`, date filter, or pagination.
- No debug logs in committed code.
- No Java files. If you find one, flag it.

### UI
- Every icon-only button has `contentDescription` (Compose equivalent of `aria-label`).
- Touch targets minimum 44×44dp. Primary buttons 56dp tall.
- Color is never the sole signal. Status dots are always paired with text.
- Respect `prefers-reduced-motion` via `LocalAccessibilityManager`.
- Body text ≥ 16sp.

### Account Deletion
Required by Play Store. Must be discoverable in-app. Implementation: delete Supabase Auth user → cascade deletes profile + devices via FK `on delete cascade`.

## Testing

- Unit tests: JUnit + MockK + Turbine (for Flow testing).
- Instrumented: HCE service with mocked APDUs, critical Compose screens.
- CI: lint + unit tests on every push to feature branches.

## Git Rules

- `main` is protected. Feature branches merge via PR.
- Never commit `google-services.json` (we don't use Firebase, but just in case).
- Never commit `local.properties` or signing keys.
- Never force-push to main.
- Never `--no-verify`.

## Rules for Any Agent Working Here

1. **Read the master plan:** `C:\Users\mhemd\.claude\plans\crispy-conjuring-pumpkin.md`.
2. **Read the design prompt plan:** `C:\Users\mhemd\.claude\plans\seqaya-design-prompt.md`.
3. **Design-system exports in `docs/design-system/` are the source of truth for UI.** Do not invent screens or tokens.
4. **Every feature: spec first in `docs/specs/YYYY-MM-DD-*.md`, approval, then code.**
5. **Follow the global rules in `C:\Users\mhemd\.claude\CLAUDE.md`** — clean code, no lazy patches, no unnecessary abstractions, no `any`.
