# Seqaya Android App — Architecture

## System Context

```
                           ┌────────────────────────┐
                           │   Supabase (EU-west1)  │
                           │   jybsouuydgstafqsxfbx │
                           │   Postgres + Auth +    │
                           │   Realtime + Storage   │
                           └────────┬───────────────┘
                                    │
                                    │ Kotlin SDK
                                    ▼
    ┌──────────┐   NFC HCE   ┌──────────────┐   Retrofit   ┌───────────────┐
    │  ESP32   │◄───────────►│  Android App │─────────────►│ FastAPI on    │
    │ firmware │             │  (this repo) │              │ Vercel        │
    └──────────┘             └──────────────┘              │ (plant ID,    │
         ▲                                                  │  care tips)   │
         │ HTTPS                                            └───────────────┘
         │ + MQTT
         ▼
    Supabase.device_readings  ← firmware writes directly
```

## Internal Layers

```
ui/       ← Compose screens + ViewModels (state holders)
 │        (reads state from repositories via Flows)
 ▼
domain/   ← pure Kotlin models + use cases (no Android imports)
 │        (use cases orchestrate repositories)
 ▼
data/     ← repositories (single source of truth per domain)
          ├── local/  (Room — offline cache)
          ├── remote/ (Supabase + AI backend API)
          └── repository/ (merges local + remote)
```

**Dependency direction:** ui → domain → data. Never reverse.

## Key Data Flows

### Reading device moisture (real-time)
1. `ReadingRepository` opens a Supabase Realtime subscription to `device_readings` filtered by the user's device serials.
2. Each new row → flow emission → ViewModel `StateFlow` → Compose recomposes.
3. Room cache is updated on every emission for offline fallback.
4. If the subscription disconnects (network lost), UI shows "offline" badge + last cached value.

### Adding a device (NFC provisioning)
1. User completes wizard (pick plant → WiFi → ready to tap).
2. App calls `SeqayaHceService.prepare(payload)` — HCE is now ready to respond when polled.
3. User presses device button → NFC field activates → device polls phone → HCE sends payload in 10-byte APDU chunks.
4. On success: device confirms via subsequent Supabase write → app's realtime subscription sees the new `device_readings` row → wizard transitions to success.

### Contextual device sheet (Locate / Hold / Resume)
1. User presses device button, brings phone close.
2. Foreground-dispatch NFC handler in `MainActivity` reads the device serial.
3. App queries Supabase for this device's current state (owner, plant, hold mode).
4. Based on state, app opens the right bottom sheet (`contextual/LocateSheet`, `HoldSheet`, `ResumeSheet`, or `ReprogramSheet`).
5. User confirms action → app sends the appropriate APDU command via HCE → device acts → state updates in Supabase.

## Why These Choices

- **Supabase over Firebase:** firmware already writes to Supabase (`device_readings`). Having two data stores would mean dual-write complexity. Postgres is also better for time-series queries (moisture charts) than RTDB.
- **Compose over XML:** faster to build, fewer files, native previews. Google's investment direction.
- **Hilt over Koin:** compile-time DI, better IDE support for Android.
- **Room over SQLDelight:** simpler, maps cleanly onto Supabase row shapes.
- **Vico over MPAndroidChart:** Compose-native, no AndroidView wrapping.

## Future

- Phase 9: disease detection (FastAPI → Replicate/HF), pgvector-based care recommendations.
- iOS: likely Kotlin Multiplatform sharing `domain/` + `data/`, native SwiftUI for UI.
