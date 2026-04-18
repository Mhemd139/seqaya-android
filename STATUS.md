# Status — Seqaya Android App

**Current phase:** Phase 2 — Android skeleton (COMPLETE, awaiting first CI run)
**Last updated:** 2026-04-18 by Muhammed
**Next action:** Open the project in Android Studio, let it sync, confirm the app launches on emulator/device. Then kick off Phase 3.

## Completed
- [x] Repo scaffold (CLAUDE.md, README, docs structure)
- [x] GitHub repo created and first commit pushed
- [x] Claude Design generated 22 screens + Locate/Hold/Resume
- [x] Design export copied to docs/design-system/ with tokens.json
- [x] Gradle KTS project scaffolded (settings, build, version catalog, wrapper 8.10.2)
- [x] `app` module with Compose + Hilt + Kotlinx Serialization + Room + Supabase BOM + Retrofit + Coil + Vico
- [x] AndroidManifest + adaptive launcher icon + backup rules
- [x] Fraunces / Inter / JetBrains Mono variable TTFs in res/font
- [x] 180×180 paper_grain.png tile (2% alpha) + PaperGrain Composable
- [x] Theme from design tokens: Color, Typography, Shape, Spacing, Motion, SeqayaTheme
- [x] SeqayaApp (Hilt entry) + MainActivity (edge-to-edge) + SeqayaRoot
- [x] Bottom nav with 3 tabs (Home, Scan, Library) + custom stroke glyphs
- [x] NavHost with placeholder tab screens
- [x] Unit-test smoke test + JUnit/MockK/Turbine test deps
- [x] CI workflow: .github/workflows/android.yml — lint + unit tests on push/PR

## In progress
- [ ] Manual verification: open in Android Studio, sync, run on emulator

## Blocked / waiting
- (none)

## Next up (in order)
1. **Phase 3 — Auth + Home:** Sign in with Google via Supabase Auth, session persistence, Home screen with device list + Supabase Realtime subscription, Room cache for offline.
2. **Phase 4 — Device detail + charts:** Vico moisture chart, rename/delete/reassign device.
3. **Phase 5 — NFC provisioning + contextual sheets:** SeqayaHceService, ApduProtocol, Add Device wizard, Locate/Hold/Resume sheets.

## Decisions locked this week

**2026-04-18 — Design system gaps closed.** Four open questions from the initial design import resolved against `seqaya-design-prompt.md`. Full rationale in [docs/design-system/README.md](docs/design-system/README.md#decisions-locked-2026-04-18).

- **Dark-mode palette:** sage `#9eb37f`, terracotta `#e89878`, cream-ink `#1a1917`, card-dark `#25231f`. Do not re-derive.
- **Body text base:** 16sp (not 14sp). Global rule + design prompt agree; initial tokens were wrong, corrected.
- **Paper grain:** ship as `drawable-nodpi/paper_grain.png` (180×180 tileable, 2% alpha, multiply blend). Not a shader.
- **Fraunces:** ship variable TTF; use `FontVariation.Setting("opsz", …)` per type-scale tier. Min SDK 26 supports this natively — no static-cut fallback.

**2026-04-18 — Phase 2 stack locked.**

- Gradle 8.10.2 + AGP 8.6.1 + Kotlin 2.0.21 + KSP 2.0.21-1.0.28.
- Compose BOM 2024.10.01 (latest stable at scaffold time). Hilt 2.52. Supabase Kotlin SDK 3.0.2 via BOM. Ktor OkHttp engine.
- `namespace` and `applicationId`: `com.seqaya.app`. Debug variant appends `.debug` suffix.
- Font files named `fraunces.ttf`, `fraunces_italic.ttf`, `inter.ttf`, `inter_italic.ttf`, `jetbrains_mono.ttf` (Android `res/font` requires lowercase + underscores).
- `SeqayaTheme` exposes `Seqaya.colors / .type / .shapes / .space / .motion` via CompositionLocals. Material 3 `colorScheme` and `Typography` are derived from the Seqaya tokens so M3 components read from the same palette.
- Bottom-nav glyphs are hand-drawn on `Canvas` per the design's "not Heroicons, not Lucide" rule; only 3 icons exist yet (home/scan/book) — the rest land as the screens that need them are built.
