# কড়ি (Kori) Final Product Specification

**Version**: 1.0 (versionCode 1)  
**Package**: `com.aistudio.moneydiary.mndytr`  
**Platform**: Android (minSdk 24, targetSdk 36, compileSdk 36)  
**Language & Architecture**: Kotlin + Jetpack Compose + Material Design 3 + Room Database (Offline-first, Diary-first)  
**Primary Language**: বাংলা (Bengali)

---

## 1. Executive Summary & Core Objective
কড়ি (Kori) is a private, Bengali-first, offline-first personal income and expense diary designed to help users understand their spending habits, identify savings opportunities, and cultivate economic discipline.

The core user experience is:
> **লিখুন** (Record)  
> → **ডায়েরিতে রাখুন** (Keep in Diary)  
> → **সময়ের সঙ্গে তুলনা করুন** (Compare across Periods)  
> → **বেশি ব্যয়ের খাত বুঝুন** (Understand High-Spending Categories)  
> → **ছোট সাশ্রয়ের সুযোগ শনাক্ত করুন** (Identify Small Savings Opportunities)  
> → **মিতব্যয়ী হওয়ার অগ্রগতি দেখুন** (Track Progress towards Frugality)

### What কড়ি Is NOT
- **Not a digital wallet** (no bKash, Nagad, or bank balance dashboards)
- **Not a payment or money-transfer app**
- **Not an account-reconciliation service**
- **Not a business accounting ERP**
- **Not reliant on mandatory online accounts or third-party servers**

---

## 2. Information Architecture & Four Main Destinations

The application UI is structured into exactly four core destinations via a bottom navigation bar:

### A. আজ (Today)
- **Wordmark & Bengali Date**: Distinctive editorial heading with the current Bengali date (`d MMMM yyyy, h:mm a` formatting).
- **Today's Financial Summary**: Today's Income (আজকের আয়), Today's Expense (আজকের ব্যয়), and Net Daily Change (নিট পরিবর্তন).
- **Empty-First Experience**:
  - Headline: "আজ এখনো কোনো হিসাব লেখা হয়নি।"
  - Subtitle: "প্রথম হিসাবটি লিখলেই কড়ি আপনার ডায়েরি সাজানো শুরু করবে।"
  - Clear dual actions: **ব্যয় লিখুন** and **আয় লিখুন**.
  - No synthetic charts, fake cards, or empty zero grids.
- **Recent Diary Feed**: Today's chronological stream of entries with category icons, descriptive notes, and necessity tags.
- **Restrained Local Insight**: At most one deterministic insight card highlighting noteworthy daily trends.

### B. ডায়েরি (Diary)
- **Date-Grouped Entries**: Entries grouped by date with daily income total, daily expense total, and daily net change.
- **Search & Filter Suite**:
  - Live description search and source text search.
  - Category filter (dropdown).
  - Entry type filter (সকল, ব্যয়, আয়).
  - Necessity classification filter (সকল, প্রয়োজনীয়, নিয়মিত, ইচ্ছাধীন, জরুরি, অনির্ধারিত).
- **Entry Details & Correction**: Modal view showing full transaction context, category, source, necessity, timestamp, and safe deletion/correction.

### C. বিশ্লেষণ (Analysis)
- **Period Comparisons**:
  - আজ বনাম গতকাল (Today vs Yesterday)
  - চলতি সপ্তাহ বনাম গত সপ্তাহ (Current Week vs Last Week)
  - চলতি মাস বনাম গত মাস (Current Month vs Last Month)
  - চলতি বছর বনাম গত বছর (Current Year vs Last Year)
- **Category Spending Ranking**: Ranked expense distribution by category.
- **Growing Categories**: Identifies categories with significant period-over-period expansion.
- **Repeated Small Expenses**: Detects recurring micro-spending (e.g., tea, snacks, rickshaw fares) that compound over time.
- **Necessity Analysis**: Proportion of spending categorized as Essential (প্রয়োজনীয়) vs Discretionary (ইচ্ছাধীন).
- **Frugality Goal Progress**: Actionable progress bars towards monthly ceilings, category caps, or reduction targets.
- **Respectful Guidance**: Completely free of shame language (strictly avoids terms like "ব্যর্থ", "অপচয়কারী", or "দায়িত্বজ্ঞানহীন").

### D. আরও (More)
- **ব্যয়ের খাত**: Manage expense categories, icons, colors, and archive status.
- **আয়ের খাত**: Manage income categories.
- **খাত ও দ্রুত লেখা**: Configure shortcuts and category icon associations.
- **মিতব্যয়িতার লক্ষ্য**: Add and manage frugality goals and budget ceilings.
- **নোটিফিকেশন**: Configure daily logging reminders and monthly reviews via WorkManager.
- **ভাষা ও অঙ্ক**: Toggle between Bengali numerals (১, ২, ৩...) and English numerals (1, 2, 3...).
- **থিম ও রঙ**: Choose between the three curated color identities (কালি ও কাগজ, মধ্যরাতের নীল, বারগান্ডি ডায়েরি).
- **নিরাপত্তা ও গোপনীয়তা**: Configure optional 6-digit PIN, password, biometrics, auto-lock timeouts, screenshot blocking, and recovery key.
- **ডেটা Export**: Local export to CSV or JSON with opt-in inclusion of personal diary notes.
- **স্থানীয় ব্যাকআপ ও পুনরুদ্ধার**: Hardened envelope backup creation and safe transaction-based restore with pre-flight validation.
- **Cloud ও Backup**: Accurately reports offline status (না/সংযুক্ত নয়).
- **গোপনীয়তা ও লাইসেন্স**: Offline privacy statement and SIL Open Font License documentation for bundled fonts.
- **ডায়াগনস্টিকস**: Available exclusively in diagnostic builds (`IS_DIAGNOSTIC=true`) for crash log inspection and system integrity review.

---

## 3. Data Model & Input Guidelines

### Entry Structure
- **Amount**: Stored internally as 64-bit integer paisa (`amountPaisa`), preventing floating-point precision loss.
- **Type**: `EXPENSE` (ব্যয়) or `INCOME` (আয়).
- **Category**: Deterministically resolved or custom user-defined category.
- **Source**: Free-text descriptive field ("টাকার উৎস, ঐচ্ছিক", e.g. "বেতন থেকে", "পকেটের টাকা"). Never derives wallet balances.
- **Necessity Classification**:
  - প্রয়োজনীয় (Essential)
  - নিয়মিত (Regular)
  - ইচ্ছাধীন (Discretionary)
  - জরুরি (Emergency)
  - অনির্ধারিত (Unclassified)
- **Notes**: Optional personal diary reflections.
- **Timestamp**: Epoch milliseconds for date and time sorting.
