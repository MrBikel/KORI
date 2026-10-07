# My Money Diary (আমার টাকার ডায়েরি)

## Project Overview
- **Project Name:** My Money Diary (আমার টাকার ডায়েরি)
- **Package Name / Namespace:** `com.aistudio.moneydiary.mndytr`
- **Application ID:** `com.aistudio.moneydiary.mndytr`
- **Current Stage:** Stage 2, Prompt 03.1 — Local Financial Core Foundation (Verified, Rectified, Rebuilt & Exported)

---

## Technical Specifications
- **Database Engine:** Android Room Persistence Library 2.7.0
- **Database Schema Version:** Version 1 (`exportSchema = true`)
- **Schema JSON Path:** `app/schemas/com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase/1.json`
- **Room Entity Count:** Exactly 14 entities
  1. `accounts`
  2. `categories`
  3. `persons`
  4. `work_records`
  5. `transactions` (Authoritative Ledger Events)
  6. `receivables`
  7. `payables`
  8. `savings_goals`
  9. `frequent_shortcuts`
  10. `attachments`
  11. `backup_jobs` (Schema-only in MVP)
  12. `backup_metadata` (Schema-only in MVP)
  13. `app_settings` (Non-secret settings only)
  14. `audit_logs` (Append-only immutable audit trail)
- **Ledger Event Code Count:** Exactly 22 distinct event codes across 5 `LedgerEventKind` categories
- **Monetary Storage & Representation:** Strict 64-bit integer Paisa (`Long`, where ৳1.00 = 100 Paisa). Checked arithmetic via `Math.addExact` / `Math.subtractExact` with zero floating-point calculations.

---

## Implemented Scope (Stage 2 Core Foundation)
- Complete 14-entity Room schema with `ForeignKey.RESTRICT` rules protecting financial history.
- 14 Data Access Objects (DAOs) supporting transactional operations.
- `FinancialCalculationEngine` supporting checked balance derivations, period income/expense filtering, and work contract projections.
- `FinancialService` orchestrating 20 domain operations (Opening balances, Direct income/expenses, Advances, Receivables, Payables, Loans, Transfers with fees, Refunds with ceilings, Balance adjustments, Reversals, and Atomic work settlements).
- 36 Automated Unit and Database Tests (all passing with 0 failures and 0 errors).

---

## Deferred Scope (Post-MVP Roadmap)
- Full Bengali Jetpack Compose UI (Dashboard, Quick Add modal, Smart-text parser, Work ledger screens) — Scheduled for Prompt 04.
- Encrypted Google Drive file upload and cloud backup sync.
- Real-time multi-device mutation sync (`sync_queue` and `conflict_staging` entities).
- Device biometric authentication prompts and hardware keystore encryption.
- Camera receipt OCR and background SMS banking parsing.

---

## Build & Test Requirements
- **Runtime JDK:** OpenJDK 21.0.12.1 (Temurin LTS)
- **Gradle Version:** Gradle 9.3.1
- **Android Gradle Plugin (AGP):** 9.1.1
- **Kotlin Version:** 2.2.10
- **KSP Version:** 2.3.5
- **compileSdk:** 36 (Android 16 / API 36)
- **minSdk:** 24 (Android 7.0)
- **targetSdk:** 36

### Build Commands
```bash
# Clean build, execute all unit and database tests, and assemble debug APK
gradle clean testDebugUnitTest assembleDebug
```

### Test Commands
```bash
# Execute unit and Robolectric database tests
gradle testDebugUnitTest
```

---

## Verified Artifacts
- **Debug APK Artifact:** `My-Money-Diary-Stage-2-Core-Debug.apk` (or `app/build/outputs/apk/debug/app-debug.apk`)
- **APK File Size:** 22,390,787 bytes (21.35 MB)
- **APK SHA-256 Checksum:** `1f521170f19b0f93426da39e89d8f42cbabc86871a3343dfd57a43ea4dbf90d6`
- **Source ZIP Artifact:** `My-Money-Diary-Stage-2-Core-Source.zip`

---

## Status Boundaries
- **Core Source:** Implemented
- **Core Tests:** 36 Tests Passed (0 Failed, 0 Skipped)
- **Clean Build:** Passed (Gradle 9.3.1)
- **Debug APK:** Generated and Verified
- **APK Installation:** Not Installed (Pending UI stage)
- **Runtime Verification:** Not Tested (Pending UI stage)
- **UI:** Not Implemented (Scheduled for Prompt 04)
- **Google Drive Backup:** Not Implemented / Not Connected
- **Multi-Device Live Sync:** Deferred
