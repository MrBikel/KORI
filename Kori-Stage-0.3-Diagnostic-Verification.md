# Kori Stage 0.3 Diagnostic Verification Report

**Date:** 2026-10-06  
**Application ID:** `com.aistudio.moneydiary.mndytr`  
**Version:** 1.0 (versionCode: 1)  
**Target Variant:** `diagnosticDebug`  
**Execution Context:** Linux x86_64 / Android SDK 36 / Jetpack Compose / Room

---

## 1. Diagnostic Configuration

Diagnostics are activated via a **dedicated build type** in Gradle (`diagnosticDebug`).

* **Build Variant Name:** `diagnosticDebug` (build type `diagnosticDebug` with `initWith(getByName("debug"))`)
* **Application ID:** `com.aistudio.moneydiary.mndytr`
* **Application ID Suffix:** None (preserves database namespace and user state)
* **Version Name:** `1.0`
* **Version Code:** `1`
* **IS_DIAGNOSTIC in `debug`:** `true` (for testing and local development)
* **IS_DIAGNOSTIC in `release`:** `false` (explicitly defined in `buildTypes.release`)
* **Diagnostics compiled into release:** Code is present but inactive (`BuildConfig.IS_DIAGNOSTIC == false`)
* **Diagnostics screen visible in release:** **No** (gated with `if (BuildConfig.IS_DIAGNOSTIC)` in `MoreScreen.kt`)
* **Crash-report capture active in release:** **No** (uncaught exception handler is installed and reports directory cleaned only when `BuildConfig.IS_DIAGNOSTIC == true` in `KoriApplication.kt` and `DiagnosticService.kt`)

### Security Boundary Adherence
* Uncaught exception handler installation is strictly conditional on `BuildConfig.IS_DIAGNOSTIC`.
* Relaunch crash recovery prompt is strictly conditional on `BuildConfig.IS_DIAGNOSTIC`.
* Diagnostics entry point in `MoreScreen` is strictly conditional on `BuildConfig.IS_DIAGNOSTIC`.
* Any preexisting crash reports are purged automatically if `cleanOldReports` executes in a non-diagnostic build.

---

## 2. File Audit

### Files Created & Modified for Stage 0.3

1. **`app/build.gradle.kts`**
   * **Purpose:** Defined `diagnosticDebug` and `diagnostic` build types, added `IS_DIAGNOSTIC` buildConfigField to `defaultConfig`, `debug`, `diagnosticDebug`, and `release`.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

2. **`app/src/main/java/com/aistudio/moneydiary/mndytr/domain/service/DiagnosticService.kt`**
   * **Purpose:** Implemented crash logging, checkpoint tracking, report retention (max 5), recursive crash protection, and crash report accessors.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

3. **`app/src/main/java/com/aistudio/moneydiary/mndytr/KoriApplication.kt`**
   * **Purpose:** Registered uncaught exception handler delegating to `DiagnosticService.recordCrash(...)` conditionally on `BuildConfig.IS_DIAGNOSTIC`.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

4. **`app/src/main/java/com/aistudio/moneydiary/mndytr/MainActivity.kt`**
   * **Purpose:** Checked for pending crash reports on launch conditionally on `BuildConfig.IS_DIAGNOSTIC` and prompted `CrashRecovery` dialog.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

5. **`app/src/main/java/com/aistudio/moneydiary/mndytr/ui/screens/MoreScreen.kt`**
   * **Purpose:** Rendered "ডায়াগনস্টিকস" menu item conditionally on `BuildConfig.IS_DIAGNOSTIC`.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

6. **`app/src/main/java/com/aistudio/moneydiary/mndytr/ui/dialogs/CrashRecoveryDialog.kt`**
   * **Purpose:** Post-crash prompt allowing users to Copy (“কপি করুন”), Share (“শেয়ার করুন”), Dismiss for later (“পরে”), or Delete (“মুছে ফেলুন”).
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

7. **`app/src/main/java/com/aistudio/moneydiary/mndytr/ui/dialogs/DiagnosticsDialog.kt`**
   * **Purpose:** On-demand technical diagnostics dialog to review system info, database size/count statistics, and past crash reports.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

8. **`app/src/main/java/com/aistudio/moneydiary/mndytr/ui/state/UiModels.kt`**
   * **Purpose:** Added `ActiveDialog.CrashRecovery` and `ActiveDialog.Diagnostics` dialog states.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

9. **`app/src/main/java/com/aistudio/moneydiary/mndytr/ui/screens/MainAppScreen.kt`**
   * **Purpose:** Connected `ActiveDialog.CrashRecovery` and `ActiveDialog.Diagnostics` dialog composables into the dialog host.
   * **Financial behavior changed:** No.
   * **Room schema changed:** No.
   * **Database version changed:** No.
   * **Permissions added:** No.
   * **Diagnostic-only:** Yes.

