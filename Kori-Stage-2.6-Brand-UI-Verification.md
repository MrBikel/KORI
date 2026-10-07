# Kori (কড়ি) - Stage 2.6 Brand & UI Verification Report

## 1. Executive Summary
- **Bengali Brand Name**: **কড়ি** (Kori)
- **Visible Launcher Label**: **কড়ি** (`@string/app_name`)
- **Proposed Store Title**: **কড়ি: আয়-ব্যয়ের ব্যক্তিগত খাতা**
- **Primary Tagline**: **প্রতিটি কড়ির নির্ভুল হিসাব**
- **Alternative Tagline**: **ছোট খরচ থেকে পুরো হিসাব**
- **Application ID**: `com.aistudio.moneydiary.mndytr` (Unchanged)
- **Build System**: Portable Gradle Wrapper (`./gradlew`) verified clean with 0 warnings
- **Test Suite**: 54/54 tests passed (100% success rate, 0 failures, 0 skipped)

---

## 2. Brand & UI Identity Refinements Implemented

1. **Brand Name Replacement**:
   - Replaced development placeholder "My Money Diary" / "আমার টাকার ডায়েরি" across all visible user-facing interfaces with **কড়ি**.
   - Preserved `applicationId` and package name `com.aistudio.moneydiary.mndytr`.

2. **Platform Sync Compliance (`metadata.json`)**:
   - `name`: "কড়ি"
   - `description`: "কড়ি: আয়-ব্যয়ের ব্যক্তিগত খাতা - প্রতিটি কড়ির নির্ভুল হিসাব।"
   - `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` preserved.

3. **Vector Adaptive Launcher Icon System**:
   - **Symbol**: Abstract Cowrie Shell (কড়ি) monetary token + subtle horizontal ledger accents.
   - **Background**: Deep Teal (`#0F5C4D`)
   - **Foreground**: Pure White (`#FFFFFF`) & Warm Gold (`#F4D77B`)
   - **High Contrast**: Explicitly tested and optimized for legibility at 24px and standard viewport scales.
   - **Density Buckets Generated**:
     - `mipmap-mdpi/ic_launcher.png` (48x48)
     - `mipmap-hdpi/ic_launcher.png` (72x72)
     - `mipmap-xhdpi/ic_launcher.png` (96x96)
     - `mipmap-xxhdpi/ic_launcher.png` (144x144)
     - `mipmap-xxxhdpi/ic_launcher.png` (192x192)
     - `mipmap-anydpi-v26/ic_launcher.xml` & `ic_launcher_round.xml`

4. **Wordmark & Typography**:
   - Wordmark: `কড়ি`
   - Primary Font: Local `Hind Siliguri` SemiBold (`res/font/hind_siliguri.ttf`) with Open Font License.
   - Distinct letterform for "ড়" preserved at all viewport sizes.
   - Tagline Policy: Omitted from Dashboard Top Bar header as specified. Displayed on Onboarding and Welcome screens.

5. **Collision Management & Disclaimers**:
   - Recorded pending brand clearance for trademark registry, Google Play store title availability, web domain registration, and social media handles in `Kori-Brand-System.md`.

---

## 3. Test Execution Matrix (54 Tests Total)

```
================================================================================
Test Suite Results: 54 tests, 0 failures, 0 skipped, Duration: 13.86s (100% PASS)
================================================================================
- FinancialDatabaseRobolectricTest      : 15 / 15 PASSED
- FinancialEngineUnitTest                : 11 / 11 PASSED
- DiaryViewModelRobolectricTest          :  9 /  9 PASSED
- MoneyPaisaUnitTest                     :  6 /  6 PASSED
- SmartTextParserUnitTest                :  6 /  6 PASSED
- ArchitectureAndSchemaVerificationTest  :  3 /  3 PASSED
- LedgerEventCodeCoverageTest            :  2 /  2 PASSED
- ExampleRobolectricTest                 :  1 /  1 PASSED
- ExampleUnitTest                        :  1 /  1 PASSED
================================================================================
BUILD SUCCESSFUL in 32s (49 actionable tasks, 0 warnings)
```

---

## 4. Deliverables & SHA-256 Hashes
- **Debug APK**: `Kori-Stage-2.6-Brand-UI-Debug.apk` (23 MB)
  - SHA-256: `af97ef41e1a5b3a064fb14c1e665b0f8a780b69d37fbada8b5a47f262ef9193f`
- **Brand System Spec**: `Kori-Brand-System.md`
- **Verification Report**: `Kori-Stage-2.6-Brand-UI-Verification.md`
- **Source ZIP**: `Kori-Stage-2.6-Brand-UI-Source.zip`
