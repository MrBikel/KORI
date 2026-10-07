package com.aistudio.moneydiary.mndytr.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "accounts",
    indices = [
        Index("type"),
        Index("isDeleted"),
        Index("isArchived")
    ]
)
data class AccountEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val currency: String = "BDT",
    val initialBalancePaisa: Long = 0L,
    val colorHex: String = "#1B5E20",
    val iconName: String = "account_balance_wallet",
    val displayOrder: Int = 0,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentCategoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("parentCategoryId"),
        Index("isMicroExpense"),
        Index("isIncomeCategory"),
        Index("isDeleted")
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nameBn: String,
    val nameEn: String,
    val parentCategoryId: String? = null,
    val iconName: String,
    val colorHex: String,
    val isMicroExpense: Boolean = false,
    val isIncomeCategory: Boolean = false,
    val isSystemDefault: Boolean = false,
    val displayOrder: Int = 0,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "persons",
    indices = [
        Index("name"),
        Index("personType"),
        Index("isDeleted")
    ]
)
data class PersonEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val personType: String = "OTHER",
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "work_records",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["preferredAccountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("clientId"),
        Index("preferredAccountId"),
        Index("workStatus"),
        Index("paymentStatus"),
        Index("isDeleted")
    ]
)
data class WorkRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val clientId: String,
    val title: String,
    val description: String? = null,
    val agreedTotalPricePaisa: Long,
    val workStatus: String,
    val paymentStatus: String,
    val startDateEpochMs: Long? = null,
    val dueDateEpochMs: Long? = null,
    val completionDateEpochMs: Long? = null,
    val preferredAccountId: String? = null,
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "receivables",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = WorkRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["workId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("personId"),
        Index("workId"),
        Index("status"),
        Index("isDeleted")
    ]
)
data class ReceivableEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val workId: String? = null,
    val title: String,
    val totalAmountPaisa: Long,
    val dueDateEpochMs: Long? = null,
    val status: String,
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payables",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("personId"),
        Index("status"),
        Index("isDeleted")
    ]
)
data class PayableEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val title: String,
    val totalAmountPaisa: Long,
    val dueDateEpochMs: Long? = null,
    val status: String,
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceAccountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationAccountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = WorkRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["workId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ReceivableEntity::class,
            parentColumns = ["id"],
            childColumns = ["receivableId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PayableEntity::class,
            parentColumns = ["id"],
            childColumns = ["payableId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["originalLedgerEventId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentTransactionId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["reversedTransactionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("eventCode"),
        Index("eventKind"),
        Index("sourceAccountId"),
        Index("destinationAccountId"),
        Index("categoryId"),
        Index("personId"),
        Index("workId"),
        Index("receivableId"),
        Index("payableId"),
        Index("originalLedgerEventId"),
        Index("parentTransactionId"),
        Index("reversedTransactionId"),
        Index("transferGroupId"),
        Index("confirmationStatus"),
        Index("eventTimestampEpochMs"),
        Index("isDeleted")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val eventCode: String,
    val eventKind: String,
    val amountPaisa: Long,
    val currency: String = "BDT",
    val sourceAccountId: String? = null,
    val destinationAccountId: String? = null,
    val sourceDescription: String? = null,
    val necessity: String = "অনির্ধারিত",
    val categoryId: String? = null,
    val subcategoryId: String? = null,
    val personId: String? = null,
    val workId: String? = null,
    val receivableId: String? = null,
    val payableId: String? = null,
    val originalLedgerEventId: String? = null,
    val transferGroupId: String? = null,
    val parentTransactionId: String? = null,
    val reversedTransactionId: String? = null,
    val confirmationStatus: String = "CONFIRMED",
    val eventTimestampEpochMs: Long = System.currentTimeMillis(),
    val description: String,
    val notes: String? = null,
    val deviceId: String = "local_device",
    val isDeleted: Boolean = false,
    val deletedAtEpochMs: Long? = null,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "frugality_goals",
    indices = [
        Index("goalType"),
        Index("categoryId"),
        Index("status"),
        Index("isDeleted")
    ]
)
data class FrugalityGoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val goalType: String,
    val categoryId: String? = null,
    val targetAmountPaisa: Long,
    val targetReductionPercentage: Int? = null,
    val periodType: String = "MONTHLY",
    val status: String = "ACTIVE",
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("accountId"),
        Index("status"),
        Index("isDeleted")
    ]
)
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val title: String,
    val targetAmountPaisa: Long,
    val targetDateEpochMs: Long? = null,
    val colorHex: String = "#0288D1",
    val iconName: String = "savings",
    val status: String,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "frequent_shortcuts",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["defaultCategoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["defaultAccountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("defaultCategoryId"),
        Index("defaultAccountId"),
        Index("isPinned"),
        Index("displayOrder")
    ]
)
data class FrequentShortcutEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val labelBn: String,
    val defaultAmountPaisa: Long,
    val eventCode: String = "TX_02_EXPENSE",
    val defaultCategoryId: String,
    val defaultAccountId: String,
    val iconName: String = "local_cafe",
    val usageCount: Long = 0L,
    val displayOrder: Int = 0,
    val isPinned: Boolean = true,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = WorkRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["workId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("transactionId"),
        Index("workId"),
        Index("isDeleted")
    ]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val transactionId: String? = null,
    val workId: String? = null,
    val fileName: String,
    val fileMimeType: String,
    val fileSizeBytes: Long,
    val localRelativePath: String,
    val sha256Hash: String,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val revisionNumber: Long = 1L,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "backup_jobs",
    indices = [
        Index("status"),
        Index("createdAtEpochMs")
    ]
)
data class BackupJobEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val status: String = "PENDING",
    val errorMessage: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "backup_metadata",
    indices = [
        Index("backupTimestampEpochMs"),
        Index("status")
    ]
)
data class BackupMetadataEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val backupTimestampEpochMs: Long,
    val driveFileId: String? = null,
    val fileName: String,
    val fileSizeBytes: Long,
    val checksumSha256: String,
    val encryptionAlgorithm: String = "AES-256-GCM",
    val keyDerivationSaltHex: String,
    val recordCountsJson: String,
    val ledgerTotalsJson: String,
    val status: String = "LOCAL_ONLY",
    val deviceId: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [
        Index("entityName", "entityId"),
        Index("timestampEpochMs")
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0L,
    val entityName: String,
    val entityId: String,
    val actionType: String,
    val previousStateJson: String? = null,
    val newStateJson: String,
    val changeReason: String? = null,
    val deviceId: String = "local_device",
    val timestampEpochMs: Long = System.currentTimeMillis()
)
