# Manual Test Checklist

Because Android enforces strict background execution limits, Doze mode, and exact alarm permission flows, certain notification and scheduling behaviors cannot be fully verified through JVM/Robolectric unit tests.

The following steps **must be verified manually** on a physical Android device.

## 1. Alarm Permission Flow (Android 12+)
- [ ] Go to device settings and revoke "Alarms & Reminders" permission for HealthTrack.
- [ ] Open the app. Verify that attempting to schedule a medication handles the lack of `SCHEDULE_EXACT_ALARM` gracefully (either falls back to inexact alarms or prompts the user via a settings intent).

## 2. Notification Permissions (Android 13+)
- [ ] Install the app on an Android 13+ device.
- [ ] Verify that upon launching or triggering the first notification, the system prompts for the `POST_NOTIFICATIONS` permission.
- [ ] Deny the permission, verify the app doesn't crash.
- [ ] Grant the permission, verify notifications appear.

## 3. Medication Reminders Timing & Doze Mode
- [ ] Add a new medication scheduled 3 minutes from the current time.
- [ ] Close the app completely (swipe away from recents).
- [ ] Turn off the device screen to encourage Doze/App Standby.
- [ ] Verify the "Time for your medication" high-priority notification fires exactly at the scheduled time.

## 4. Medication Notification Actions (Closed App)
- [ ] Trigger a medication notification.
- [ ] With the app closed, tap the "Taken" action button directly on the notification.
- [ ] Verify the notification dismisses.
- [ ] Open the app and navigate to the Medications tab. Verify the dose was correctly marked as "Taken" in the UI and the Adherence Streak was updated.
- [ ] (NEW M3) Tap the "Snooze" action button. Verify it re-triggers the notification based on the custom Snooze user setting.

## 5. Water Reminder Interval Window
- [ ] Go to Settings and set the Water Reminder interval to 30 minutes, starting at the current hour.
- [ ] Wait 30 minutes. Verify the "Stay Hydrated" notification fires.
- [ ] Change the Active Window so that the current time is *outside* the window (e.g., set window end to 1 hour ago).
- [ ] Wait for the interval. Verify the notification is suppressed (does not fire).
- [ ] (NEW W8) Verify the water notification contains a functional "Add 250 ml" quick-action button.

## 6. Device Reboot (BOOT_COMPLETED)
- [ ] Ensure medications and water intervals are scheduled.
- [ ] Restart the physical device.
- [ ] Without opening the app, wait for the next scheduled medication time.
- [ ] Verify the notification fires, proving the `BootReceiver` successfully regenerated the `AlarmManager` intents.

## 7. Firebase Offline Capabilities
- [ ] Turn on Airplane Mode (disable Wi-Fi and Cellular).
- [ ] Open the app, add a new water log, and add a new medication.
- [ ] Verify the UI updates instantly via the `LocalCache` write-through.
- [ ] Close and reopen the app while still in Airplane mode. Verify the data persists.
- [ ] Turn off Airplane Mode.
- [ ] Wait 10 seconds, then check the Firebase Firestore console on a computer. Verify the queued offline writes successfully synced to the cloud.

## 8. Dark Mode
- [ ] Toggle the system-wide Dark Mode on the device.
- [ ] Navigate through all tabs (Home, Hydration, Medications, Evaluate, Profile).
- [ ] Verify all colors, cards, text, and icons are legible and respect the `HealthNexaTheme` dark palette definitions.