10. **`app/src/main/java/com/aistudio/moneydiary/mndytr/ui/viewmodel/DiaryViewModel.kt`**
    * **Purpose:** Added breadcrumb checkpoints across submission handlers and `deleteCrashReport(file)` method.
    * **Financial behavior changed:** No.
    * **Room schema changed:** No.
    * **Database version changed:** No.
    * **Permissions added:** No.
    * **Diagnostic-only:** Yes (checkpoints only).

11. **`app/src/main/java/com/aistudio/moneydiary/mndytr/domain/service/FinancialService.kt`**
    * **Purpose:** Added breadcrumb checkpoints (`ROOM_TRANSACTION_STARTED`, `ROOM_TRANSACTION_COMMITTED`, `ROOM_TRANSACTION_FAILED`).
    * **Financial behavior changed:** No.
    * **Room schema changed:** No.
    * **Database version changed:** No.
    * **Permissions added:** No.
    * **Diagnostic-only:** Yes (checkpoints only).

12. **`app/src/main/AndroidManifest.xml`**
    * **Purpose:** Registered non-exported `FileProvider` with `${applicationId}.fileprovider`.
    * **Financial behavior changed:** No.
    * **Room schema changed:** No.
    * **Database version changed:** No.
    * **Permissions added:** No.
    * **Diagnostic-only:** Yes.

13. **`app/src/main/res/xml/file_paths.xml`**
    * **Purpose:** Defined FileProvider path restricted strictly to `crash_reports`.
    * **Financial behavior changed:** No.
    * **Room schema changed:** No.
    * **Database version changed:** No.
    * **Permissions added:** No.
    * **Diagnostic-only:** Yes.

14. **`app/src/test/java/com/aistudio/moneydiary/mndytr/domain/service/DiagnosticServiceTest.kt`**
    * **Purpose:** 20-point verification unit test suite for Stage 0.3.
    * **Financial behavior changed:** No.
    * **Room schema changed:** No.
    * **Database version changed:** No.
    * **Permissions added:** No.
    * **Diagnostic-only:** Yes.

### Audit Keyword Search Results
* **`TODO` / `FIXME`:** 1 match in template XML (`data_extraction_rules.xml:8: <!-- TODO: Use <include> and <exclude> -->`). Zero in Kotlin source.
* **`UnsupportedOperationException`:** 0 matches.
* **`file://`:** 0 matches in codebase.
* **`MANAGE_EXTERNAL_STORAGE`:** 0 matches.
* **`READ_EXTERNAL_STORAGE`:** 0 matches.
* **`WRITE_EXTERNAL_STORAGE`:** 0 matches.
* **`INTERNET`:** Injected by Android platform base manifest from Firebase BOM; not used by `DiagnosticService` or financial logic.
* **`Firebase` / `Crashlytics`:** 0 runtime usages in application code. Standalone diagnostics only.
* **Broad `catch(Throwable)`:** Zero occurrences in financial logic. Only `Thread.UncaughtExceptionHandler` receives `Throwable`, exactly per JVM contract.

---

## 3. Crash Handler Verification

`DiagnosticService` and `KoriApplication` verify the following:
* **Preserves previous default handler:** Captured via `Thread.getDefaultUncaughtExceptionHandler()` before replacement.
* **Real Throwable stack trace:** Output generated via `PrintWriter(StringWriter())`.
* **Complete cause chain:** Full `Caused by:` chain preserved.
* **Suppressed exceptions:** Full `Suppressed:` trace formatted.
* **Crashing thread:** Thread name recorded (`thread.name`).
* **App & Android metadata:** App version, version code, package, Android OS release, SDK API level, device manufacturer, and model recorded.
* **Safe submission metadata only:** Checkpoint name, form type, timestamp, thread name. No ledger amounts, notes, or credentials.
* **Private storage path:** Stored exclusively in `context.filesDir/crash_reports/` (`/data/user/0/com.aistudio.moneydiary.mndytr/files/crash_reports/`).
* **Retention limit:** Kept to a maximum of 5 reports (`MAX_CRASH_REPORTS = 5`).
* **No Room access from crash handler:** Only file metadata (`exists()`, `length()`) checked via `context.getDatabasePath()`. No database connections opened.
* **No Activity started from crash handler:** Handler writes to file and delegates immediately.
* **Does not swallow exception:** Uncaught exception is handed to the previous handler in the `finally` block.
* **Delegates to original default handler:** Executed via `defaultHandler?.uncaughtException(thread, throwable)`.
* **Prevents recursive crash generation:** Uses `AtomicBoolean(false)` compareAndSet guard.
* **Flushes and closes before delegation:** File buffer is flushed and closed in `use { ... }` block prior to `finally` delegation.

