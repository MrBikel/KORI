# কড়ি (Kori) Cloud & External Integration Readiness Report

## 1. Firebase Runtime Status
- **Current Status**: `Blocked by missing project configuration`
- **Details**:
  - The build environment contains no active Firebase Project credentials or provisioned Firestore instances.
  - No synthetic or mock `google-services.json` has been fabricated.
  - The project architecture is strictly offline-first (Room Database).
  - All data synchronization interfaces and sync state contracts (`LOCAL_ONLY`, `PENDING`, `SYNCING`, `SYNCED`, `FAILED`, `CONFLICT`) are defined in domain models for clean future cloud integration when valid Firebase credentials are provided.

---

## 2. Google Drive Backup Status
- **Current Status**: `Blocked by missing OAuth/project configuration`
- **Details**:
  - Google Drive cloud snapshot upload requires Google Identity OAuth client provisioning and user authorization.
  - In the absence of an authenticated client, local document picker and Android sharesheet export (`content://` URI) are fully functional as the primary backup channel.
  - Unencrypted backups are never uploaded to remote servers to ensure user financial privacy.

---

## 3. External AI Integration Status
- **Current Status**: `Not implemented` (Offline deterministic analysis engine active)
- **Details**:
  - The application relies entirely on deterministic, private on-device financial analysis (period comparisons, category trends, micro-expense detection, frugality progress tracking).
  - No speculative AI chat panels or external API calls are present.
