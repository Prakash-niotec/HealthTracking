# HealthTrack Architecture

## Overview
HealthTrack is built using a modern Android architecture based on **Unidirectional Data Flow (UDF)** and **Clean Architecture** principles. The app heavily relies on Jetpack Compose for the UI layer and Kotlin Coroutines/StateFlow for reactive state management.

## Project Structure
- **`com.healthtrack.app`**: Root package
  - **`ui`**: Presentation layer.
    - **`navigation`**: Compose `NavHost`, Screen definitions (`NavRoutes`), and main navigation wiring.
    - **`screens`**: Composable screens organized by feature (`auth`, `main`).
    - **`theme`**: Color palettes, Typography, and customized standard components (e.g. `HealthNexaCard`).
    - **`model`**: UI-specific models mapped from the data layer for rendering.
    - **`viewmodel`**: `HealthViewModel` and `ViewModelFactory`. Exposes `StateFlow` to the UI and delegates commands to the data/domain layer.
  - **`data`**: Data layer managing both local cache and remote sources.
    - **`model`**: Pure data classes defining the core business entities (`User`, `Medication`, `WaterLog`, `DoseLog`, `Evaluation`).
    - **`repository`**: Interface contracts defining data operations.
    - **`local`**: Implementations using `SharedPreferences` (wrapped in `LocalCache`) and Gson for local, synchronous storage.
    - **`remote`**: Implementations using Firebase Authentication and Cloud Firestore. Uses a **write-through** strategy: writing to local cache immediately, then to Firestore, while listening to Firestore snapshots to keep the local cache synchronized.
    - **`fake`**: In-memory fake implementations for fast, deterministic unit testing.
  - **`domain.engine`**: Pure Kotlin business logic.
    - **`HealthRuleEngine`**: Evaluates ingredient safety against predefined `StarterRules` based on user health conditions.
    - **`WaterCalculator`**: Computes daily water targets based on user weight.
    - **`DoseScheduler`**: Generates scheduled doses from medication configurations.
    - **`AdherenceCalculator`**: Computes adherence statistics and percentages.
    - **`StreakCalculator`**: Calculates daily consecutive streaks combining water targets and medication adherence.
  - **`notifications`**: 
    - **`AlarmSchedulerImpl`**: Wraps Android's `AlarmManager` for exact scheduling.
    - **`NotificationHelper`**: Sets up Notification Channels.
    - **`Receivers`**: `BootReceiver`, `WaterReceiver`, `MedicationReceiver`, and `MedicationActionReceiver`.
  - **`di`**:
    - **`AppContainer`**: A manual dependency injection container holding singleton references to repositories and configuration toggles.
  - **`util`**:
    - Result wrappers, TimeProviders, and Validators.

## Data Flow (Write-Through Hybrid Strategy)
1. **Action**: User performs an action on the UI (e.g., toggles a medication dose).
2. **ViewModel**: `HealthViewModel` intercepts the intent, maps UI models to domain models if necessary, and calls the appropriate repository function.
3. **Repository (Local/Remote)**: 
   - The repository instantly writes the change to `LocalCache` to guarantee zero latency and offline persistence.
   - It then attempts an async write to Cloud Firestore.
4. **Synchronization**: A continuous Firestore `SnapshotListener` observes the remote collection. When remote data changes (via sync or another device), it updates `LocalCache` and pushes the new list to the `StateFlow`.
5. **UI Update**: Jetpack Compose re-composes automatically reacting to the updated `StateFlow`.

## Rules and Constraints
- The **Domain layer (`domain.engine`) must remain pure Kotlin** and have zero dependencies on Android frameworks (`Context`, `SharedPreferences`, `Firebase`).
- **Data Repositories** must always return a generic `Result<T>` wrapper, guaranteeing no raw exceptions crash the UI.
- The `AppContainer` uses a boolean flag (`useFirebase`) to seamlessly switch between Local-only and Firebase-backed repositories.
