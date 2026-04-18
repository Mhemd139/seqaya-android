# Seqaya Design System

Source of truth for the Seqaya Android app's visual design. Exported from Claude Design bundle `yYdiWjOnL3SqETo1Qoen7w` on 2026-04-18.

Compose implements. It does not reinvent.

## What's here

```
docs/design-system/
├── README.md              # this file — screen inventory + Compose mapping
├── tokens.json            # palette, type scale, radius, spacing, motion, components
└── source/
    ├── README.md          # original Claude Design handoff notes
    ├── chat1.md           # design chat transcript (intent + iteration history)
    ├── Seqaya App.html    # master HTML file with design tokens in :root
    ├── seqaya-app.jsx     # canvas composition (DesignSystem + 22+7 screens)
    ├── seqaya-icons.jsx   # custom icon set + LeafMark + PlantSketch + Sparkline
    ├── seqaya-screens-shared.jsx   # Phone frame, TopBar, TabBar, StepHeader
    ├── seqaya-screens-auth.jsx     # A1, A2
    ├── seqaya-screens-home.jsx     # B1–B6
    ├── seqaya-screens-scan.jsx     # C1–C4
    ├── seqaya-screens-library.jsx  # D1–D3
    ├── seqaya-screens-settings.jsx # E1–E3, F1–F5
    ├── seqaya-screens-nfc.jsx      # G1–G5 contextual sheets + illustrations
    └── android-frame.jsx           # Material 3 frame (unused by Seqaya — reference only)
```

## How to use this during implementation

1. **Every new Composable reads `tokens.json`.** The Compose theme (`ui/theme/Color.kt`, `Typography.kt`, `Shape.kt`, `Motion.kt`) is generated once from `tokens.json` and everything else pulls from the theme.
2. **When building a screen, open the matching JSX file in `source/`.** Copy the structure, the text, the spacing, the illustrations. Translate from JSX to Compose; do not redesign.
3. **If a Compose implementation has to diverge from the source** (e.g., Android platform constraint), write a note in the screen's spec file explaining why — do not silently drift.

## Stack reminders

- The design bundle is HTML/CSS/JS — a prototype medium.
- Target is Jetpack Compose + Material 3 (per [CLAUDE.md](../../CLAUDE.md)).
- Icons are **custom** (see [seqaya-icons.jsx](source/seqaya-icons.jsx)). Do not substitute with Heroicons, Lucide, or Material Symbols.
- Fraunces is a variable font — honor the `opsz` axis per the token scale.

## Screen inventory & Compose mapping

Total: **22 screens + 7 contextual + 5 shared components + 1 design-system preamble + 1 state diagram**.

### § 02 — Auth

