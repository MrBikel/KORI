package com.aistudio.moneydiary.mndytr.domain.model

import java.io.Serializable

/**
 * Versioned backup envelope for Kori local backups.
 */
data class BackupEnvelope(
    val formatIdentifier: String = "KORI_BACKUP_V1",
    val backupFormatVersion: Int = 1,
    val roomSchemaVersion: Int,
    val appVersionCode: Long,
    val appVersionName: String,
    val createdTimestamp: Long,
    val entryCount: Int,
    val categoryCount: Int,
    val goalCount: Int,
    val includesNotes: Boolean,
    val preferencesPresent: Boolean,
    val payload: String, // Minimized JSON string of data
    val integrityChecksum: String // SHA-256
) : Serializable

/**
 * Summary of a backup used for the Restore Preview UI.
 */
data class BackupSummary(
    val createdTimestamp: Long,
    val appVersionName: String,
    val roomSchemaVersion: Int,
    val entryCount: Int,
    val incomeCount: Int,
    val expenseCount: Int,
    val categoryCount: Int,
    val goalCount: Int,
    val includesNotes: Boolean,
    val dateRangeStart: Long?,
    val dateRangeEnd: Long?,
    val currentLocalRecordCount: Int,
    val rawJson: String // Kept for the confirm action
) : Serializable

/**
 * Result of a restore operation.
 */
sealed class RestoreResult {
    object Success : RestoreResult()
    data class Failure(val messageBn: String, val technicalDetails: String? = null) : RestoreResult()
}
