package com.aistudio.moneydiary.mndytr.ui.state

import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.FrequentShortcutEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.PersonEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.WorkRecordEntity
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.model.DayGroupedDiary
import com.aistudio.moneydiary.mndytr.domain.model.FrugalityGoal
import com.aistudio.moneydiary.mndytr.domain.model.FrugalityGoalProgress
import com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification
import com.aistudio.moneydiary.mndytr.domain.model.WorkFinancialProjection
import com.aistudio.moneydiary.mndytr.domain.service.AnalysisInsights

// --- Stage 1 Diary-First UI Models ---

data class TodaySummary(
    val todayIncomePaisa: Long = 0L,
    val todayExpensePaisa: Long = 0L,
    val todayNetChangePaisa: Long = 0L,
    val hasEntriesToday: Boolean = false
)

enum class AppNavTab(val labelBn: String) {
    TODAY("আজ"),
    DIARY("ডায়েরি"),
    ANALYSIS("বিশ্লেষণ"),
    MORE("আরও");

    companion object {
        val DASHBOARD = TODAY
        val TRANSACTIONS = DIARY
        val ACCOUNTS = MORE
        val WORKS = MORE
    }
}

sealed class ActiveDialog {
    object None : ActiveDialog()
    object AddExpense : ActiveDialog()
    object AddIncome : ActiveDialog()
    data class EntryDetail(val entry: DiaryEntry) : ActiveDialog()
    data class EditEntry(val entry: DiaryEntry) : ActiveDialog()
    data class DeleteEntryConfirm(val entryId: String, val description: String) : ActiveDialog()
    object AddFrugalityGoal : ActiveDialog()
    object ManageCategories : ActiveDialog()
    object ReminderSettings : ActiveDialog()
    object ThemeSelection : ActiveDialog()
    object SecuritySettings : ActiveDialog()
    object SetupPin : ActiveDialog()
    object SetupPassword : ActiveDialog()
    object VerifyRecoveryKey : ActiveDialog()
    object OpenRecoveryKeyShow : ActiveDialog()
    object OpenSourceLicenses : ActiveDialog()
    object CrashRecovery : ActiveDialog()
    object Diagnostics : ActiveDialog()
    data class RestorePreview(val summary: com.aistudio.moneydiary.mndytr.domain.model.BackupSummary) : ActiveDialog()
    object BackupOptions : ActiveDialog()

    // Backward compatibility aliases for existing test suite
    object AddWorkAdvance : ActiveDialog()
    object Transfer : ActiveDialog()
    object SavingsAllocation : ActiveDialog()
    object SmartEntry : ActiveDialog()
    data class OpeningBalance(val accountId: String? = null) : ActiveDialog()
    object AddAccount : ActiveDialog()
    data class SettleWork(val workId: String) : ActiveDialog()
    data class ReverseTransaction(val transactionId: String, val description: String) : ActiveDialog()
    data class TransactionDetail(val item: TransactionDisplayItem) : ActiveDialog()
}

data class AccountWithBalance(
    val account: AccountEntity,
    val currentBalancePaisa: Long
)

data class TransactionDisplayItem(
    val id: String,
    val eventCode: String,
    val amountPaisa: Long,
    val sourceAccountId: String? = null,
    val destinationAccountId: String? = null,
    val sourceAccountName: String? = null,
    val destinationAccountName: String? = null,
    val categoryId: String? = null,
    val categoryNameBn: String? = null,
    val categoryIcon: String? = null,
    val categoryColorHex: String? = null,
    val personId: String? = null,
    val personName: String? = null,
    val workId: String? = null,
    val workTitle: String? = null,
    val description: String,
    val notes: String? = null,
    val timestampEpochMs: Long,
    val confirmationStatus: String,
    val originalLedgerEventId: String? = null,
    val reversedTransactionId: String? = null,
    val isDraft: Boolean = false,
    val isReversible: Boolean = true
)

data class WorkDisplayItem(
    val work: WorkRecordEntity,
    val clientName: String,
    val projection: WorkFinancialProjection
)

data class DashboardSummary(
    val totalLiquidCashPaisa: Long = 0L,
    val todayExpensePaisa: Long = 0L,
    val monthExpensePaisa: Long = 0L,
    val monthEarnedIncomePaisa: Long = 0L,
    val unearnedAdvancePaisa: Long = 0L,
    val activeReceivablePaisa: Long = 0L,
    val activePayablePaisa: Long = 0L
)

data class DiaryUiState(
    val isLoading: Boolean = true,
    val isSetupCompleted: Boolean = true,
    val useBengaliDigits: Boolean = true,
    val selectedTab: AppNavTab = AppNavTab.TODAY,
    val activeDialog: ActiveDialog = ActiveDialog.None,

    // Diary-First State
    val todaySummary: TodaySummary = TodaySummary(),
    val todayEntries: List<DiaryEntry> = emptyList(),
    val allDiaryEntries: List<DiaryEntry> = emptyList(),
    val dayGroupedEntries: List<DayGroupedDiary> = emptyList(),
    val filteredDayGroupedEntries: List<DayGroupedDiary> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: DiaryEntryType? = null,
    val selectedCategoryIdFilter: String? = null,
    val selectedNecessityFilter: NecessityClassification? = null,
    val selectedDateEpochMs: Long? = null,

    // Analysis State
    val analysisInsights: AnalysisInsights? = null,
    val todayHighlightInsight: String? = null,

    // Frugality Goals & Settings
    val frugalityGoals: List<FrugalityGoal> = emptyList(),
    val frugalityGoalProgresses: List<FrugalityGoalProgress> = emptyList(),
    val isDailyReminderEnabled: Boolean = false,
    val dailyReminderTime: String = "21:00",
    val diaryName: String = "কড়ি",
    val themeMode: String = "SYSTEM",
    val colorIdentity: com.aistudio.moneydiary.mndytr.ui.theme.KoriColorIdentity = com.aistudio.moneydiary.mndytr.ui.theme.KoriColorIdentity.INK_AND_PAPER,

    // Security & App Lock State
    val securityConfig: com.aistudio.moneydiary.mndytr.domain.service.AppSecurityConfig = com.aistudio.moneydiary.mndytr.domain.service.AppSecurityConfig(),
    val isAppLocked: Boolean = false,
    val latestGeneratedRecoveryKey: String? = null,

    // Categories
    val categories: List<CategoryEntity> = emptyList(),
    val activeCategories: List<CategoryEntity> = emptyList(),

    // Preserved for rollback safety & legacy tests
    val dashboardSummary: DashboardSummary = DashboardSummary(),
    val accountsWithBalance: List<AccountWithBalance> = emptyList(),
    val frequentShortcuts: List<FrequentShortcutEntity> = emptyList(),
    val recentTransactions: List<TransactionDisplayItem> = emptyList(),
    val allTransactions: List<TransactionDisplayItem> = emptyList(),
    val persons: List<PersonEntity> = emptyList(),
    val activeWorks: List<WorkDisplayItem> = emptyList(),
    val snackbarMessage: String? = null,
    val lastAddedEventId: String? = null
)