| ID     | Name     | Source                                                        | Compose target (Phase 3)                                |
| :----- | :------- | :------------------------------------------------------------ | :------------------------------------------------------ |
| A · 01 | Splash   | [seqaya-screens-auth.jsx#A1_Splash](source/seqaya-screens-auth.jsx)   | `ui/auth/SplashScreen.kt`                               |
| A · 02 | Sign in  | [seqaya-screens-auth.jsx#A2_SignIn](source/seqaya-screens-auth.jsx)   | `ui/auth/SignInScreen.kt`                               |

### § 03 — Home

| ID     | Name                       | Source                                                                  | Compose target                                          |
| :----- | :------------------------- | :---------------------------------------------------------------------- | :------------------------------------------------------ |
| B · 01 | Home, empty                | [seqaya-screens-home.jsx#B1_HomeEmpty](source/seqaya-screens-home.jsx)  | `ui/home/HomeEmptyScreen.kt` (Phase 3)                  |
| B · 02 | Home, populated            | [seqaya-screens-home.jsx#B2_HomePop](source/seqaya-screens-home.jsx)    | `ui/home/HomeScreen.kt` (Phase 3)                       |
| B · 03 | Device detail              | [seqaya-screens-home.jsx#B3_DeviceDetail](source/seqaya-screens-home.jsx) | `ui/device/DeviceDetailScreen.kt` (Phase 4)           |
| B · 04 | Add device · Pick plant    | [seqaya-screens-home.jsx#B4_PickPlant](source/seqaya-screens-home.jsx)  | `ui/provisioning/PickPlantScreen.kt` (Phase 5)          |
| B · 05 | Add device · Wi-Fi         | [seqaya-screens-home.jsx#B5_WiFi](source/seqaya-screens-home.jsx)       | `ui/provisioning/WifiScreen.kt` (Phase 5)               |
| B · 06 | Add device · Tap           | [seqaya-screens-home.jsx#B6_TapPhone](source/seqaya-screens-home.jsx)   | `ui/provisioning/TapPhoneScreen.kt` (Phase 5)           |

### § 04 — Scan

| ID     | Name              | Source                                                             | Compose target (Phase 7)                    |
| :----- | :---------------- | :----------------------------------------------------------------- | :------------------------------------------ |
| C · 01 | Scan · camera     | [seqaya-screens-scan.jsx#C1_Camera](source/seqaya-screens-scan.jsx) | `ui/scan/CameraScreen.kt`                   |
| C · 02 | Scan · analyzing  | [seqaya-screens-scan.jsx#C2_Analyzing](source/seqaya-screens-scan.jsx) | `ui/scan/AnalyzingScreen.kt`              |
| C · 03 | Scan · result     | [seqaya-screens-scan.jsx#C3_Result](source/seqaya-screens-scan.jsx) | `ui/scan/ResultScreen.kt`                   |
| C · 04 | Scan · no match   | [seqaya-screens-scan.jsx#C4_NoMatch](source/seqaya-screens-scan.jsx) | `ui/scan/NoMatchScreen.kt`                  |

### § 05 — Library

| ID     | Name              | Source                                                                   | Compose target (Phase 7)                   |
| :----- | :---------------- | :----------------------------------------------------------------------- | :----------------------------------------- |
| D · 01 | Library           | [seqaya-screens-library.jsx#D1_Browse](source/seqaya-screens-library.jsx) | `ui/plants/LibraryScreen.kt`              |
| D · 02 | Plant detail      | [seqaya-screens-library.jsx#D2_PlantDetail](source/seqaya-screens-library.jsx) | `ui/plants/PlantDetailScreen.kt`      |
| D · 03 | Library · search  | [seqaya-screens-library.jsx#D3_Search](source/seqaya-screens-library.jsx) | `ui/plants/PlantSearchScreen.kt`          |

### § 06 — Settings & profile

| ID     | Name              | Source                                                                       | Compose target (Phase 8)                    |
| :----- | :---------------- | :--------------------------------------------------------------------------- | :------------------------------------------ |
| E · 01 | Settings          | [seqaya-screens-settings.jsx#E1_Settings](source/seqaya-screens-settings.jsx) | `ui/settings/SettingsScreen.kt`            |
| E · 02 | Profile           | [seqaya-screens-settings.jsx#E2_Profile](source/seqaya-screens-settings.jsx)  | `ui/settings/ProfileScreen.kt`             |
| E · 03 | Delete account    | [seqaya-screens-settings.jsx#E3_DeleteAccount](source/seqaya-screens-settings.jsx) | `ui/settings/DeleteAccountScreen.kt`  |

### § 07 — Shared parts

| ID     | Name                     | Source                                                                       | Compose target                              |
| :----- | :----------------------- | :--------------------------------------------------------------------------- | :------------------------------------------ |
| F · 01 | Offline banner           | [seqaya-screens-settings.jsx#F1_Offline](source/seqaya-screens-settings.jsx) | `ui/components/OfflineBanner.kt`            |
| F · 02 | Toast                    | [seqaya-screens-settings.jsx#F2_Toast](source/seqaya-screens-settings.jsx)   | `ui/components/Toast.kt`                    |
| F · 03 | Device selector sheet    | [seqaya-screens-settings.jsx#F3_DeviceSheet](source/seqaya-screens-settings.jsx) | `ui/components/DeviceSelectorSheet.kt`  |
| F · 04 | Plant selector sheet     | [seqaya-screens-settings.jsx#F4_PlantSheet](source/seqaya-screens-settings.jsx) | `ui/components/PlantSelectorSheet.kt`   |
| F · 05 | Empty state              | [seqaya-screens-settings.jsx#F5_Empty](source/seqaya-screens-settings.jsx)   | `ui/components/EmptyState.kt`               |

### § 09 — Contextual sheets (NFC tap-driven)

These open after an NFC tap, routed by device state. One bottom sheet, not a modal stack.

| ID     | Name                          | Source                                                               | Compose target (Phase 5)                    |
| :----- | :---------------------------- | :------------------------------------------------------------------- | :------------------------------------------ |
| G · 01 | Locate — which plant?         | [seqaya-screens-nfc.jsx#G1_Locate](source/seqaya-screens-nfc.jsx)    | `ui/contextual/LocateSheet.kt`              |
| G · 02 | Hold — pause                  | [seqaya-screens-nfc.jsx#G2_Hold](source/seqaya-screens-nfc.jsx)      | `ui/contextual/HoldSheet.kt`                |
| G · 03 | Hold — paused confirmation    | [seqaya-screens-nfc.jsx#G3_HoldConfirm](source/seqaya-screens-nfc.jsx) | `ui/contextual/HoldConfirmSheet.kt`       |
| G · 04 | Resume — wake                 | [seqaya-screens-nfc.jsx#G4_Resume](source/seqaya-screens-nfc.jsx)    | `ui/contextual/ResumeSheet.kt`              |
| G · 05 | Resume — welcome back         | [seqaya-screens-nfc.jsx#G5_ResumeConfirm](source/seqaya-screens-nfc.jsx) | `ui/contextual/ResumeConfirmSheet.kt`   |

The `IdentityCard` composable (name + species/location/added + photo + moisture/lastWatered/state ribbon + italic history line) is shared across G·01, G·02, G·04. Implement once at `ui/contextual/components/IdentityCard.kt`.

### Reusable components (from screens)

Extract during implementation — not screens of their own, but called from many places.

| Component      | Seen in                              | Compose target                               |
| :------------- | :----------------------------------- | :------------------------------------------- |
| `DeviceCard`   | B·02, F·01, F·02                     | `ui/components/DeviceCard.kt`                |
| `Sparkline`    | B·02 card, B·03 chart                | `ui/components/Sparkline.kt` (or Vico)       |
| `LeafMark`     | A·01 splash, F·05 empty              | `ui/components/LeafMark.kt` (custom draw)    |
| `PlantSketch`  | B·04, D·01, D·02, D·03, F·04, G·*    | `ui/components/PlantSketch.kt` (9 variants)  |
| `StepHeader`   | B·04, B·05, B·06                     | `ui/components/StepHeader.kt`                |
| `TopBar`       | B·03, C·*, D·02, D·03, E·*, F·5      | `ui/components/SeqayaTopBar.kt`              |
| `TabBar`       | All tab-bearing screens              | `ui/navigation/SeqayaBottomBar.kt`           |

## Design principles (enforced during review)

1. **Cream does the work.** Terracotta and sage speak only when they have something to say. Don't color a button because it "looks dull."
2. **Hairlines over shadows.** Every border is 1dp. No elevation shadows on app surfaces (the phone frame in mockups is the only shadow in the whole system).
3. **No gradients, no glassmorphism.** The camera viewfinder's radial is a viewfinder, not decoration — do not generalize.
4. **Fraunces carries feeling, Inter carries work.** Serif on titles, numbers, italic asides. Sans for everything transactional.
5. **Custom icons only.** See [seqaya-icons.jsx](source/seqaya-icons.jsx). Never swap in Material Symbols or Lucide.
6. **Motion is slow and eased.** `cubic-bezier(0.2, 0.8, 0.2, 1)`, 180ms micro / 260ms state / 420ms screen. No springs. Reduced-motion disables the three looping animations (leaf breathe, NFC wave, orbit loader).
7. **Color is never the sole signal.** Status dots are always paired with a text label.
8. **Hairline dividers at 1dp `border.default`**, vertical margin 14dp. Not `Divider()` with default thickness.
9. **Dark mode: 5 screens inverted.** Ink ↔ cream, accents lighten slightly. Exact dark-accent hex values TBD in Phase 2 theme implementation.

## Decisions locked (2026-04-18)

The four open questions from the initial import have been resolved against `seqaya-design-prompt.md` (the authoritative design prompt). These are locked — don't re-open them without a new design-review cycle.

### 1. Dark-mode palette — LOCKED

Pinned from [seqaya-design-prompt.md:299–307](source/uploads/seqaya-design-prompt.md):

| Token             | Hex        | Notes                                 |
| :---------------- | :--------- | :------------------------------------ |
| bg.cream          | `#1a1917`  | Near-black surface                    |
| bg.creamLight     | `#25231f`  | Card surface on dark                  |
| text.primary      | `#faf9f5`  | Inverts to cream                      |
| text.secondary    | `#b0aea5`  | Same as light-mode tertiary           |
| accent.green      | `#9eb37f`  | Sage, lightened for contrast          |
| accent.brown      | `#e89878`  | Terracotta, lightened for contrast    |

Extended values (`creamLightest`, `border.default`, `border.strong`, soft-accent rgbas, `accent.blue` dark) derived by applying the same light-mode relationships to the dark base. See `tokens.json → color.dark`.

### 2. Body text base size — LOCKED at 16sp

The design prompt and the global CLAUDE.md agree: body is 16sp on mobile with 1.55 line-height. My initial tokens treated the HTML's 14sp as body — that was wrong. The HTML uses 14sp for inline helpers inside dense cards. Token scale corrected:

| Token           | Size | Purpose                                           |
| :-------------- | :--- | :------------------------------------------------ |
| `body`          | 16sp | Default body copy                                 |
| `bodySecondary` | 15sp | Dense card interiors where 16sp would wrap badly  |
| `caption`       | 13sp | Card metadata (last watered, seen)                |
| `fine`          | 12sp | Legal fine print                                  |

### 3. Paper grain — LOCKED as tiled PNG

The grain is load-bearing: the design forbids flat fills and says it "adds warmth." On Android:

- **Asset:** `app/src/main/res/drawable-nodpi/paper_grain.png` — 180×180 tileable PNG at 2% alpha.
- **Generation:** pre-render once from the SVG spec (fractalNoise baseFrequency 0.9, numOctaves 2, colorMatrix 0.08/0.08/0.07), rasterize, set alpha to 2%.
- **Render:** Compose `drawBehind` on the root Scaffold surface using `BitmapShader` with `TileMode.REPEAT` and `BlendMode.Multiply`.

Not a shader, not skipped. The 2% opacity is the spec (`seqaya-design-prompt.md:353`); the HTML's 35% value is a CSS mix-blend artifact, not the intent.

### 4. Fraunces variable font — LOCKED

- **Ship the variable TTF** (`Fraunces[SOFT,WONK,opsz,wght].ttf` + matching italic). Place in `app/src/main/res/font/`.
- Android 26+ supports variable fonts via `FontVariation.Setting`; min SDK is exactly 26, so no static fallback needed.
- Active `opsz` values used in the design, per audit:
  - `14` — inline serif (wordmark)
  - `24` — hM (20sp headings)
  - `36` — hL (26sp section titles)
  - `72` — hXL (34sp page titles) + displayL (48sp card numbers)
  - `96` — IdentityCard nickname (38sp)
  - `144` — displayXL (72sp hero number on device detail)
- Compose: build a `FontFamily` with two files (upright + italic) and set per-style `FontVariation.Setting("opsz", N), FontVariation.Setting("wght", 500)` in `Typography.kt`.

See `tokens.json → typography.fraunces` for the full spec.

## What NOT to do

- Do not render the HTML in a browser and screenshot it. Everything is in the source files — read them.
- Do not copy CSS-in-JS inline styles into Compose verbatim. Translate through `tokens.json` and the theme.
- Do not invent new screens. The 22 + 7 + 5 inventory above is complete for v2.0.
- Do not commit any design file to `docs/design-system/` by hand-editing. Re-export from Claude Design and replace `source/` wholesale.
