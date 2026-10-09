# Architectural & Root Cause Decisions

## 1. Root Cause Analysis of Earlier Failures
- **L1 Medication Time Bug Root Cause:** 
  `HealthViewModel.addMedication` hardcoded `times = listOf("08:00")` during the object creation mapping step. Even though `AddMedicationScreen` allowed picking custom times, theViewModel discarded the picked time list and passed `listOf("08:00")` to `MedicationRepository`. 
  *Fix:* `AddMedicationScreen` now produces a `Medication` object with the user-selected times, frequency, dosage, and start date, which `HealthViewModel` passes verbatim to the repository without overwriting fields.
  *Seed Data Removal:* Guaranteed that no hardcoded sample medications (Amoxicillin / Atorvastatin) exist in repository defaults or UI initial states.

- **L2 Dose Status vs. Time Calculation Root Cause:** 
  `MedsScreen` classified any medication with `!isTaken` into an "Upcoming Doses" list, disregarding the current clock time.
  *Fix:* Implemented dynamic time-window calculations based on the user's configurable `onTimeWindowMinutes` and `missedWindowMinutes`. Status is derived dynamically: `PENDING` (future), `DUE` (within on-time window), `MISSED` (past missed threshold), `TAKEN`, `SKIPPED`. The screen is reorganized into: *Due now*, *Upcoming*, *Missed today*, and *Taken/Skipped today*.

- **U1 & U2 Card Inner Rectangle Mismatch & Text Truncation Root Cause:** 
  `HealthNexaCard` wrapped children in elevated/colored `Card` views while inner child `Column`s or `Surface`s applied their own `background(...)` without clipping or with mismatched container colors.
  *Fix:* Unified `HealthNexaCard` to use a single background container, clipped with `RoundedCornerShape`, and removed all redundant inner background modifier layers. Changed fixed heights on header cards to `wrapContentHeight()` with proper `minHeight` constraints.

- **U3 Dose Action Buttons Root Cause:** 
  `HealthNexaButton` was being rendered with a full-width container background inside small card action rows, creating solid dark green blocks.
  *Fix:* Replaced with distinct, accessible action buttons ("Mark Taken", "Skip", "Snooze", "Edit", "Pause/Resume", "Delete") with WCAG compliant contrast (> 4.5:1) and minimum 48dp touch targets.

- **U4 Bottom Navigation Label Wrapping Root Cause:** 
  Bottom navigation labels used `MainTab.title` ("Medications"), which exceeded the width of the selected indicator pill on 360dp devices, causing "Medicatio / ns" line wrapping.
  *Fix:* Renamed title/label to "Meds", set `maxLines = 1`, `softWrap = false`, and adjusted indicator pill padding to maintain equal dimensions across all 5 navigation tabs. Tested at 1.3x font scale and 360dp width.

- **L3 Data-Driven Ingredient Catalogue Architecture:**
  Created `assets/ingredients.json` to store canonical nutrients, display names, aliases, unit families, and conversion factors. Implemented a fuzzy-matching search pipeline and autocomplete dropdown in `EvaluateScreen`. Generated quick-select chips dynamically from active rules.

- **U7 Privacy & Compliance Notice Correction:**
  Replaced false HIPAA compliance claims with accurate disclaimer text: "Your data is stored on this device and synced to your account. HEALTHNEXA is a decision-support tool, not medical advice."
