# Gap Report & Audit

Based on the required "Flexibility and Logic Fix" guidelines, here are the identified gaps between the current implementation and the requested behavior.

## Global
- **G1 (Single Source of Truth):** Most screens read from `HealthViewModel` StateFlows correctly, but the dashboard UI doesn't fully react to underlying edits yet (needs ViewModel to react dynamically to changes).
- **G2 (Editable Settings):** Need to ensure every setting (including intervals, windows) triggers `AlarmScheduler.cancelAllAlarms()` and reschedules.
- **G3 (Backward Compatibility):** New fields added to `WaterLog`, `Medication`, and `NotificationSettings` need fallback defaults for Gson parsing so existing records don't crash.
- **G4 (Immutable History):** Edits to Medication dosage/name must not overwrite past `DoseLog` entries (already mostly handled, as `DoseLog` takes a snapshot of `medName`, but needs confirmation for dosage). Daily water goal needs a per-day snapshot.

## Water
- **W1 (Goal Rules):** `WaterCalculator` calculates auto goals but does not enforce the strict 500-10000ml validation bounds.
- **W2 (Goal Snapshot):** The app currently uses the live `userProfile.dailyWaterTarget` for history and streaks. A mechanism to store the per-day goal snapshot (e.g., `DailyWaterGoal` entity or embedding it in the day's record) is missing.
- **W3 (Drink Types & Factors):** `WaterLog` currently only stores `amountMl`. Missing `drinkType` and `effectiveMl`. `NotificationSettings` is missing a map of hydration factors.
- **W4 (CRUD on logs):** `WaterRepository` only has `undoLastLog`. Missing `deleteLog(id)` and `updateLog(log)`.
- **W5 (Backdating):** `addHydrationLog` hardcodes `System.currentTimeMillis()`. It needs to accept a custom past timestamp and reject future timestamps.
- **W6 (Reminder Settings):** Interval is currently fixed to specific presets in the UI/model. Needs to support 15-480 validation. Active window logic needs to correctly handle midnight crossing (e.g. 22:00 to 06:00).
- **W7 (Smart Scheduling):** `AlarmSchedulerImpl.scheduleWaterAlarms` currently just adds `intervalMs` from `now`. It must find the *last drink time*, add the interval, constrain it inside the active window, and suppress it if the daily target is met.
- **W8 (Notification Action):** `WaterReceiver` is missing the "Add 250 ml" PendingIntent action.

## Medication
- **M1 (Editing):** Needs full edit capability without altering past logs.
- **M2 (Pause/Resume):** `Medication` has `isActive`, but we need to ensure the UI can toggle it and that toggling it triggers an alarm reschedule.
- **M3 (Configurable Windows):** `AdherenceCalculator` hardcodes `ON_TIME_WINDOW_MINUTES` and `MISSED_THRESHOLD_MINUTES`. Snooze duration is missing entirely. These must become user settings in `NotificationSettings`.
- **M4 & M5 (Status updates):** User needs the ability to mark past missed doses as taken.
- **M6 (Empty states):** Must show "No doses today".

## Stats & Dashboard
- **S1 & S2 (Reactivity & Snapshot Streaks):** `StreakCalculator` must be updated to consume the daily goal snapshots rather than the current live goal.
