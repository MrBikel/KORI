# কড়ি (Kori) Physical-Device Acceptance Checklist

This checklist defines the physical handset acceptance testing matrix for validating release candidates on real Android hardware across Android 7.0 (API 24) to Android 15 (API 35+).

---

## 1. Installation & Cold Boot
- [ ] **Clean Install**: Install `Kori-Final-Release-Candidate-Debug.apk` on a clean device.
- [ ] **Upgrade Install**: Install on top of an existing database version without data loss.
- [ ] **Diagnostic Launch**: Install `Kori-Final-Release-Candidate-Diagnostic.apk` and verify diagnostic options in More screen.
- [ ] **Launcher Icon**: Custom adaptive coin/shell launcher icon displays crisply on launcher grid.

---

## 2. Empty-First & Diary Core CUJ
- [ ] **Empty First State**: Displays "আজ এখনো কোনো হিসাব লেখা হয়নি।" and dual action buttons.
- [ ] **Record Expense**: ৳450 for ওষুধ, source বেতন থেকে, category medicine icon auto-suggested.
- [ ] **Record Income**: ৳500 for অনলাইন কাজ, source গ্রাহকের দেওয়া টাকা.
- [ ] **Record Small Expense**: ৳10 for চা, source পকেটের টাকা.
- [ ] **Verify Calculations**: Today income ৳500, expense ৳460, net change +৳40.
- [ ] **Diary View**: Chronological grouping by day, date headers, description and source search.

---

## 3. Visual & Typography Acceptance
- [ ] **All Three Themes**: Toggle between কালি ও কাগজ, মধ্যরাতের নীল, and বারগান্ডি ডায়েরি in Light and Dark modes.
- [ ] **High Contrast**: Verify no illegible text or low-contrast blending.
- [ ] **Bengali Rendering**: Verify correct Bengali ligature shaping in Hind Siliguri and Noto Serif Bengali.
- [ ] **Font Scaling**: Verify UI layout stability at 1.5x and 2.0x system font scale.

---

## 4. Security & Privacy
- [ ] **PIN Setup**: Setup 6-digit PIN; verify sequential PINs (123456) are warned.
- [ ] **Biometric Prompt**: Unlock via fingerprint or face recognition.
- [ ] **Recent Apps Privacy**: App screen is blanked or hidden in Android recent apps switcher.
- [ ] **Lockout Delay**: Verify 30-second penalty after 5 consecutive incorrect PIN attempts.

---

## 5. Local Backup & Restore
- [ ] **Create Backup**: Export backup code / file; verify exclusion of plaintext secrets.
- [ ] **Corrupt Backup Rejection**: Modify one character of backup text; verify checksum mismatch rejection.
- [ ] **Full Restore**: Restore valid backup; verify record counts, categories, and totals match perfectly.

---

## 6. Execution Status
- **Source / Robolectric Automated**: 100% Passed (117/117 tests)
- **Real-Device Physical Execution**: Pending availability of physical test hardware