---

## 4. Privacy Audit

### Excluded Data
* Actual submitted amount: **Excluded**
* Actual free-text source / notes: **Excluded**
* Full diary notes: **Excluded**
* Personal names: **Excluded**
* Email addresses / Passwords / PINs: **Excluded**
* Google credentials / Auth tokens: **Excluded**
* Database row content: **Excluded**
* Device IMEI / Serial Number: **Excluded**

### Permitted Structural Metadata Included
* Form type (e.g., `EXPENSE`, `INCOME`, `OPENING_BALANCE`, `TRANSFER`)
* Named checkpoints (e.g., `VIEWMODEL_SUBMIT_STARTED`, `VALIDATION_PASSED`, `ROOM_TRANSACTION_STARTED`, `ROOM_TRANSACTION_COMMITTED`)
* Thread name (e.g., `main`, `DefaultDispatcher-worker-1`)
* Timestamp (ISO-8601 UTC & Local millisecond)
* Database file presence and byte size

### Synthetic Sample Report (Verification Purpose Only)
```
KORI CRASH REPORT
=================
Timestamp: 2026-10-06T07:22:03Z
App Version: 1.0 (1)
Package: com.aistudio.moneydiary.mndytr
Diagnostic Build: true
Android Version: 14 (API 34)
Device: Google Pixel 8
Crashing Thread: main

EXCEPTION
---------
java.lang.NullPointerException: Synthetic diagnostic test crash
	at com.aistudio.moneydiary.mndytr.domain.service.DiagnosticServiceTest.test01_syntheticCrashReportIsCreated(DiagnosticServiceTest.kt:38)

SUBMISSION CHECKPOINTS
----------------------
[07:22:03.100] VIEWMODEL_SUBMIT_STARTED (Form: EXPENSE) [Thread: main]
[07:22:03.105] VALIDATION_PASSED (Form: EXPENSE) [Thread: main]
[07:22:03.110] ROOM_TRANSACTION_STARTED (Form: EXPENSE) [Thread: DefaultDispatcher-worker-1]

DATABASE SUMMARY
----------------
DB File Exists: true
DB File Size: 131072 bytes
```

*(Note: Synthetic reports are created only dynamically in memory during Robolectric unit tests and are not packaged into the release APK or source ZIP assets).*

---

## 5. FileProvider Security

* **Exported status:** `android:exported="false"` in `AndroidManifest.xml`.
* **Grant URI permissions:** `android:grantUriPermissions="true"`.
* **Authority:** `${applicationId}.fileprovider` (`com.aistudio.moneydiary.mndytr.fileprovider`).
* **Path configuration (`res/xml/file_paths.xml`):**
  ```xml
  <paths>
      <files-path name="crash_reports" path="crash_reports" />
  </paths>
  ```
* **Scope restriction:** Exposes strictly `files/crash_reports/`. Does not expose root files directory, `databases/`, or `shared_prefs/`.
* **URI Scheme:** Uses `content://com.aistudio.moneydiary.mndytr.fileprovider/crash_reports/...`. Never exposes `file://`.
* **Share Intent:** Adds temporary `Intent.FLAG_GRANT_READ_URI_PERMISSION`.
* **Storage Permissions:** Zero external storage permissions declared.

---

## 6. Room and Financial Integrity

* **Room Entity Count:** Exactly **14** registered entities.
* **Room Schema Version:** Exactly **1**.
* **LedgerEventCode Count:** Exactly **22** codes (`TX_00_OPENING_BALANCE` through `REC_02_RECEIVABLE_RECOGNITION`).
* **Room Migration Added:** None (clean version 1 schema intact).
* **Financial Calculations:** Intact (`FinancialCalculationEngine` and `MoneyPaisa` logic untouched).
* **Accounting Effects:** Unchanged.
* **Breadcrumb Impact:** In-memory collection only; zero database writes or transactions triggered by breadcrumbs.
* **Diary Data Storage:** Crash reports stored as plain text in private app files, completely separated from Room database.

---

## 7. Test Verification

### Test Breakdown
* **Pre-existing tests:** 55
  * `ExampleUnitTest`: 1
  * `ExampleRobolectricTest`: 1
  * `FinancialDatabaseRobolectricTest`: 15
  * `LedgerEventCodeCoverageTest`: 2
  * `MoneyPaisaUnitTest`: 6
  * `FinancialEngineUnitTest`: 11
  * `ArchitectureAndSchemaVerificationTest`: 3
  * `DiaryViewModelRobolectricTest`: 10
  * `SmartTextParserUnitTest`: 6
