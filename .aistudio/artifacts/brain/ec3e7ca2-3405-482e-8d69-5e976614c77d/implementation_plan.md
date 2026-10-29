# Implementation Plan: In-App OTA Update Installer & Auto-Relaunch Recovery System

Implement an in-app Over-The-Air (OTA) update installation engine and recovery console for ATHENA that bypasses USB installation blocks, allows one-tap acceptance of update packages, and cleanly auto-relaunches ATHENA with state recovery.

---

## User Review Required

> [!IMPORTANT]
> **Android Permissions**: Android 8.0+ enforces `REQUEST_INSTALL_PACKAGES` for in-app APK installations. If not previously allowed on your device, ATHENA will present a one-tap shortcut to your device's "Install Unknown Apps" screen where you can toggle it once.
>
> **Device Reboot vs. App Relaunch**: Physical device reboot (`android.permission.REBOOT`) requires system-level OS firmware signing; however, ATHENA will execute an **auto-relaunch with state recovery** (using a scheduled restart intent + clean process exit), ensuring your workspace, active tab, and experimental data are restored immediately upon relaunch. A one-tap shortcut to Android Developer Options is also included if you need to toggle "Install via USB" or "Verify apps over USB" on hardware.

---

## Proposed Changes

### 1. Permissions & File Provider Configuration
#### `app/src/main/AndroidManifest.xml`
- Add `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />` to allow in-app APK installation prompts.
- Verify `FileProvider` authorities (`${applicationId}.fileprovider`) and `file_paths.xml` coverage for APK installation packages.

---

### 2. Update & Reboot Management Engine
#### `app/src/main/java/com/example/system/update/AthenaUpdateManager.kt` (New)
- **Permission Checking**:
  - `canRequestPackageInstalls(context: Context): Boolean`
  - `openUnknownAppSourcesSettings(context: Context)`
  - `openDeveloperOptions(context: Context)`
- **Update Package Discovery**:
  - Discovers local update packages in `apk_builds/` or internal cache.
  - Provides a staging generator to prepare a signed OTA update bundle from active builds.
- **Package Installation Flow**:
  - Creates a content URI via `FileProvider.getUriForFile`.
  - Dispatches `Intent(Intent.ACTION_VIEW)` with MIME `application/vnd.android.package-archive` and `FLAG_GRANT_READ_URI_PERMISSION`.
- **State Recovery & Auto-Relaunch**:
  - `saveStateForUpdateRecovery(context: Context, currentTab: String, note: String)`: Stores recovery tokens in `SharedPreferences`.
  - `checkAndConsumeUpdateRecovery(context: Context): UpdateRecoveryInfo?`: Checks and consumes recovery token on launch.
  - `triggerCleanAppRestart(context: Context)`: Schedules an immediate relaunch of `MainActivity` via `AlarmManager` or `PendingIntent` and terminates the current process cleanly (`exitProcess(0)`).

---

### 3. Cybernetic UI Console
#### `app/src/main/java/com/example/ui/components/AthenaSystemUpdateConsole.kt` (New)
- Modal dialog / bottom sheet providing:
  - **OTA Permission Status Indicator**: Green badge when authorized, or an "Authorize Direct Installation" button if restricted.
  - **Developer Options Quick Access**: One-tap shortcut to Android Developer Settings to toggle "Install via USB" or "Verify apps over USB" if using a cable.
  - **Version & Build Telemetry**: Current version code, name, and package ID.
  - **Available Update Packages**: Card list showing found APK update files with title, file size, timestamp, and an **"Accept & Install Update"** button.
  - **Reboot & Clean Restart**: Dedicated action button to immediately relaunch ATHENA with state recovery armed.

---

### 4. Integration into ATHENA UI
#### `app/src/main/java/com/example/ui/components/SystemSettingsDrawerSheet.kt`
- Add a new "System Updates & Recovery" entry in the system settings drawer with direct trigger to open `AthenaSystemUpdateConsole`.

#### `app/src/main/java/com/example/ui/MainApp.kt`
- Wire `AthenaSystemUpdateConsole` visibility state.
- Add update icon button in the top bar actions.
- Check `checkAndConsumeUpdateRecovery` on launch to display a confirmation toast when ATHENA restarts after an update.

---

## Verification Plan

### Automated Build & Test Verification
1. Run `compile_applet` to verify compilation of `AthenaUpdateManager`, `AthenaSystemUpdateConsole`, and manifest changes.
2. Run `gradle :app:testDebugUnitTest` to ensure existing tests pass.

### Interactive Verification
1. Open the **System Updates & Recovery Console** from the top bar or settings drawer.
2. Verify that the permission check correctly indicates whether "Install unknown apps" is enabled, and test the one-tap settings shortcut.
3. Test the "Developer Options" button to ensure it navigates to device USB debugging settings.
4. Test the "Restart ATHENA" action to confirm clean state preservation and auto-relaunch.
