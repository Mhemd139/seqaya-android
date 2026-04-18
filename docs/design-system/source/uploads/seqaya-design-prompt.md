# Seqaya v2 — Design Prompt for Claude Design

## Context

This plan file is a **single, polished prompt** to paste into Claude Design (Anthropic's new design tool, launched 2026-04-16). It produces the design system and all screens for the Seqaya v2 Android app. The prompt encodes the principles from the `ui-ux-pro-max` skill (accessibility, touch targets, consistency, pre-delivery checklist) and the stance from the `frontend-design` skill (commit to a bold aesthetic direction, avoid AI-slop defaults, distinctive typography).

The technical plan lives in `crispy-conjuring-pumpkin.md`. **This plan is only for the visual design phase.** When Claude Design finishes, the output (tokens + screen prototypes) feeds Claude Code (in the Android repo) for implementation.

**Design goal:** every screen in the app feels like it belongs in an Anthropic Instagram post — editorial, warm, unhurried, confident. Not tech-startup. Not dashboard-flat. A calm, literary product that happens to water plants.

---

## How to Use This File

1. Open Claude Design (Pro/Max subscription required).
2. Start a new project called **Seqaya**.
3. Copy the entire **THE PROMPT** section below into the first message.
4. Let Claude Design generate the design system + all screens.
5. Iterate in Claude Design using the inline tools (comments, sliders, edits).
6. When satisfied, export each screen → packaged for Claude Code.
7. In the Android repo (`C:\Dev\Seqaya\Android_app`), point Claude Code at the exported package to implement as Jetpack Compose.

---

## Screen Inventory — Every Screen, Every Piece of Content

Seqaya has three primary tabs (Home, Scan, Library) + auth + settings + provisioning + device detail. Full count below. Every screen is listed with exactly what it displays.

### A. Auth

**A1. Splash / Loading**
- Seqaya wordmark, center, serif display.
- A single slow-breathing leaf mark beneath it (SVG, ~40px).
- Cream background `#faf9f5`.
- No spinner. Max 1.5s before routing to A2 or B1.

**A2. Sign In**
- Wordmark top-left, small.
- Centered block:
  - Serif headline: "Care, quietly."
  - Sans subline: "Seqaya keeps your plants watered while you live your life."
  - Single button: **Continue with Google** (brown `#d97757` fill, cream text, 56px tall).
  - Fine print under button: "By continuing you agree to our Terms and Privacy."
- Footer: "Made for the windowsill, not the warehouse." (muted gray `#b0aea5`, italic).

### B. Home (Tab 1 of 3)

**B1. Home — empty state (no devices yet)**
- Top bar: Seqaya wordmark left, avatar circle right.
- Centered composition:
  - Serif headline: "Your first plant is waiting."
  - Sans paragraph (2 lines): "Add your Seqaya device to start. It takes about a minute."
  - Primary button: **Add a device** (brown fill).
  - Secondary text link beneath: "Browse the plant library."
- Hand-drawn leaf illustration, off-center, bottom-right third of screen.

**B2. Home — populated (one or more devices)**
- Top bar: Seqaya wordmark left, "Add" icon-button (plus) right, avatar circle far right.
- Scrollable list of **Device Cards**. Each card shows:
  - Top row: plant nickname (serif, 20pt) + status dot (green = happy, brown = needs attention, gray = offline).
  - Second row: plant species (sans italic, muted).
  - Large moisture percentage (serif, 48pt, tabular-nums).
  - Sparkline chart (last 24h moisture), 60px tall, green stroke, no axes.
  - Bottom row: "Last watered 3h ago" (sans, small, muted) + "Last seen" timestamp.
  - Card surface: cream `#faf9f5` on cream-light `#e8e6dc` bg, 1px border `#e8e6dc`, 20px radius, generous 24px internal padding.
  - Tap → B3 (Device Detail).
- If any device is in "needs attention" (low moisture, offline >30 min): show a top banner above the list, brown accent, one-liner + "Review" text button.

**B3. Device Detail**
- Top bar: back arrow left, device nickname center (serif 18pt), overflow menu right.
- Hero section (top third of screen):
  - Plant species (sans italic, muted).
  - Current moisture % (serif, 72pt).
  - "Target: 60%" (sans, small, muted).
  - State chip: "Thriving" / "Thirsty" / "Soaked" / "Offline" — colored dot + label.
- Chart section:
  - Moisture over time (line chart, green stroke, cream background).
  - Segmented control above chart: 24h / 7d / 30d (default 7d).
  - Y-axis hidden, X-axis minimal (just day labels).
  - Watering events shown as small brown dots on the line.
- Care block:
  - "Care for [Species]" (serif heading).
  - Bulleted care tips (3-5 items, sans, from Perenual).
- Device block:
  - Row: "Device serial" — tap to copy.
  - Row: "Plant" — current plant, tap to change.
  - Row: "Nickname" — tap to edit inline.
  - Row: "Last seen" — timestamp.
  - Row: "Firmware" — version string, muted.
  - Row: "Delete device" — brown text, destructive.

**B4. Add Device Wizard — Step 1 of 3: Pick a Plant**
- Top bar: X close left, title "Step 1 of 3" center (sans caps, muted), "Skip for now" right text-button.
- Below title: serif headline "What are you growing?"
- Search field with leaf icon.
- Below search: grid of common plants (2 columns), each tile shows:
  - Hand-drawn illustration of the plant.
  - Common name (serif).
  - Scientific name (sans italic, muted).
- At bottom: text link "Can't find it? Add a custom plant."
- Primary button anchored bottom: **Next** (brown, disabled until selection).

**B5. Add Device Wizard — Step 2 of 3: Connect to Wi-Fi**
- Top bar: back arrow left, title "Step 2 of 3" center, "Skip for now" right.
- Serif headline: "Which Wi-Fi?"
- Sans explanation (2 lines): "Your device connects to this network. 2.4 GHz only — most home networks are fine."
- **SSID field**: pre-filled with the phone's current network name, tap to edit or change.
- **Password field**: masked, with eye icon to reveal. Helper text: "We never store your password."
- Expandable "Choose a different network" — opens system WiFi picker.
- Primary button anchored bottom: **Next**.

**B6. Add Device Wizard — Step 3 of 3: Tap Your Phone**
- Top bar: back arrow left, title "Step 3 of 3" center.
- Serif headline: "Tap your phone to the device."
- Centered illustration: phone and device silhouettes with NFC waves animating between them (slow, eased).
- Below illustration: status text that cycles:
  - Idle: "Hold the back of your phone against the Seqaya logo on the device."
  - Transmitting: "Sending setup… don't move."
  - Done: "Done. [Plant nickname] is online."
- On success: confetti-free; a subtle green checkmark fades in, then auto-advance to B2.
- On failure: brown banner "Didn't work — try again" + retry button.

### C. Scan (Tab 2 of 3)

**C1. Scan — Camera**
- Full-bleed camera preview.
- Top bar (overlaid, cream semi-transparent): back arrow left, title "Identify" center, flash toggle right.
- Camera viewfinder with corner brackets (brown accent), square aspect.
- Instruction text above viewfinder: "Center the leaf in the frame."
- Bottom controls (overlaid):
  - Gallery icon (pick from photos) left.
  - Large capture button center (cream circle with brown inner ring, 72px).
  - Flip camera icon right.
- Fine print at bottom: "Works best with a single leaf, good light."

**C2. Scan — Analyzing**
- Frozen capture shown at top, square.
- Below: serif headline "Looking closely…"
- Animated loading state: 3 leaves rotating slowly around a center point (SVG).
- No percentage. No progress bar. Just patient motion.
- Cancel text-link at bottom.

**C3. Scan — Result**
- Top bar: X close left, share icon right.
- Captured image, square, top.
- Below image:
  - Serif headline: common name (e.g., "Fiddle Leaf Fig").
  - Sans italic subline: scientific name (e.g., "Ficus lyrata").
  - Confidence chip: "Confident match" (green dot) or "Possible match" (brown dot).
- Care summary block (from Perenual):
  - Water icon + "Weekly, keep soil moist."
  - Sun icon + "Bright, indirect light."
  - Temp icon + "18-24°C."
- Primary CTA: **Add to a device** (opens device selector sheet).
- Secondary CTA: "View in library" → E2.
- Tertiary text link at bottom: "Not right? Try another photo."

**C4. Scan — Error / No match**
- Same top bar.
- Captured image, square.
- Serif headline: "I'm not sure what this is."
- Sans paragraph: "Try another photo with better light, or browse the library."
- Buttons: "Try again" (brown), "Browse library" (text link).

### D. Library (Tab 3 of 3)

**D1. Library — Browse**
- Top bar: wordmark left, search icon right.
- Below top bar: horizontal scroll of category chips (All, Herbs, Leafy, Succulents, Flowering, Tropical, etc.).
- Below chips: section headings (serif) + 2-column grid of plant tiles. Each tile:
  - Hand-drawn illustration.
  - Common name (serif).
  - Scientific name (sans italic, muted, smaller).
  - Tap → D2.
- Sections: "Popular this month", "Easy to start", "For low light", "Herbs".
- Empty footer card: "Suggest a plant" text link.

**D2. Library — Plant Detail**
- Top bar: back arrow left, bookmark icon right.
- Hero: hand-drawn illustration, large, centered on cream.
- Below hero:
  - Serif headline: common name.
  - Sans italic: scientific name.
  - Summary paragraph (2-3 lines from Perenual).
- Care grid (2×2 tiles):
  - Water (icon + frequency + "keep soil moist").
  - Light ("bright indirect").
  - Temperature ("18-24°C").
  - Humidity ("moderate").
- Longer care notes section (serif heading + paragraphs).
- "Troubles" section: common issues (brown accent icons): yellow leaves, drooping, root rot.
- CTA at bottom: **Assign to a device** → sheet.

**D3. Library — Search**
- Top bar: search field active with cursor, X to dismiss right.
- Below: recent searches (text links, muted).
- As user types: results appear as a list (tile on left, name + species on right).

### E. Settings + Profile

**E1. Settings**
- Top bar: back arrow left, title "Settings" center.
- Sections (grouped, cream-light backgrounds):
  - **Account**: Profile name, email (readonly), "Edit profile" → E2.
  - **Preferences**: Theme (Auto / Light / Dark), Units (Celsius / Fahrenheit), Notifications toggle.
  - **About**: App version, Privacy policy (external link), Terms (external link), Open source licenses.
  - **Danger zone**: "Sign out" (text button), "Delete account" (brown destructive text button) → E3.

**E2. Profile**
- Top bar: back, title "Profile".
- Avatar (editable), display name field, email (readonly).
- Save button anchored bottom.

**E3. Delete Account Confirmation**
- Full-screen modal.
- Serif headline: "Delete your account?"
- Sans paragraph: "This removes your profile, your devices' assignments, and all your plant history. It cannot be undone."
- Input: type DELETE to confirm.
- Two buttons: **Cancel** (text), **Delete permanently** (brown fill).

### F. Global / Shared

**F1. Offline banner** — top inset banner when app detects no network: brown, one-liner "You're offline — showing last known state."

**F2. Toast** — bottom pill, cream surface with dark text, auto-dismiss 3s.

**F3. Bottom sheet — Device selector** — reused by C3 and D2: list of user's devices, select one, primary button "Assign".

**F4. Bottom sheet — Plant selector** — reused by B3 plant row and others: search + grid.

**F5. Empty states** (reusable component): centered hand-drawn illustration + serif headline + sans subline + optional button.

**Total: 22 unique screens + 5 shared components.**

---

## Design System Principles (summary for the prompt)

These principles, from the `ui-ux-pro-max` and `frontend-design` skills, are embedded in the prompt below so Claude Design honors them from turn one.

| Principle | Applied |
|---|---|
| Accessibility ≥ 4.5:1 contrast | `#141413` text on `#faf9f5` cream passes 16.1:1. Green `#788c5d` accents must not be the sole signal — pair with labels. |
| Touch targets ≥ 44×44 | All buttons 56px tall, icon buttons 48×48 hit area. |
| 16px minimum body on mobile | Body 16pt, captions 13pt, display up to 72pt. |
| Line-height 1.5-1.75 | Body 1.55, display 1.1. |
| Line-length 65-75 ch | All paragraph containers capped. |
| Font pairing personality | Serif display (literary) + sans body (clean) — opposite enough to create contrast, harmonious enough to coexist. |
| 150-300ms micro-interactions | All transitions in this range, eased `cubic-bezier(0.2, 0.8, 0.2, 1)`. |
| Transform/opacity only | No layout-shifting hovers. |
| No emoji icons | Custom hand-drawn SVG set, not Heroicons/Lucide default. |
| Cursor pointer | N/A on mobile; equivalent: always-visible tap feedback ripple. |
| Light/dark contrast | Dark mode inverts cream → near-black, keeps green + brown accents saturated. |
| Z-index scale | sheet=50, modal=40, toast=30, banner=20, nav=10. |
| Reduced-motion | Breathing logo, loading leaves, NFC waves: all disable under `prefers-reduced-motion`. |
| Reserve space for async | Sparklines and charts have placeholder skeletons of exact final dimensions. |
| Commit to a bold direction | "Editorial literary warmth" — not brutalist, not glassmorphism, not neumorphism. Closer to a New Yorker column than a tech dashboard. |
| Distinctive typography | Fraunces (display serif, optical size axis) + Inter (body sans). Avoids Space Grotesk / Satoshi / all recent AI-app defaults. |
| Dominant colors + sharp accents | 80% cream + dark text, green + brown reserved for meaning (status, CTAs, links). |
| Atmospheric backgrounds | Cream surfaces with subtle paper-grain noise overlay (2% opacity); no solid flat fills. |

---

## THE PROMPT (paste this into Claude Design)

````
# Seqaya — mobile app design system and screens

I'm designing a native Android app called **Seqaya**. It's an NFC-powered plant-watering system — the physical device sits in the pot, measures moisture, and waters autonomously. The app is how the human checks in, identifies plants, and sets up new devices.

The product is calm and domestic, not industrial. Users are people with a few plants on their windowsill, not growers running greenhouses. The tone is editorial, unhurried, literary — closer to a New Yorker column about gardening than to a SaaS dashboard.

Please build a complete design system and prototypes for all 22 screens listed at the bottom.

---

## Aesthetic direction — commit fully

**Editorial literary warmth.** Serif display type, generous whitespace, warm cream surfaces, muted sage and terracotta accents. Think: a well-bound small-press book about houseplants. Confident, calm, not competing for attention. Nothing glassmorphism, nothing neon, no gradient mesh, no glossy tech-startup polish. If a design choice would look at home on a fintech landing page, reject it.

**Avoid AI-slop defaults.** Do not default to: Space Grotesk, Satoshi, Inter-everywhere, purple-on-white gradients, Heroicons-style generic line icons, dashboard cards with drop shadows, generic Lucide plant icons. Make one-of-a-kind choices that serve this specific product.

---

## Color tokens (strict — match these hex values exactly)

```
bg.cream          #faf9f5   /* primary surface */
bg.cream.light    #e8e6dc   /* secondary surface, cards-on-cream */
text.primary      #141413   /* body & headlines */
text.secondary    #75736b   /* meta, timestamps, captions */
text.tertiary     #b0aea5   /* deep mutes, fine print */
accent.green      #788c5d   /* "happy" status, success, positive CTAs when appropriate */
accent.brown      #d97757   /* primary CTA fill, "needs attention" status, destructive when appropriate */
accent.blue       #6a9bcc   /* informational only, used sparingly */
border            #e8e6dc   /* 1px hairlines on cards */
```

Dark mode:
```
bg.cream          #1a1917
bg.cream.light    #25231f
text.primary      #faf9f5
text.secondary    #b0aea5
accent.green      #9eb37f   /* lightened for contrast */
accent.brown      #e89878   /* lightened for contrast */
```

Dominant colors with sharp accents, not evenly distributed. Cream + dark text should cover ~80% of every screen; green and brown show up where meaning is carried.

---

## Typography

- **Display / headlines**: **Fraunces** (Google Fonts, variable, use optical-size axis — larger headlines get tighter optical size). Weight 500-600. Tracking -0.01em on display sizes.
- **Body / UI**: **Inter**, weight 400 body, 500 UI labels, 600 buttons. Tracking 0 for body, 0.02em for small caps labels.
- **Sizes** (mobile):
  - Display XL 48-72pt (hero moisture %, marquee headlines)
  - Display L 32pt (screen titles)
  - Heading 20pt
  - Body 16pt (line-height 1.55)
  - Label / caption 13pt
  - Tabular-nums on all numbers (moisture %, timestamps, percentages)
- Italics: always the matching italic cut of the same family, never pseudo-italic.

---

## Motion

- Standard easing: `cubic-bezier(0.2, 0.8, 0.2, 1)`.
- Durations: 180ms for micro (hover, toggle), 260ms for state, 420ms for screen transition.
- Motion is slow and eased. No bounces, no springs. Scale transforms limited to 0.98–1.02. Translates under 8px.
- Specific motion moments:
  - Splash: a leaf mark breathes (scale 1.0 → 1.04 → 1.0 over 2.2s, infinite, paused under `prefers-reduced-motion`).
  - NFC tap screen: three concentric waves emanate from the phone silhouette toward the device silhouette, staggered 300ms.
  - Success checkmark: stroke draws in over 400ms.
  - Loading (scan analyzing): three leaves orbit a center at 8s per revolution, each offset 120°.

All motion must respect `prefers-reduced-motion: reduce` — replace with static equivalents.

---

## Illustration style

A custom hand-drawn illustration set. Pencil-line weight, slightly irregular, not geometric-perfect. Off-black ink on cream, with occasional green or brown fills at 20% opacity. Subjects: houseplants (fiddle leaf fig, monstera, pothos, snake plant, basil, mint, rosemary, succulent, orchid), a stylized NFC phone-to-device scene, abstract leaf marks for empty states. Never photographic, never 3D-rendered. Think botanical field guide by way of small-press zine.

Icons follow the same hand: SVG, 24×24 base, 1.5px stroke, rounded caps. Not Heroicons, not Lucide. Custom set for: leaf, water-drop, sun, thermometer, wind, plant-pot, phone-tap, device, chart-line, search, settings, user, plus, close, back-arrow, eye, check, alert, trash.

---

## Surfaces and depth

- Primary surface: cream `#faf9f5` with a 2% opacity paper-grain noise overlay (subtle, adds warmth).
- Card surface: cream-light `#e8e6dc` on cream, 1px hairline border, 20px radius, no drop-shadow, no glass.
- Modal / sheet: solid cream with a subtle top hairline, slides up from bottom 420ms eased.
- No glassmorphism, no frosted blur, no neumorphism.

---

## Layout discipline

- 4-pt grid. All spacing in multiples of 4.
- Screen horizontal padding: 20px.
- Card internal padding: 24px.
- Safe areas respected top and bottom.
- Max content width on large phones: 480px, centered.
- Bottom nav: 3 tabs (Home, Scan, Library). Tab bar is cream with a top hairline, no shadow. Active tab shows a filled icon in dark; inactive is stroke-only muted.

---

## Accessibility (non-negotiable)

- All text ≥ 4.5:1 contrast against its background. Verify every token pair.
- Touch targets ≥ 44×44; primary buttons 56px tall.
- Icon-only buttons include visible 24×24 icon with an 48×48 hit area, plus an accessible label (screen reader).
- Color is never the sole signal — "needs attention" uses a brown dot AND a label.
- Focus states visible on all interactive elements (2px brown outline, 2px offset).
- Body text ≥ 16pt on mobile, line-height 1.55, line-length capped 65-75ch.
- Reduced motion honored everywhere.

---

## The 22 screens — build each

Every screen should match the aesthetic above and carry exactly the content listed. Build them as high-fidelity prototypes in the iPhone-ish portrait frame (or Android frame — doesn't matter, same proportions).

### A. Auth

**A1. Splash** — Seqaya wordmark center (Fraunces display), breathing leaf mark below, cream bg.

**A2. Sign In** — Top-left wordmark. Centered: headline "Care, quietly.", sub "Seqaya keeps your plants watered while you live your life.", primary button **Continue with Google** (brown fill, 56px). Fine print about Terms/Privacy. Footer italic: "Made for the windowsill, not the warehouse."

### B. Home

**B1. Home — empty** — Top bar (wordmark left, avatar right). Centered: headline "Your first plant is waiting.", paragraph, primary button **Add a device**, text link "Browse the plant library." Hand-drawn leaf bottom-right.

**B2. Home — populated** — Top bar (wordmark, plus icon, avatar). Scrollable list of Device Cards. Each card:
- Plant nickname (Fraunces 20pt) + status dot.
- Species (Inter italic, muted).
- Moisture % (Fraunces 48pt, tabular-nums).
- 24h sparkline, 60px tall, green stroke, no axes.
- "Last watered 3h ago" + "Last seen" row.
- 20px radius, 24px padding, 1px hairline, cream-light on cream.
If any device needs attention, show a brown banner above the list with "Review" link.

**B3. Device Detail** — Back arrow, nickname, overflow. Hero: species, moisture % (Fraunces 72pt), target %, state chip. Chart section with 24h/7d/30d segmented control, watering events as brown dots. Care block (3-5 tips). Device block (serial, plant, nickname, last seen, firmware, delete).

**B4. Wizard 1 of 3 — Pick a Plant** — Step header, headline "What are you growing?", search field, 2-col grid of plant tiles (illustration + common + scientific). Text link for custom plant. Next button disabled until selection.

**B5. Wizard 2 of 3 — Connect to Wi-Fi** — Step header, headline "Which Wi-Fi?", two-line sans explanation. Pre-filled SSID field (tap to change), password field with eye icon, helper "We never store your password." Expandable "Choose a different network". Next button.

**B6. Wizard 3 of 3 — Tap Your Phone** — Step header, headline "Tap your phone to the device.", centered illustration of phone + device with animating NFC waves. Cycling status text. On success: soft green check + auto-advance.

### C. Scan

**C1. Camera** — Full-bleed preview, overlaid cream-translucent top bar, square viewfinder with brown corner brackets, bottom controls (gallery / capture / flip).

**C2. Analyzing** — Frozen capture top, headline "Looking closely…", three leaves orbiting a center.

**C3. Result** — Captured image top, common name (Fraunces), scientific (Inter italic), confidence chip. Care summary (water / sun / temp icons + lines). Primary **Add to a device**, secondary "View in library", tertiary "Not right? Try another photo."

**C4. Error / No match** — Captured image, headline "I'm not sure what this is.", paragraph, buttons "Try again" + "Browse library".

### D. Library

**D1. Browse** — Wordmark + search icon top. Category chip row (horizontal scroll). Sections: "Popular this month", "Easy to start", "For low light", "Herbs". 2-col grid of plant tiles. "Suggest a plant" footer link.

**D2. Plant Detail** — Back + bookmark. Hero illustration. Common name, scientific, summary. Care grid (2×2: water, light, temp, humidity). Longer notes section. "Troubles" section with brown-accent issue icons. **Assign to a device** CTA.

**D3. Search** — Search field active, X to dismiss. Recent searches as muted text links. Live results list (tile + name/species).

### E. Settings + Profile

**E1. Settings** — Grouped sections: Account (name, email, Edit profile), Preferences (theme, units, notifications), About (version, privacy, terms, licenses), Danger zone (sign out, delete account).

**E2. Profile** — Avatar editable, display name field, email readonly, save anchored bottom.

**E3. Delete Account Confirmation** — Full-screen modal. Headline "Delete your account?", paragraph explaining irreversibility. Input: type DELETE to confirm. **Cancel** + **Delete permanently** (brown).

### F. Shared components (design once, reuse)

**F1. Offline banner** — top inset, brown, "You're offline — showing last known state."

**F2. Toast** — bottom pill, cream surface, dark text, auto-dismiss 3s.

**F3. Device selector sheet** — bottom sheet, list of devices, **Assign** button.

**F4. Plant selector sheet** — bottom sheet, search + grid.

**F5. Empty state component** — centered illustration + headline + subline + optional button.

---

## Deliverables

1. **Design tokens** exported as JSON and CSS variables.
2. **Component library**: buttons (primary, secondary, text, destructive), inputs, cards, chips, status dots, sparkline placeholder, segmented control, bottom nav, top bar variants, modal, sheet, toast.
3. **All 22 screens** as high-fidelity prototypes, light mode.
4. **Dark mode variants** for the 5 most common screens: B2, B3, C1, D1, E1.
5. **Package for Claude Code** — so I can take the export into my Android repo and implement as Jetpack Compose.

---

## Anti-patterns — if you catch yourself doing these, back out

- Adding a drop shadow to a card "for depth".
- Reaching for Space Grotesk, Satoshi, or a mono display font.
- Purple or blue primary accent.
- Glassmorphism, frosted blur, neumorphism.
- Emoji as icons.
- Generic Material Design patterns that erase Seqaya's voice.
- Centered hero with a SaaS gradient mesh background.
- A "dashboard" feel — rows of stat cards with icons in colored circles.

Stay in the editorial literary register. When in doubt, take something away, not add.
````

---

## What to Do With Claude Design's Output

Once Claude Design finishes generating screens and tokens:

1. **Export tokens** as a JSON file → commit to `C:\Dev\Seqaya\Android_app\docs\design-tokens.json`.
2. **Export the screens** as the Claude Code package. Save under `C:\Dev\Seqaya\Android_app\docs\design-system\`.
3. **Open a new Claude Code session in the app repo.** The session's first task is to translate tokens into `ui/theme/Color.kt`, `Typography.kt`, `Shape.kt`, then implement screens in order A → B → C → D → E, using the design exports as ground truth.
4. **Iterate in Claude Design, not in Compose.** If a screen feels wrong, go back to Claude Design, refine, re-export. Do not hand-tune Compose UI against design intent — the design file is the source of truth.

---

## Verification

The Claude Design output is "good enough to build" when:

1. All 22 screens exist as prototypes, not placeholders.
2. Every token in the palette table is present and named per the spec.
3. Tapping through the auth → home → add-device flow feels like one product, not 22 separate mocks.
4. A plant-loving friend shown the prototype describes it unprompted as "calm" / "warm" / "literary" — not "clean" or "techy".
5. Contrast-check passes for every text token against every surface token (4.5:1 minimum).
6. Dark mode variants exist for B2, B3, C1, D1, E1 and maintain contrast.
7. The export packages cleanly for Claude Code (no broken references).
8. `ui-ux-pro-max` pre-delivery checklist passes (touch targets, cursor/tap feedback equivalent, focus states, reduced-motion, alt text, form labels).

---

## Open items (defer, do not block the prompt)

- **Fraunces vs. Tiempos** — start with Fraunces (free, Google Fonts). If Seqaya ever has budget, swap for Tiempos before Play Store launch.
- **Illustration artist** — the prompt tells Claude Design to generate hand-drawn illustrations. If the output illustrations feel off, commission a real illustrator in Phase 7 before Play launch.
- **Localization** — English only at launch. Arabic is a likely near-term addition (user is in Israel). Leave room in the layout for 1.3× text length.
- **Dark mode priority** — user hasn't specified. Plan assumes "system default, honor both." Claude Design covers both from Phase 1.
