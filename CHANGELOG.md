# Changelog

## [1.0.0] - Phase 4 (Final Integration)

### Added
- **Project Foundation:** Initialized robust architecture with `AppContainer`, manual DI, and UDF (Unidirectional Data Flow) principles.
- **Frontend Integration:** Successfully integrated `HealthNexaTheme` Compose UI sourced from external GitHub repository. Safely decoupled `ui.model` components from pure data models.
- **Health Rule Engine:** Incorporated rules parsing logic mapped from `Nutri_V4`, localized to `HealthRuleEngine` and `StarterRules`. Supports unit conversion (g/mg/mcg) and alias resolution for dynamic ingredient safety evaluation.
- **Core Domain Logic:**
  - `WaterCalculator`: Computes dynamic hydration goals based on user weight.
  - `DoseScheduler`: Generates active schedules handling `DAILY`, `SPECIFIC_DAYS`, and `CUSTOM_INTERVAL`.
  - `AdherenceCalculator`: Computes compliance statistics without divide-by-zero risks.
  - `StreakCalculator`: Evaluates chronological adherence combining daily water targets and taken medications.
- **Local Persistence:** Wrapped `SharedPreferences` in `LocalCache`. Implemented `Gson` serialization for offline storage (`LocalAuthRepository`, `LocalWaterRepository`, etc.).
- **Firebase Integration:** 
  - Authenticated via `FirebaseAuth`.
  - Synced data via `FirebaseFirestore` using a custom "Write-through" hybrid strategy. Writes hit `LocalCache` synchronously for 0ms UI latency, then dispatch asynchronously to Firestore.
  - Setup snapshot listeners for bi-directional state synchronization.
  - Deployed rigorous `firestore.rules` isolating document access to `request.auth.uid == userId`.
- **Notifications:** Configured exact-alarms via `AlarmManager` and `AlarmSchedulerImpl`. Added `MedicationReceiver`, `MedicationActionReceiver` (for Taken/Skipped actions), `WaterReceiver` (respecting quiet hours), and `BootReceiver` for persistence across device restarts.
- **Tests:** Added 27 automated JVM tests running on Robolectric + JUnit4 covering view rendering, boundary conditions, edge cases, date math, unit conversion, and alarm suppression. Pass rate is 100%.

### Fixed
- Fixed Robolectric Firebase initialization error by gracefully bootstrapping `FirebaseApp.initializeApp(app)` inside test harnesses.
- Corrected numerous conflicting imports between the native data models and the externally sourced UI models.
- Removed deprecated UI serialization tags conflicting with domain-level data mapping.
