# Progress

## Phase 0: Project setup - **COMPLETE**
## Phase 1: Frontend - **COMPLETE**
## Phase 2: Domain Logic and Local Data - **COMPLETE**

## Phase 3: Firebase Integration - **COMPLETE**
- Verified `google-services.json` presence.
- Upgraded Gradle dependencies `libs.versions.toml` to include `google-services`, `firebase-bom`, `firebase-auth`, `firebase-firestore`, and `kotlinx-coroutines-play-services`.
- Applied `com.google.gms.google-services` plugin.
- Added `FirestoreAuthRepository`, `FirestoreWaterRepository`, `FirestoreMedicationRepository`, `FirestoreEvaluationRepository`, and `FirestoreSettingsRepository`.
- **Hybrid Write-through Storage Strategy**:
  - Combined `LocalCache` (SharedPreferences + Gson) for instant synchronous writes and immediate offline reads with Firebase Firestore `SnapshotListeners`.
  - Listeners update `LocalCache` upon successful cloud syncs, avoiding delays in the UI.
- Switched the `AppContainer` to use the Firebase repositories (`useFirebase = true`).
- Resolved UI Test failures in `SmokeComposeTest` by bootstrapping `FirebaseApp.initializeApp(app)` in the Robolectric context.
- Implemented `firestore.rules` applying standard user-isolated permissions `request.auth.uid == userId`.
- **Testing**: All JVM Tests successfully ran and passed, verifying no regressions in business rules or local DI boundaries. 

## Next Step
- **Phase 4**: Integration and JVM-side final QA.
  - End-to-end flow tests with fake/local repositories.
  - Final full QA loop, code review, leak/cancellation analysis.
  - Build universal APK.
  - Write up standard `/docs` files (`ARCHITECTURE.md`, `README.md`, `CHANGELOG.md`, `MANUAL_TEST_CHECKLIST.md`).
