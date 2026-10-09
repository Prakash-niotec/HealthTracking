# HealthTrack

HealthTrack is an intelligent, daily health companion app built for Android using Jetpack Compose, Kotlin Coroutines, and Firebase. It tracks hydration, manages complex medication schedules, and evaluates ingredient safety based on user-specific health conditions.

## Features
- **Hydration Tracking:** Calculates dynamic water goals based on weight.
- **Medication Management:** Schedule doses by specific days, intervals, or daily frequencies.
- **Ingredient Evaluator:** A smart rule engine that evaluates sodium, sugar, and fat against personalized health conditions (e.g., Hypertension, Diabetes).
- **Notifications:** Robust local exact-alarms with actionable notifications (mark as taken/skipped) that persist through device reboots.
- **Offline-First Firebase:** Uses a hybrid write-through cache strategy, guaranteeing zero-latency UI updates while seamlessly syncing to Cloud Firestore in the background.

## Setup Instructions

### 1. Requirements
- JDK 17
- Android Studio
- Android SDK (minSdk 26, targetSdk 34)

### 2. Firebase Configuration
To build the app, you must connect it to a Firebase project:
1. Go to the [Firebase Console](https://console.firebase.google.com).
2. Create a new Android app with the package name `com.healthtrack.app`.
3. Enable **Authentication** (Email/Password).
4. Enable **Firestore Database** (Test Mode or Production).
5. Download the `google-services.json` file.
6. Place the `google-services.json` file inside the `app/` directory of this project.
7. Copy the contents of `firestore.rules` into your Firebase Firestore Rules console to secure user data.

### 3. Running the App
Run the following Gradle command to compile and install the debug APK onto a connected device or emulator:
```bash
./gradlew installDebug
```

### 4. Running Tests
The project relies on JVM-based Robolectric tests for maximum speed and CI compatibility. No physical emulator is required to run the test suite.
```bash
./gradlew testDebugUnitTest
```

## Architecture
See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for a detailed breakdown of the Clean Architecture, Unidirectional Data Flow, and Local/Remote data synchronization strategies.