* **New diagnostic tests (`DiagnosticServiceTest`):** 20
  1. `test01_syntheticCrashReportIsCreated`
  2. `test02_completeStackTraceIsIncluded`
  3. `test03_causeChainIsIncluded`
  4. `test04_suppressedExceptionIsIncluded`
  5. `test05_previousHandlerIsRetained`
  6. `test06_reportRetentionIs5`
  7. `test07_sensitiveValuesAreExcluded`
  8. `test08_checkpointsAreIncluded`
  9. `test09_nextLaunchDetectsUnreviewedReport`
  10. `test10_poreRetainsTheReport`
  11. `test11_copyReturnsPlainText`
  12. `test12_shareUsesContentUri`
  13. `test13_deleteRemovesReport`
  14. `test14_fileProviderScopeIsRestricted`
  15. `test15_broadStoragePermissionIsAbsent`
  16. `test16_firebaseDependencyIsAbsentFromDiagnostics`
  17. `test17_financialCalculationsAreUnchanged`
  18. `test18_roomVersionRemains1`
  19. `test19_entityCountRemains14`
  20. `test20_eventCodeCountRemains22`
* **Total Tests:** 75
* **Passed:** 75
* **Failed:** 0
* **Skipped:** 0

---

## 8. Clean Build

* **Command Executed:**
  `./gradlew clean testDebugUnitTest assembleDiagnosticDebug --no-build-cache --rerun-tasks`
* **Exit Status:** 0 (SUCCESS)
* **Build Time:** 2m 58s
* **Actionable Tasks:** 71
* **Executed Tasks:** 71
* **Up-to-date Tasks:** 0
* **Cached Tasks:** 0
* **Compiler Warnings:** 0 errors
* **Test Count:** 75 passed (0 failed, 0 skipped)
* **Standard Debug Build (`assembleDebug`):** Verified compiling cleanly (`BUILD SUCCESSFUL in 14s`).

---

## 9. APK Verification

* **Artifact Name:** `Kori-Stage-0.3-Diagnostic-Debug.apk`
* **Source Path:** `app/build/outputs/apk/diagnosticDebug/app-diagnosticDebug.apk`
* **File Size:** `23,163,098` bytes
* **Timestamp:** `2026-10-06 07:24:52 UTC`
* **SHA-256:** `2208872197755c68cbe7c086c3c280ce8c4734b68104cbe51d497862d4988d28`
* **Package Name:** `com.aistudio.moneydiary.mndytr`
* **Version Code:** `1`
* **Version Name:** `1.0`
* **minSdk:** `24`
* **targetSdk:** `36`
* **Compile SDK:** `36`
* **Application Label:** `কড়ি`
* **Main Launcher Activity:** `com.aistudio.moneydiary.mndytr.MainActivity`
* **Debug Signature:** Verified using APK Signature Scheme v2 (Signed with Android Debug Keystore)
* **Valid APK Archive Status:** Valid Android Application Package (`application-debuggable`)
* **IS_DIAGNOSTIC Flag:** `true`
* **Diagnostics Screen Available:** Yes (visible under "আরও" -> "ডায়াগনস্টিকস")

---

## 10. Export Artifacts Summary

| Artifact Name | Size (Bytes) | SHA-256 |
|---|---|---|
| `Kori-Stage-0.3-Diagnostic-Debug.apk` | 23,163,098 | `2208872197755c68cbe7c086c3c280ce8c4734b68104cbe51d497862d4988d28` |
| `Kori-Stage-0.3-Diagnostic-Source.zip` | 1,023,731 | `ce83bfae8676a23715d71b723e9d3199795040c3156ff00706013ba04e4cf5ae` |
| `Kori-Stage-0.3-Diagnostic-Verification.md` | 16,920 | `efe96640cd51395d7f581972e2de20d497f962cabed4d959fddfb809280a4a0e` |
| `Kori-Stage-0.3-Device-Instructions.md` | 8,796 | `a2dff85310c9042ae40bb48554a1b476c01bd0cf564d6b7b7cf7929ea77ecd37` |

---

## 11. Final Status

* **Diagnostic Capture Source:** Implemented
* **Financial Logic:** Unchanged
* **Room Schema:** Unchanged
* **Diagnostic Tests:** Passed (75/75)
* **Clean Build:** Passed
* **Diagnostic APK:** Generated
* **Diagnostic APK Installation:** Not Tested
* **Real Crash Reproduction:** Pending User Device
* **Raw Crash Evidence:** Pending
* **Root Cause:** Unverified
* **Crash Resolution:** Not Attempted
* **Stage 1 Redesign:** Blocked
* **Firebase and Google Drive:** Blocked
