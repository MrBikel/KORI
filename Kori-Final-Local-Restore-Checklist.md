# কড়ি (Kori) Local Restore Device Checklist & Safety Procedure

## 1. Safety Restoration Protocol (20-Step Verification Workflow)
Before any database write occurs, the restore engine executes the following verification stages:

1. **Receive Backup Text/File**: Accept content via file selector, sharesheet, or clipboard.
2. **Size Guard**: Verify payload size is below the 5MB text-based threshold.
3. **Format Header Check**: Verify `format_identifier == "KORI_BACKUP_V1"`.
4. **Format Version Compatibility**: Verify `format_version <= CURRENT_BACKUP_FORMAT_VERSION`.
5. **SHA-256 Checksum Calculation**: Calculate SHA-256 over raw `payload` string.
6. **Integrity Match**: Ensure calculated hash strictly matches envelope `checksum`.
7. **JSON Parse in Isolation**: Parse payload into standalone in-memory structures without database side-effects.
8. **Schema Version Compatibility**: Verify embedded schema version is compatible with current Room schema.
9. **Record Validation**: Validate data types, non-null constraints, and timestamp validity.
10. **Duplicate ID Check**: Verify no duplicate record UUIDs exist within the backup payload.
11. **Foreign Key Integrity**: Check category IDs reference valid category entities in the payload.
12. **Calculate Totals**: Compute total income, total expense, and net balance from payload.
13. **Generate User Preview**: Display record count, date span, and totals to the user for explicit confirmation.
14. **Create Pre-Restore Safety Snapshot**: Back up current active database state before initiating modification.
15. **User Explicit Confirmation**: User taps "রিস্টোর নিশ্চিত করুন".
16. **Atomic Transaction Execution**: Execute database insert and replace operations inside a Room `@Transaction`.
17. **Integrity Check**: Run SQLite `PRAGMA integrity_check`.
18. **Recompute State**: Trigger `refreshNow()` to update all UI StateFlows.
19. **Notify Success**: Present Bengali success confirmation dialog.
20. **Automatic Rollback**: Any error during parsing or transaction execution immediately triggers complete rollback with current database preservation.

---

## 2. Physical Device Testing Status
- **Source / Robolectric Automated Testing**: 100% Passed (Validated in `LocalBackupHardeningTest.kt` and `BackupHardeningTest.kt`).
- **Real-Device Backup Generation**: Pending physical hardware availability.
- **Real-Device Restoration**: Pending physical hardware availability.
- **Cross-Device Migration**: Pending physical hardware availability.
