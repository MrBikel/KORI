# Privacy Policy for Kori (কড়ি)

**Last Updated:** October 26, 2026  
**Application Name:** কড়ি (Kori)  
**Package:** `com.aistudio.moneydiary.mndytr`  
**Developer:** Mehedi Hasan  
**Contact:** `ektasobdohinbiswas@gmail.com`  

---

## 1. Introduction

Kori ("কড়ি", "we", "our") is an offline-first personal financial diary and record-keeping application designed to help individuals and small businesses track their daily income, expenses, debts, and budgets accurately and privately.

Your privacy is our core design priority. This policy outlines exactly how your information is handled.

---

## 2. Core Principle: 100% Local On-Device Processing

- **No Remote Servers:** Kori does not operate remote database servers, user tracking systems, or commercial analytics backends.
- **No Developer Collection:** When you record transactions, categories, debtor names, or notes, that data is stored solely inside your Android device's private application storage. It is never transmitted to the developer or any third party.
- **No Account Required:** You do not need to register, log in, or provide an email address or phone number to use Kori.

---

## 3. Financial Information and Sensitive Records

- Any financial data entered (such as amounts, accounts, payment methods, transaction descriptions, invoices, or debt records) remains on your phone.
- We do not have access to your bank accounts, card numbers, passwords, or transaction histories.
- Financial analysis, monthly summaries, budget tracking, and recommendations are computed entirely by deterministic mathematical algorithms running locally on your device's CPU.

---

## 4. Artificial Intelligence & Third-Party Processing

- Kori does **not** send your financial records, notes, or personal diary entries to external generative AI services or cloud LLM APIs at runtime.
- All categorization, search filtering, and financial calculations are executed on-device without remote data sharing.

---

## 5. Cloud Sync & Google Drive Transparency

- In the current release (v1.0), remote cloud synchronization and Google Drive auto-sync are **disabled**.
- The application clearly states this status in the UI. No simulated cloud connections or false claims of zero-knowledge cloud encryption are made.
- If you wish to back up your data, you can create a local structured backup file (JSON or CSV) using the in-app export tools and store it in your chosen personal storage location.

---

## 6. Backup Files and File System Access

- **Export and Restore:** When you export or restore a backup file, Kori uses the standard Android Storage Access Framework (SAF). The app only accesses the specific file you choose in the system picker.
- **Unencrypted Exports:** Please note that exported JSON and CSV files are saved in standard unencrypted text format so that you can view and verify them. You are responsible for keeping your exported backup files safe.

---

## 7. Device Permissions

Kori requests only minimal, necessary Android system permissions:
- `POST_NOTIFICATIONS`: Optional permission used only if you enable daily bookkeeping reminders or budget notifications.
- `USE_BIOMETRIC`: Optional permission used only if you enable app lock with your device's fingerprint or face unlock. Biometric authentication is handled directly by Android OS system APIs; Kori never has access to your biometric templates.

---

## 8. Data Retention and Deletion

- You retain complete ownership and control of your data at all times.
- To delete your data:
  1. You may delete individual transactions, debts, or accounts at any time inside the app.
  2. You can wipe all data at once via the app settings.
  3. Uninstalling the app permanently removes all database records stored on your device.

---

## 9. Changes to This Privacy Policy

Any future revisions will be published with updated version notes in the app and on the Google Play Store listing.

---

## 10. Contact Us

If you have questions about this privacy policy, please contact:  
**Developer:** Mehedi Hasan  
**Email:** `ektasobdohinbiswas@gmail.com`
