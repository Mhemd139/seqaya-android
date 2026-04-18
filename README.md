# Seqaya Android App

The companion Android app for Seqaya plant-watering devices.

## Status

Phase 0: scaffolded. Android project not yet generated.

Next: Phase 1 (design system in Claude Design → export to `docs/design-system/`) and Phase 2 (Supabase schema + Android skeleton).

## Stack

Kotlin + Jetpack Compose + Material 3 + Hilt + Room + Supabase Kotlin SDK + Coroutines/Flow + Vico charts + Coil.

## Setup (once the Android project is generated)

```bash
# Open in Android Studio Ladybug or later
# Requires JDK 17+, Android SDK 35

./gradlew assembleDebug     # debug build
./gradlew test              # unit tests
./gradlew connectedCheck    # instrumented tests
```

## Design

Designs live in Claude Design (Anthropic, launched 2026-04-16). Exports land in `docs/design-system/`. The palette is the Anthropic cream + sage-green + terracotta-brown set. Typography is Fraunces + Inter.

## Architecture

- **MVVM** with clean `data/ → domain/ → ui/` layering.
- **Supabase** for auth, Postgres, Realtime, and Storage.
- **Room** for offline-first caching.
- **NFC HCE** for device provisioning (protocol unchanged from v1 firmware).

## License

TBD.
