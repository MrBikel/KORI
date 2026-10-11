# কড়ি (Kori) Local Backup Format Specification

## 1. Specification Overview
- **Format Identifier**: `KORI_BACKUP_V1`
- **Format Version**: `1`
- **Integrity Protection**: SHA-256 Checksum on the payload string
- **Serialization**: JSON
- **Privacy Notice**: SHA-256 guarantees payload integrity, not cryptographic confidentiality.

---

## 2. Envelope Specification
The backup is structured as an envelope containing verification metadata and an isolated payload:

```json
{
  "format_identifier": "KORI_BACKUP_V1",
  "format_version": 1,
  "schema_version": 2,
  "app_version_code": 1,
  "app_version_name": "1.0",
  "created_timestamp": 1728374000000,
  "entry_count": 42,
  "category_count": 18,
  "goal_count": 2,
  "includes_notes": false,
  "preferences_present": true,
  "payload": "{...}",
  "checksum": "a1b2c3d4..."
}
```

### Excluded Sensitive Fields
The following fields are strictly excluded from backup payloads:
- `app_pin`
- `pin_hash`
- `password_hash`
- `salt`
- `recovery_key`
- `biometric_secret_id`
- `auth_token`
- Crash reports and stack traces

### Notes Inclusion Policy
- **Personal Diary Notes** (`notes`): **Excluded by default**.
- Included only when the user explicitly opts in via checkbox before export.
