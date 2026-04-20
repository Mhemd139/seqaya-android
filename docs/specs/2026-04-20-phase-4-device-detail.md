# Phase 4 — Device Detail (B·03) + Moisture Chart

**Date:** 2026-04-20
**Branch:** `chore/phase-4-device-detail`
**Design source:** `docs/design-system/source/seqaya-screens-home.jsx` → `B3_DeviceDetail`

## Scope (final — approved by Mo)

Tap a device card on Home → device detail screen. It shows:

1. **TopBar** — back arrow, nickname (serif center). No overflow (…) yet; appears in Phase 5 with Locate/Hold/Re-provision NFC actions.
2. **Hero** — scientific name (italic muted), moisture % (Fraunces hero), state chip (Thriving/Thirsty), target text.
3. **Range toggle** — 24h / 7d / **30d (default)**.
4. **Moisture chart** — line + watering-event dots.
5. **Tap-to-inspect** on the chart — tap/long-press shows value + timestamp overlay.
6. **Empty state** — "Waiting for the first reading…" when no data.
7. **Device info block** — serial (read-only), plant (read-only until Phase 5 wires plants), **nickname (inline tap-to-edit)**, **target % (inline tap-to-edit)**, last seen.
8. **Delete device** — bottom of screen, terracotta, confirmation dialog.

**Realtime:** new firmware readings update the chart live (already subscribed at app level via `ReadingRepository.subscribe`).

## Deferred to later phases (explicit — not lost)

| Feature | Phase | Why |
|---|---|---|
| Overflow (…) menu | 5 | Contents are all NFC: Locate, Hold, Re-provision. Not built yet. |
| Care tips | 5 | Sourced from `plants` table once Plant Library is wired. No static fallback. |
| Edit plant (reassign) | 5 | Requires Plant Library picker. |
| Per-device settings screen | — | Rejected. Inline edits on Device Detail instead. |

## Data flow

| Need | Source |
|---|---|
| Moisture history per range | `ReadingRepository.observeRecent(serial, sinceEpochMs)` — already exists |
| Watering events | Pure function on readings: `false → true` transition on `isValveOpen`. Unit-tested. |
| Tap-to-inspect value/time | Vico `MarkerComponent` + touch gesture |
| Nickname / target edits | `DeviceRepository.updateNickname(id, nickname)` + `updateTarget(id, percent)` → Supabase update, cache re-sync |
| Delete | `DeviceRepository.delete(id)` → Supabase delete, FK cascade drops readings, Room row removed, nav pops |

## Architecture

```
app/src/main/java/com/seqaya/app/ui/device/
├── DeviceDetailScreen.kt         Compose UI
├── DeviceDetailViewModel.kt      state, range selection, edit + delete actions
├── MoistureChart.kt              Vico wrapper (line + event dots + marker)
├── RangeSegmentedControl.kt      24h / 7d / 30d pill toggle
├── EditNicknameDialog.kt         single-line text field + save/cancel
└── EditTargetDialog.kt           numeric stepper (10–90% range, 1% increments)

app/src/main/java/com/seqaya/app/domain/
└── WateringEvents.kt             pure: List<Reading> -> List<Instant>

app/src/test/java/com/seqaya/app/domain/
└── WateringEventsTest.kt

app/src/test/java/com/seqaya/app/ui/device/
└── DeviceDetailViewModelTest.kt
```

**Modifications:**
- `DeviceRepository` — add `delete(id)`, `updateNickname(id, nickname)`, `updateTarget(id, percent)`. All `suspend fun`, return `Result<Unit>`.
- `DeviceDao` — add `suspend fun deleteById(id: String)`, `suspend fun updateNickname(id: String, nickname: String?)`, `suspend fun updateTarget(id: String, target: Int?)`.
- `SeqayaRoot.kt` — new nav route `device/{serial}`.
- `DeviceCard` — `onClick` prop (was static before).
- `HomeScreen` — wire nav lambda to DeviceCards.

## Navigation

```
Home.DeviceCard.onClick(serial) → navController.navigate("device/$serial")
Detail.backArrow → popBackStack()
Detail.delete (confirmed) → Supabase delete → popBackStack() → toast "Removed"
```

## Target editor behavior

- Stepper dialog (not slider — slider is imprecise for whole percents).
- Range 10–90%, 1% increments. Current value pre-filled.
- Buttons: Cancel (secondary) + Save (primary sage).

## Delete confirmation dialog

- Title: "Remove [nickname]?"
- Body: "The device will be unpaired and its 30-day history deleted. This can't be undone."
- Buttons: Cancel (secondary) + Remove (terracotta destructive).

## Test plan (TDD order)

1. **`WateringEventsTest`** — false→true = event; starts-with-true = no event; consecutive trues = one event at first; empty list = no events.
2. **`DeviceDetailViewModelTest`** — range change re-queries window; delete flow sets `navigateBack=true`; nickname update triggers Supabase call; target update clamps to 10–90.
3. Manual on device: navigate from Home, see 30d chart, tap a point → overlay, switch ranges, rename → see it in Home top bar, change target → see state chip update, delete → confirm → back on Home with card gone.

## Risks / known limits

- **30d = up to 1440 points** (48/day × 30). Vico handles it; we cap `observeRecent` to safe bound anyway.
- **No plant_id wiring yet** — "Plant" row shows scientific name from `plants` join once Phase 5 wires it. For Phase 4: show "—" or hide row if null.
- **Nickname edit during offline** — writes fail, surface error via existing `HomeUiState.error` pattern. Optimistic cache update rolled back on failure.

## Acceptance

- [ ] Tap any device card → land on Device Detail with correct data
- [ ] 30d chart renders by default, switches cleanly to 24h and 7d
- [ ] Watering events render as brown dots at correct x-positions
- [ ] Tap-to-inspect: tapping a point shows value + timestamp overlay
- [ ] Realtime: firmware posts new reading → chart updates within ~2s without restart
- [ ] Empty state shows when no readings in window
- [ ] Tap nickname row → edit dialog → save → reflects in top bar + Home
- [ ] Tap target row → stepper → save → state chip re-evaluates
- [ ] Tap Delete → confirmation → confirm → Supabase delete + nav back + toast
- [ ] Back arrow pops to Home
- [ ] All VMs + pure fns unit-tested (TDD)
- [ ] CI green, CodeRabbit clean

## Implementation order (TDD + subagent-driven)

1. Branch `chore/phase-4-device-detail` off `main`.
2. **TDD:** `WateringEvents` pure fn (test → impl).
3. **TDD:** `DeviceDetailViewModel` (test → impl).
4. `DeviceDao` + `DeviceRepository` edit/delete methods.
5. `RangeSegmentedControl` (stateless Compose).
6. `MoistureChart` (Vico wrapper with marker).
7. `EditNicknameDialog` + `EditTargetDialog`.
8. `DeviceDetailScreen` composing the above.
9. Navigation wiring in `SeqayaRoot` + `DeviceCard.onClick`.
10. Manual test on device.
11. Commit, push, PR.
