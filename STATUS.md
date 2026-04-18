# Status — Seqaya Android App

**Current phase:** Phase 2 — Android skeleton
**Last updated:** 2026-04-18 by Muhammed
**Next action:** Generate the Android project with Gradle KTS + version catalog

## Completed
- [x] Repo scaffold (CLAUDE.md, README, docs structure)
- [x] GitHub repo created and first commit pushed
- [x] Claude Design generated 22 screens + Locate/Hold/Resume
- [x] Design export copied to docs/design-system/

## In progress
- [ ] Generate Android project (Gradle KTS, libs.versions.toml, Hilt, Compose)

## Blocked / waiting
- (none)

## Next up (in order)
1. Base theme from design tokens (Color.kt, Typography.kt, Shape.kt)
2. App shell: SeqayaApp + MainActivity + bottom nav with 3 empty tabs
3. CI workflow: .github/workflows/ci.yml runs lint + unit tests on push
4. Phase 3 kickoff: Supabase auth + Home screen

## Decisions locked this week

**2026-04-18 — Design system gaps closed.** Four open questions from the initial design import resolved against `seqaya-design-prompt.md`. Full rationale in [docs/design-system/README.md](docs/design-system/README.md#decisions-locked-2026-04-18).

- **Dark-mode palette:** sage `#9eb37f`, terracotta `#e89878`, cream-ink `#1a1917`, card-dark `#25231f`. Do not re-derive.
- **Body text base:** 16sp (not 14sp). Global rule + design prompt agree; initial tokens were wrong, corrected.
- **Paper grain:** ship as `drawable-nodpi/paper_grain.png` (180×180 tileable, 2% alpha, multiply blend). Not a shader.
- **Fraunces:** ship variable TTF; use `FontVariation.Setting("opsz", …)` per type-scale tier. Min SDK 26 supports this natively — no static-cut fallback.
