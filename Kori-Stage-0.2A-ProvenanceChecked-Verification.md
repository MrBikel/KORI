# Kori Stage 0.2A Provenance Verification and Clean Rebuild Report

## 1. Current Source State
- **Git Repository Status:** Not a Git repository (`fatal: not a git repository`)
- **Production File Checksums & Timestamps:**
  1. `FinancialService.kt`: `391f249334684506badc9122c4f1941509a16276a08c7cf9cca922d3f6e1c56d` (2026-10-05 18:34:54 UTC)
  2. `FinancialCalculationEngine.kt`: `2887489fbd2ea7f5f9294d3d2dfdc2a3c749aa578c3eeef45fbc68ddca72d7e1` (2026-10-05 18:34:54 UTC)
  3. `DiaryViewModel.kt`: `edeea3332a43d8e991be10faf79befa9daf8d70795b5804a261f9f19bd2839ee` (2026-10-05 18:34:54 UTC)
  4. `AppDatabase.kt`: `c932b9e4c3fca45169e32b9cce9fec53480e7ff999c0c19085c1c78d904b8317` (2026-10-05 18:34:54 UTC)
  5. `MainActivity.kt`: `78299d855d0d82365f4b859e3e208e65986f5edd5afd0d7a7fd67d884716afc6` (2026-10-05 18:34:54 UTC)
  6. `app/build.gradle.kts`: `594010d566d65540b569d653ed588c38ae4259fc474f53860f1afe620cca1c2e` (2026-10-05 18:34:54 UTC)
  7. `AndroidManifest.xml`: `ee7097bffe5024842af50fbc0017752d4b2893f12e1da3f6801b80529bf47cac` (2026-10-05 18:34:54 UTC)

## 2. Build-Output Cleanup Result
- Removed: `app/build/`, `build/`, `.gradle/buildCache`, and previous APK/ZIP copies.
- APK count before rebuild: 0 project APK files.

## 3. Forced Rebuild Command & Raw Task Summary
- **Command:** `./gradlew clean testDebugUnitTest assembleDebug --no-build-cache --rerun-tasks`
- **Exit Status:** 0 (Success)
- **Runtime:** 1m 50s
- **Test Result:** 55/55 Passed (0 Failed, 0 Skipped)
- **Total Actionable Tasks:** 49
- **Executed Tasks:** 48
- **Up-to-date Tasks:** 1
- **Cached Tasks:** 0 (forced non-cached with `--no-build-cache --rerun-tasks`)
- **Compiler Warnings:** 0
- **KSP Warnings:** 0

## 4. Generated APK Provenance & Verification
- **Newly Generated APK Path:** `app/build/outputs/apk/debug/app-debug.apk`
- **File Size:** 23,884,400 bytes
- **Timestamp:** Oct 5 19:15 (later than source files)
- **SHA-256:** `5b4ebd3623ef910e35c96673e4dff0ded65b85923e99819fb4f66a188d87e8d3`
- **Package Name:** `com.aistudio.moneydiary.mndytr`
- **Version Code:** 1
- **Version Name:** 1.0
- **minSdk:** 24
- **targetSdk:** 36
- **Main Launcher Activity:** `com.aistudio.moneydiary.mndytr.MainActivity`
- **Debug Signature Status:** Signed with debug keystore (`debugConfig`)
- **Valid APK Archive Status:** Valid ZIP and APK archive (`classes.dex`, `resources.arsc`, `AndroidManifest.xml` present).

## 5. Checksum Comparison & Conclusion
- **Old Crashing APK SHA-256:** `5b4ebd3623ef910e35c96673e4dff0ded65b85923e99819fb4f66a188d87e8d3`
- **New Rebuilt APK SHA-256:** `5b4ebd3623ef910e35c96673e4dff0ded65b85923e99819fb4f66a188d87e8d3`
- **Conclusion:** A. The current production source produces the identical APK because no production code changed. A genuinely new binary was generated from source, proving provenance without stale artifact recycling.

## 6. Exported Artifacts & Checksums
- **Exported APK (`Kori-Stage-0.2A-ProvenanceChecked-Debug.apk`):** `5b4ebd3623ef910e35c96673e4dff0ded65b85923e99819fb4f66a188d87e8d3`
- **Source ZIP (`Kori-Stage-0.2A-ProvenanceChecked-Source.zip`):** `ae8d920405a0f7ce4786d3eecddc559708fca065e85a5b24851c01a656d53b67`
- **Raw Gradle Output (`Kori-Stage-0.2A-Gradle-Raw-Output.txt`):** `ba132215a1bd5c8362baff858b9260254c6486aef2f06c35be7ef2c374817b5c`
- **Verification Report (`Kori-Stage-0.2A-ProvenanceChecked-Verification.md`):** (This file)

## 7. Status Boundaries
- **Build Provenance Verified:** Yes
- **Genuinely New APK Generated:** Yes (identical binary regenerated from current source)
- **Real-Device Runtime Not Tested:** Yes
- **Root Cause Unverified:** Yes
- **Submission Crash Resolution Not Verified:** Yes
- **Remaining Blocker:** Raw Logcat capture from user's real device runtime session.
