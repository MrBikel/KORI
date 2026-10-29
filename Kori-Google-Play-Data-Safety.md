# KORI (কড়ি) - Google Play Data Safety Inventory

**Package Name:** `com.aistudio.moneydiary.mndytr`  
**Version:** 1.0 (VersionCode 1)  
**Database Version:** 4 (Room SQLite)  
**Verification Date:** 2026-10-26  

---

## Factual Data Safety Declarations

| # | Item | Assessment / Finding | Status Label |
|---|---|---|---|
| 1 | **Data collected by the app (Developer Collection)** | No user financial, personal, or usage data is transmitted to or collected by the developer or developer servers. All transactions, accounts, categories, and debts entered by the user are stored strictly inside the device-private SQLite database (`money_diary.db`). | **Verified in source** |
| 2 | **Data shared with third parties** | No third-party data-sharing occurs. No user records or device identifiers are shared with external advertisers, analytics providers, or cloud servers. | **Verified in source** |
| 3 | **Data transmitted off-device** | None. The app operates in complete offline-first mode. No network transmissions of user financial records occur during normal execution. | **Verified in source** |
| 4 | **Data stored only locally** | All user entries (income, expense, transfer, party debts, line items, shopping lists, notes) are stored exclusively in the local Room SQLite database in device-protected storage (`/data/data/com.aistudio.moneydiary.mndytr/databases/`). | **Verified in source** |
| 5 | **Whether financial information is collected** | Financial information is entered and processed locally by the user on-device for personal bookkeeping purposes. It is **not collected** by the developer (Google Play Data Safety definition: data transferred off the device to the developer). | **Verified in source** |
| 6 | **Whether personal information is collected** | Names of parties/debtors or custom descriptions entered by the user remain strictly on-device. No personal identifiable information (PII) is collected by the developer. | **Verified in source** |
| 7 | **Whether files or documents are accessed** | Yes, strictly via the zero-permission Android Storage Access Framework (SAF) system file picker when the user explicitly triggers local JSON/CSV backup export, restore, or PDF report sharing. | **Verified in source** |
| 8 | **Contacts, SMS, Photos, Microphone, Camera, Location** | None accessed at runtime. Broad storage, camera, contacts, SMS, and fine location permissions are completely absent from the production manifest. | **Verified in source** |
| 9 | **Advertising SDKs** | No advertising SDKs (e.g., Google AdMob, Unity Ads, AppLovin) are integrated or present in the dependencies or source code. | **Not present** |
| 10 | **Analytics / Crash-reporting SDKs** | No third-party analytics (Google Analytics, Mixpanel) or remote crash reporters (Firebase Crashlytics, Sentry) are active. Crash reports are written strictly to local files in app-internal cache. | **Not present** |
| 11 | **Firebase status** | Dormant build configuration. Firebase AI and AppCheck dependencies exist in build catalog for future roadmap, but no active background initialization or cloud synchronization is enabled at runtime. | **Disabled** |
| 12 | **Google Drive status** | Disabled in production UI. The app explicitly displays "ক্লাউড সিঙ্ক অনুপলব্ধ" (Cloud sync unavailable), presents no false connection claims, and guides users to local offline backups. | **Disabled** |
| 13 | **Data encrypted in transit** | Not applicable as no data is transmitted off-device. (Cleartext network traffic is disabled). | **Verified in source** |
| 14 | **Local database encryption** | The local SQLite database (`money_diary.db`) relies on standard Android file-based encryption (FBE) provided by Android OS sandboxing; SQLCipher is not integrated. Clearly stated as unencrypted database on device filesystem. | **Verified in source** |
| 15 | **Exported backup files encrypted** | Unencrypted plain JSON / UTF-8 CSV. The app's backup documentation and UI transparently disclose that exported files are plain text backups that users should store safely. | **Verified in source** |
| 16 | **Account creation status** | No account creation or registration required. The app is ready to use immediately upon launch without email, phone, or password. | **Verified in source** |
| 17 | **Account deletion requirement** | Not applicable because no online accounts or server-side profiles exist. | **Requires Play Console decision** |
| 18 | **User data deletion controls** | The app provides full user control to reset/delete all data via the settings menu, as well as granular record deletion and replacement via backup restore. | **Verified in source** |
| 19 | **Permissions requested** | `android.permission.POST_NOTIFICATIONS` (Optional runtime permission for daily reminders and budget alerts). `USE_BIOMETRIC` / `USE_FINGERPRINT` for optional on-device app lock. No dangerous hardware or privacy permissions. | **Verified in source** |
| 20 | **Privacy statements vs code** | Fully consistent. In-app privacy dialog and policies honestly disclose offline-only processing and absence of cloud storage or external AI transmission. | **Verified in source** |

---

## Play Console Data Safety Form Guidance

1. **Does your app collect or share any of the required user data types?**  
   **Answer:** **No** (Because all financial and personal records are processed strictly on-device and never sent off the device to the developer or any third party).
2. **Is all of the user data collected by your app encrypted in transit?**  
   **Answer:** **Yes / Not applicable** (No data transmitted).
3. **Do you provide a way for users to request that their data is deleted?**  
   **Answer:** **Yes** (Users can wipe all data directly in app settings).
