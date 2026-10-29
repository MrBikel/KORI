# KORI (কড়ি) - Google Play Release Verification Report

**Date of Release Verification:** 2026-10-26  
**Application ID:** `com.aistudio.moneydiary.mndytr`  
**Namespace:** `com.aistudio.moneydiary.mndytr`  
**Version:** 1.0 (versionCode 1)  
**SDK Configuration:** minSdk 24, targetSdk 36, compileSdk 36  
**Database Schema:** Version 4 (Room SQLite, non-destructive explicit migrations MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)  
**Developer Attribution:** Developed by Mehedi Hasan  

---

## 1. Release Android App Bundle (AAB) Verification

- **Relative Path:** `app/build/outputs/bundle/release/app-release.aab`
- **Absolute Path:** `/app/applet/app/build/outputs/bundle/release/app-release.aab`
- **Size (Bytes):** `17,540,056 bytes`
- **Size (MiB):** `16.73 MiB`
- **SHA-256 Checksum:** `b6be7c77fccb3826e670bddf0941346581b0879fe4e72dd0f7ad257516f14be2`
- **Bundle Format:** Valid Android App Bundle (Contains `BundleConfig.pb`, `BUNDLE-METADATA`, multi-dex `base/dex/classes{1..4}.dex`, protobuf resources).
- **Package Name:** `com.aistudio.moneydiary.mndytr` (Verified from merged bundle manifest).
- **Version Code:** `1`
- **Version Name:** `1.0`
- **Target SDK:** `36` (Android 16 compatible)
- **Min SDK:** `24` (Android 7.0 Nougat)
- **Debuggable Status:** `android:debuggable` is **false** (omitted in release manifest, `BuildConfig.DEBUG = false`, `BuildConfig.IS_DIAGNOSTIC = false`).
- **Signing Status:** **Unsigned Release Bundle** (Signing status: **Blocked**). As per security guidelines, no fake upload keystore was fabricated. The release bundle has been built through `:app:packageReleaseBundle` and `:app:bundleRelease` and is ready for signing with the developer's private Play Console upload key via `jarsigner` / `apksigner` or Google Play App Signing.

---

## 2. Debug APK Verification

- **Relative Path:** `app/build/outputs/apk/debug/app-debug.apk`
- **Absolute Path:** `/app/applet/app/build/outputs/apk/debug/app-debug.apk`
- **Size (Bytes):** `25,934,210 bytes`
- **Size (MiB):** `24.73 MiB`
- **SHA-256 Checksum:** `6e594ac6ef26f8b05e0cf1161a3a017eca32694bf0a70b29c46b00699389764d`
- **Package Name:** `com.aistudio.moneydiary.mndytr`
- **Version Code:** `1`
- **Version Name:** `1.0`
- **Debuggable Status:** `true` (debuggable build with debug keystore for development / emulator inspection).

---

## 3. Data Safety & Architecture Validation

- **Room Schema Version:** 4
- **Destructive Migration Fallback:** Completely absent (`fallbackToDestructiveMigration` and `fallbackToDestructiveMigrationOnDowngrade` are NOT configured).
- **Schema Preservation:** Explicit, verified migrations (`MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`) preserve all existing records, amounts, IDs, timestamps, categories, Bengali strings, accounts, and relationships.
- **Migration Unit Tests:** Tested and passed with real pre-populated Version 3 SQLite databases (`Migration3To4RealDatabaseTest`).
- **Backup & Restore:** Transactional with pre-restore safety points and rollback on syntax error.
- **CSV Handling:** Strict UTF-8 parsing with duplicate row validation and error reporting.
- **Google Drive / Cloud Status:** Clearly marked unavailable in UI ("ক্লাউড সিঙ্ক অনুপলব্ধ"), zero misleading encryption claims.
- **External AI Status:** No runtime remote AI calls; local deterministic economic calculations.
- **PDF Generation:** Genuine native Android `PdfDocument` Canvas generation with bundled Bengali typefaces (`Adorsholipi`, `Noto Serif Bengali`, `Hind Siliguri`).
