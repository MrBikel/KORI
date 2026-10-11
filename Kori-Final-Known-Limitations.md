# কড়ি (Kori) Known Technical Limitations

1. **Cloud Synchronization Blocked**:
   - Absence of configured Firebase project credentials and Google OAuth client means multi-device cloud synchronization is currently not operational. Local backup/restore via file export is the primary data portability solution.

2. **Database Encryption Disclosure**:
   - Database storage utilizes standard SQLite via Room within the private Android application sandbox (`/data/data/com.aistudio.moneydiary.mndytr/databases/`). Full-disk/SQLCipher database-level encryption is not implemented; security relies on Android OS user isolation.

3. **Backup Checksum Nature**:
   - SHA-256 embedded in the backup envelope verifies data integrity and detects corruption or tampering, but does not provide cryptographic confidentiality. Unencrypted backup text files should be stored securely by the user.

4. **Physical Device Testing Environment**:
   - Automated testing baseline (117 test cases across 15 test suites) has been executed completely via JVM Robolectric test runner. Real physical hardware acceptance testing remains pending.
