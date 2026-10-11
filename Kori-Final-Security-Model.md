# কড়ি (Kori) Final Security & Privacy Model

## 1. Core Security Guarantees
- **Offline-First Isolation**: All user financial records are stored strictly in on-device storage. No unauthenticated telemetry or background sync to third-party endpoints occurs.
- **Zero Plaintext Credentials**: PINs, passwords, and recovery keys are never written to plaintext storage, preferences, log files, diagnostics, or exported backups.
- **Multi-Round Key Derivation**: Credentials use 5,000 rounds of salted SHA-256 derivation with unique per-installation cryptographic salts (`SecureRandom`).

---

## 2. Authentication & Access Control

### App Lock Modes
1. **Disabled (স্বাভাবিক/বন্ধ)**: Default state for zero-friction access.
2. **6-Digit PIN**: Verified with sequential and repeating number pattern checks (rejects obvious codes like `123456`, `111111`).
3. **Password**: Freeform alphanumeric passphrase.
4. **Biometric Unlock**: Uses Android Jetpack `BiometricPrompt` (Class 3 / Class 2 biometrics) with seamless PIN/password fallback.

### Auto-Lock Timeouts
Configurable periods before app re-engagement locks:
- Immediate (0 seconds)
- 30 seconds
- 1 minute (Default upon enabling lock)
- 5 minutes
- 15 minutes

### Brute-Force Defense
Progressive lockout delays triggered upon successive invalid attempts:
- 5 failures: 30-second lockout
- 10 failures: 5-minute lockout

---

## 3. Screen & App-Switcher Privacy
- **Recent Apps Privacy**: Obscures app window preview in Android's recent apps switcher (`FLAG_SECURE` or blank masking surface).
- **Screenshot Protection**: Configurable option to block screenshots and screen recordings.
- **Notification Privacy**: Configurable option to suppress financial amounts in notification summaries.

---

## 4. Accurate Status Disclosures
As presented transparently in the Security settings screen:
- **App Lock (অ্যাপ লক)**: সক্রিয় (Active) or বন্ধ (Disabled) based on user configuration.
- **Database Encryption (ডেটাবেস এনক্রিপশন)**: পূর্ণভাবে যুক্ত নয় (Plain SQLite/Room on device sandbox, protected by Android OS sandbox permissions).
- **Cloud Recovery (ক্লাউড রিকভারি)**: সংযুক্ত নয় (Offline local recovery key mechanism only).
