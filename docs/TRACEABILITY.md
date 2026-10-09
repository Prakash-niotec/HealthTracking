# Requirements Traceability Matrix

| Requirement ID | Requirement Description | UI Control / TestTag | ViewModel Event | Repository Method | Test Name | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **G1** | Single source of truth across all screens | Navigation tabs (`testTag("tab_*")`) | `selectMainTab` | `currentUser`, `getLogsForDate`, `getMedications` | `testCrossScreenReactivity` | VERIFIED |
| **G2** | Editable, persisted, validated settings | `testTag("btn_reminder_settings")` | `updateSettings` | `SettingsRepository.updateSettings` | `testSettingsPersistenceAndReschedule` | VERIFIED |
| **G3** | Backward compatibility / default values | App Startup | Flow emissions | `Local*Repository.init` | `testLegacyDataMigration` | VERIFIED |
| **G4** | Immutable history (snapshots) | `testTag("med_item_*")` | `addMedication`, `markDose` | `MedicationRepository.markDose` | `testMedicationHistoryImmutability` | VERIFIED |
| **G5** | Constants in single file | N/A | N/A | N/A | `ValidatorsTest` | VERIFIED |
| **W1** | Auto/manual water goal (500-10000ml) | `testTag("input_water_goal")` | `updateProfile` | `AuthRepository.updateProfile` | `testWaterGoalValidation` | VERIFIED |
| **W2** | Per-day water goal snapshot | `testTag("water_progress_card")` | `updateProfile` | `WaterRepository.setGoalForDate` | `testDailyWaterGoalSnapshot` | VERIFIED |
| **W3** | Drink types & hydration factors | `testTag("drink_type_chip_*")` | `addHydrationLog` | `WaterRepository.addLog` | `testDrinkTypesAndFactors` | VERIFIED |
| **W4** | Full CRUD on today's water entries | `testTag("btn_edit_water_*")`, `testTag("btn_delete_water_*")` | `editHydrationLog`, `deleteHydrationLog` | `WaterRepository.updateLog`, `deleteLog` | `testWaterLogCRUD` | VERIFIED |
| **W5** | Water backdating (reject future) | `testTag("input_water_time")` | `addHydrationLog` | `WaterRepository.addLog` | `testWaterBackdating` | VERIFIED |
| **W6** | Water reminder settings (15-480min, window crossing midnight) | `testTag("dialog_reminder_settings")` | `updateSettings` | `SettingsRepository.updateSettings` | `testReminderSettings` | VERIFIED |
| **W7** | Smart water scheduling (last drink + interval) | `testTag("text_next_reminder")` | Flow observation | `AlarmSchedulerImpl.scheduleWaterAlarms` | `WaterReminderCalculatorTest` | VERIFIED |
| **W8** | Notification "Add 250 ml" action | Notification Action | `WaterReceiver` broadcast | `WaterRepository.addLog` | `WaterReceiverTest` | VERIFIED |
| **M1** | Medication editable without corrupting past logs | `testTag("btn_edit_med_*")` | `updateMedication` | `MedicationRepository.updateMedication` | `testMedicationEditingPreservesHistory` | VERIFIED |
| **M2** | Pause / resume medication | `testTag("switch_pause_med_*")` | `updateMedication` | `MedicationRepository.updateMedication` | `testMedicationPauseResume` | VERIFIED |
| **M3** | Configurable snooze, missed & on-time windows | `testTag("input_snooze_duration")` | `updateSettings` | `SettingsRepository.updateSettings` | `testConfigurableMedicationWindows` | VERIFIED |
| **M4** | Mark taken/skipped from app or notification | `testTag("btn_take_dose_*")`, `testTag("btn_skip_dose_*")` | `toggleMedicationTaken` | `MedicationRepository.markDose` | `testMarkDoseFromAppAndNotification` | VERIFIED |
| **M5** | Log past dose (mark missed as taken late) | `testTag("btn_take_late_*")` | `toggleMedicationTaken` | `MedicationRepository.markDose` | `testLogPastMissedDose` | VERIFIED |
| **M6** | Empty state "No doses today" | `testTag("card_empty_meds")` | N/A | N/A | `testMedicationEmptyState` | VERIFIED |
| **L1** | Exact chosen medication times saved & live schedule preview | `testTag("time_picker_med")` | `addMedication` | `MedicationRepository.addMedication` | `testExactMedicationTimesSaved` | VERIFIED |
| **L2** | Clock-based dose status categorization | `testTag("section_due_now")`, `testTag("section_upcoming")` | Flow observation | `MedicationRepository.getDoseLogsForDate` | `testClockBasedDoseStatus` | VERIFIED |
| **L3** | Ingredient catalogue, synonyms, autocomplete & unit dropdown | `testTag("autocomplete_ingredient")` | `evaluateIngredient` | `HealthRuleEngine.evaluate` | `testIngredientCatalogueAndSynonyms` | VERIFIED |
| **L4** | Reminder settings screen & "Next reminder" text | `testTag("btn_hydration_bell")` | `updateSettings` | `SettingsRepository.updateSettings` | `testReminderSettingsScreen` | VERIFIED |
| **L5** | Drink type selector & effective ml calculation | `testTag("drink_selector")` | `addHydrationLog` | `WaterRepository.addLog` | `testDrinkTypeSelectionAndFactors` | VERIFIED |
| **L6** | Editable profile health conditions & "Check against all conditions" | `testTag("chip_condition_*")` | `updateProfile` | `AuthRepository.updateProfile` | `testProfileHealthConditions` | VERIFIED |
| **L7** | Home dashboard completeness & tappable medication rows | `testTag("home_med_row_*")` | `toggleMedicationTaken` | `MedicationRepository.markDose` | `testHomeDashboardCompleteness` | VERIFIED |
| **L8** | Title-case display names | `testTag("text_user_name")` | N/A | N/A | `testTitleCaseDisplayName` | VERIFIED |
| **U1-U12** | Complete UI/UX, contrast, clipping, and accessibility fixes | All screens | N/A | N/A | `UiAccessibilityAndContrastTest` | VERIFIED |
