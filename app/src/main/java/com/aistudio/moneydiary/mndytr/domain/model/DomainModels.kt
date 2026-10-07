package com.aistudio.moneydiary.mndytr.domain.model

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
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

data class Category(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val parentCategoryId: String? = null,
    val iconName: String,
    val colorHex: String,
    val isMicroExpense: Boolean = false,
    val isIncomeCategory: Boolean = false,
    val isSystemDefault: Boolean = false,
    val isDeleted: Boolean = false,
    val displayOrder: Int = 0,
    val revisionNumber: Long = 1L,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class Person(
    val id: String,
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

data class WorkRecord(
    val id: String,
    val clientId: String,
    val title: String,
    val description: String? = null,
    val agreedTotalPricePaisa: Long,
    val workStatus: WorkStatus = WorkStatus.CONFIRMED,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
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

data class LedgerEvent(
    val id: String,
    val eventCode: LedgerEventCode,
    val eventKind: LedgerEventKind = eventCode.kind,
    val amountPaisa: Long,
    val currency: String = "BDT",
    val sourceAccountId: String? = null,
    val destinationAccountId: String? = null,
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
    val confirmationStatus: ConfirmationStatus = ConfirmationStatus.CONFIRMED,
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

data class Receivable(
    val id: String,
    val personId: String,
    val workId: String? = null,
    val title: String,
    val totalAmountPaisa: Long,
    val dueDateEpochMs: Long? = null,
    val status: ReceivableStatus = ReceivableStatus.ACTIVE,
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class Payable(
    val id: String,
    val personId: String,
    val title: String,
    val totalAmountPaisa: Long,
    val dueDateEpochMs: Long? = null,
    val status: PayableStatus = PayableStatus.ACTIVE,
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class SavingsGoal(
    val id: String,
    val accountId: String,
    val title: String,
    val targetAmountPaisa: Long,
    val targetDateEpochMs: Long? = null,
    val colorHex: String = "#0288D1",
    val iconName: String = "savings",
    val status: SavingsGoalStatus = SavingsGoalStatus.IN_PROGRESS,
    val isDeleted: Boolean = false,
    val revisionNumber: Long = 1L,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class FrequentShortcut(
    val id: String,
    val labelBn: String,
    val defaultAmountPaisa: Long,
    val eventCode: LedgerEventCode = LedgerEventCode.TX_02_EXPENSE,
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

data class Attachment(
    val id: String,
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

data class BackupJob(
    val id: String,
    val status: BackupJobStatus = BackupJobStatus.PENDING,
    val errorMessage: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class BackupMetadata(
    val id: String,
    val backupTimestampEpochMs: Long,
    val driveFileId: String? = null,
    val fileName: String,
    val fileSizeBytes: Long,
    val checksumSha256: String,
    val encryptionAlgorithm: String = "AES-256-GCM",
    val keyDerivationSaltHex: String,
    val recordCountsJson: String,
    val ledgerTotalsJson: String,
    val status: BackupStatus = BackupStatus.LOCAL_ONLY,
    val deviceId: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

data class AppSetting(
    val key: String,
    val value: String,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class AuditLog(
    val logId: Long = 0L,
    val entityName: String,
    val entityId: String,
    val actionType: AuditActionType,
    val previousStateJson: String? = null,
    val newStateJson: String,
    val changeReason: String? = null,
    val deviceId: String = "local_device",
    val timestampEpochMs: Long = System.currentTimeMillis()
)

// Projections & Reporting Summaries
data class AccountBalanceProjection(
    val accountId: String,
    val accountName: String,
    val accountType: AccountType,
    val initialBalancePaisa: Long,
    val totalInflowPaisa: Long,
    val totalOutflowPaisa: Long,
    val currentBalancePaisa: Long
)

data class PeriodFinancialSummary(
    val startEpochMs: Long,
    val endEpochMs: Long,
    val earnedIncomePaisa: Long,
    val incurredExpensePaisa: Long,
    val unearnedAdvancePaisa: Long,
    val netPeriodSavingsPaisa: Long,
    val totalCashInflowPaisa: Long,
    val totalCashOutflowPaisa: Long
)

data class WorkFinancialProjection(
    val workId: String,
    val agreedTotalPricePaisa: Long,
    val advanceReceivedPaisa: Long,
    val advanceEarnedPaisa: Long,
    val advanceReturnedPaisa: Long,
    val progressPaymentsCollectedPaisa: Long,
    val totalReceivedCashPaisa: Long,
    val totalEarnedRevenuePaisa: Long,
    val expectedRemainingPaymentPaisa: Long,
    val recognizedReceivablePaisa: Long
)

data class RefundStatus(
    val originalLedgerEventId: String,
    val originalAmountPaisa: Long,
    val totalRefundedPaisa: Long,
    val remainingRefundablePaisa: Long
)

// --- Stage 1 Diary-First Domain Models ---

enum class NecessityClassification(val banglaLabel: String) {
    ESSENTIAL("প্রয়োজনীয়"),
    REGULAR("নিয়মিত"),
    DISCRETIONARY("ইচ্ছাধীন"),
    EMERGENCY("জরুরি"),
    UNSPECIFIED("অনির্ধারিত");

    companion object {
        fun fromLabel(label: String?): NecessityClassification {
            if (label == null) return UNSPECIFIED
            return values().firstOrNull { it.banglaLabel == label || it.name.equals(label, ignoreCase = true) } ?: UNSPECIFIED
        }
    }
}

enum class DiaryEntryType(val banglaLabel: String) {
    INCOME("আয়"),
    EXPENSE("ব্যয়")
}

data class DiaryEntry(
    val id: String,
    val type: DiaryEntryType,
    val amountPaisa: Long,
    val categoryId: String? = null,
    val categoryNameBn: String = "অন্যান্য",
    val categoryIconKey: String = "category",
    val sourceDescription: String? = null,
    val occurrenceDateEpochMs: Long = System.currentTimeMillis(),
    val description: String,
    val notes: String? = null,
    val necessity: NecessityClassification = NecessityClassification.UNSPECIFIED,
    val confirmationStatus: ConfirmationStatus = ConfirmationStatus.CONFIRMED,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class DayGroupedDiary(
    val dateEpochMs: Long,
    val dateLabelBn: String,
    val totalIncomePaisa: Long,
    val totalExpensePaisa: Long,
    val netChangePaisa: Long,
    val entries: List<DiaryEntry>
)

data class FrugalityGoal(
    val id: String,
    val title: String,
    val goalType: String,
    val categoryId: String? = null,
    val categoryNameBn: String? = null,
    val targetAmountPaisa: Long,
    val targetReductionPercentage: Int? = null,
    val periodType: String = "MONTHLY",
    val status: String = "ACTIVE",
    val notes: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class FrugalityGoalProgress(
    val goal: FrugalityGoal,
    val currentSpendingPaisa: Long,
    val remainingAllowancePaisa: Long,
    val percentageUsed: Double,
    val isExceeded: Boolean,
    val guidanceMessageBn: String
)

data class CategoryShare(
    val categoryNameBn: String,
    val iconKey: String,
    val amountPaisa: Long,
    val percentageOfTotal: Double
)

data class CategoryDifferenceContribution(
    val categoryNameBn: String,
    val differencePaisa: Long,
    val isIncrease: Boolean
)

data class PeriodComparison(
    val currentPeriodLabel: String,
    val previousPeriodLabel: String,
    val currentExpensePaisa: Long,
    val previousExpensePaisa: Long,
    val currentIncomePaisa: Long = 0L,
    val previousIncomePaisa: Long = 0L,
    val absoluteDifferencePaisa: Long,
    val isExpenseIncrease: Boolean,
    val percentageDifference: Double?,
    val topContributingCategories: List<CategoryDifferenceContribution>,
    val narrative: String
)

