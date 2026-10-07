# Kori (কড়ি) - Stage 0.1 Manual Runtime Test Checklist

**Target Build**: `Kori-Stage-0.1-RuntimeTest-Debug.apk`  
**Package Name**: `com.aistudio.moneydiary.mndytr`  
**Purpose**: Verify real Android device runtime stability and submission persistence prior to authorizing Stage 1 Redesign.

---

## Manual Test Cases

### A. Installation & Initial Launch
- [ ] Install `Kori-Stage-0.1-RuntimeTest-Debug.apk` on a physical device or streaming emulator.
- [ ] Launch application from home screen icon ("কড়ি").
- [ ] Verify app launches smoothly without immediate crash.

### B. First-Launch Setup
- [ ] First launch setup screen displays in Bengali.
- [ ] Select or accept initial account configuration (e.g. নগদ, Bank, bKash).
- [ ] Tap **শুরু করুন** / Complete Setup.
- [ ] Main dashboard loads with summary cards.

### C. Quick Expense Submission
- [ ] Tap **দৈনন্দিন খরচ** (Quick Expense) button.
- [ ] Enter Amount: `১০` (৳10).
- [ ] Select Category: **চা** (or type custom category).
- [ ] Free-text Source: `পকেটের টাকা`.
- [ ] Tap **সংরক্ষণ করুন** (Submit/Save).
- [ ] **Verify**: No crash occurs, dialog dismisses, entry appears in transaction history, and Today's Expense updates to `৳১০`.

### D. Quick Income Submission
- [ ] Tap **সরাসরি আয়** (Quick Income) button.
- [ ] Enter Amount: ` ৫০০` (৳500).
- [ ] Select Category: **অনলাইন কাজ**.
- [ ] Free-text Source: `গ্রাহকের দেওয়া টাকা`.
- [ ] Tap **সংরক্ষণ করুন** (Submit/Save).
- [ ] **Verify**: No crash occurs, entry appears in transaction history, and Today's Income updates to `৳৫০০`.

### E. Additional Expense Entry
- [ ] Tap **দৈনন্দিন খরচ** (Quick Expense) button again.
- [ ] Enter Amount: `৪৫০` (৳450).
- [ ] Select Category: **ওষুধ**.
- [ ] Free-text Source: `বেতন থেকে`.
- [ ] Tap **সংরক্ষণ করুন** (Submit/Save).
- [ ] **Verify**: No crash occurs, entry appears in history.

### F. Restart & Persistence Verification
- [ ] Close the app completely from Recent Apps / Task Manager.
- [ ] Relaunch the app.
- [ ] **Verify**:
  - All 3 recorded transactions persist in the history list.
  - Today's Income remains `৳৫০০`.
  - Today's Expense remains `৳৪৬০` (৳10 + ৳450).
  - Net Balance reflects accurate calculated total.

---

## Failure Evidence Capture Guidelines
If any submission crashes during testing, please capture and record:
1. **Exact Form**: (e.g. Quick Expense / Quick Income / Work Advance)
2. **Entered Values**: Amount, Category, Source, Description
3. **Device Details**: Android OS Version & Device Model
4. **Time of Crash**: Exact local timestamp
5. **Raw Logcat Output** (if connected via USB/ADB):
   - FATAL EXCEPTION stack trace
   - Process name: `com.aistudio.moneydiary.mndytr`
