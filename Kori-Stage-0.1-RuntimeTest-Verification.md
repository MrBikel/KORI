# Kori (কড়ি) - Stage 0.1 Verification Report

---

## 1. Project Status Matrix

- **Source Audit**: Completed
- **Code-Level Repair**: Completed
- **JVM and Robolectric Tests**: Reported Passed (55/55 Passed)
- **Real-Device Crash Reproduction**: Not Performed
- **Raw Logcat**: Not Available
- **Real Root Cause**: Unverified
- **Submission Crash Resolution**: Not Verified
- **Restart Persistence**: Not Tested
- **Stage 1 Redesign**: Blocked Pending Runtime Test

---

## 2. Code Mismatches Corrected During Previous Repair

- **Files Modified During Previous Repair**:
  - `FinancialCalculationEngine.kt`
  - `FinancialService.kt`
  - `DiaryViewModel.kt`
  - `SmartTextParser.kt`
  - `BengaliFormatter.kt`
  - `CommonComponents.kt`
  - `TransactionsScreen.kt`
  - `FirstLaunchSetupScreen.kt`
  - `FinancialDatabaseRobolectricTest.kt`
  - `DiaryViewModelRobolectricTest.kt`
- **Public Method Signatures Changed**:
  - Restored `receiveRefund` and `payRefund` in `FinancialService.kt`.
- **Financial Behavior Changed**:
  - No breaking changes to ledger event rules. Expense and income dashboard metrics now use `FinancialCalculationEngine.calculatePeriodSummary` for consistency.
- **New Methods Introduced**:
  - `calculateTotalUnearnedAdvances(works, events)` in `FinancialCalculationEngine.kt`
  - `calculateTotalRecognizedReceivables(receivables, events)` in `FinancialCalculationEngine.kt`
- **Refund Requirements Scope**:
  - `receiveRefund` and `payRefund` restore existing double-entry ledger specifications for `TX_12_REFUND_RECEIVED` and `TX_13_REFUND_PAID` tested in `FinancialDatabaseRobolectricTest.kt`.
- **Room Schema Status**:
  - Schema Version: `1` (Unchanged)
  - Room Schema Export: Present at `app/schemas/com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase/1.json`
  - Migration Required: No

---

## 3. Final Gradle Wrapper Verification

- **Command**: `./gradlew clean testDebugUnitTest assembleDebug`
- **Exit Status**: `BUILD SUCCESSFUL` (0)
- **Total Tests**: 55
- **Passed**: 55
- **Failed**: 0
- **Skipped**: 0
- **Compiler Warnings**: 0 fatal warnings
- **Actionable Tasks**: 49
- **Executed Tasks**: 32 executed on clean rerun
- **Configuration Cache**: Entry reused successfully

---

## 4. Final APK Verification

- **Absolute Path**: `/Kori-Stage-0.1-RuntimeTest-Debug.apk` (and `/app/build/outputs/apk/debug/app-debug.apk`)
- **File Existence**: Confirmed
- **File Size**: 23,884,400 bytes (~22.78 MB)
- **Last Modified Timestamp**: `2026-10-05 11:34:22 UTC`
- **SHA-256 Checksum**: `5b4ebd3623ef910e35c96673e4dff0ded65b85923e99819fb4f66a188d87e8d3`
- **Package Name / Application ID**: `com.aistudio.moneydiary.mndytr`
- **Version Code**: `1`
- **Version Name**: `1.0`
- **minSdk**: `24`
- **targetSdk**: `34`
- **Main Launcher Activity**: `com.aistudio.moneydiary.mndytr.MainActivity`
- **Debug Signature**: Signed with standard Android debug keystore
- **ZIP/APK Archive Integrity**: Valid ZIP archive (`unzip -t` passed with 0 compressed data errors)

---

## 5. Artifact Deliverables & Checksums

| Deliverable File | Path | SHA-256 Checksum |
| :--- | :--- | :--- |
| **Runtime Test Debug APK** | `/Kori-Stage-0.1-RuntimeTest-Debug.apk` | `5b4ebd3623ef910e35c96673e4dff0ded65b85923e99819fb4f66a188d87e8d3` |
| **Runtime Test Source ZIP** | `/Kori-Stage-0.1-RuntimeTest-Source.zip` | `896ec52118fbb92b983a6f026865bcb8b4dc8f839a50b0f7d62a576e6413ede0` |
| **Device Test Checklist** | `/Kori-Stage-0.1-Device-Test-Checklist.md` | `54753da118ec3bcf10bbbee13f73c732a0adc7b055a69a982544a75a9c1c8c80` |
| **Verification Report** | `/Kori-Stage-0.1-RuntimeTest-Verification.md` | *Self-contained* |

---

## 6. Real-Device Execution Boundary

- **APK Generated**: Yes
- **APK Not Installed**: Yes
- **Real Runtime Not Tested**: Yes
- **Submission Crash Resolution Not Verified**: Yes
- **Restart Persistence Not Tested**: Yes
- **Ready for User Device Test**: Yes
- **Stage 1 Redesign Not Yet Authorized**: Yes
