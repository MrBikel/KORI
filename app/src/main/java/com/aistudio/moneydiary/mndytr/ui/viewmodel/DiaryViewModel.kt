package com.aistudio.moneydiary.mndytr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.AppSettingEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.FrequentShortcutEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.FrugalityGoalEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import com.aistudio.moneydiary.mndytr.domain.model.*
import com.aistudio.moneydiary.mndytr.domain.service.AnalysisInsights
import com.aistudio.moneydiary.mndytr.domain.service.CategoryIconResolver
import com.aistudio.moneydiary.mndytr.domain.service.DiagnosticService
import com.aistudio.moneydiary.mndytr.domain.service.DiaryAnalysisEngine
import com.aistudio.moneydiary.mndytr.domain.service.FinancialCalculationEngine
import com.aistudio.moneydiary.mndytr.domain.service.FinancialResult
import com.aistudio.moneydiary.mndytr.domain.service.FinancialService
import com.aistudio.moneydiary.mndytr.ui.screens.SetupAccountDraft
import com.aistudio.moneydiary.mndytr.ui.state.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class DiaryViewModel(
    private val db: AppDatabase,
    private val financialService: FinancialService = FinancialService(db),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val accountDao = db.accountDao()
    private val categoryDao = db.categoryDao()
    private val ledgerEventDao = db.ledgerEventDao()
    private val appSettingDao = db.appSettingDao()
    private val personDao = db.personDao()
    private val workDao = db.workDao()
    private val shortcutDao = db.frequentShortcutDao()
    private val frugalityGoalDao = db.frugalityGoalDao()

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _typeFilter = MutableStateFlow<DiaryEntryType?>(null)
    private val _categoryFilter = MutableStateFlow<String?>(null)
    private val _necessityFilter = MutableStateFlow<NecessityClassification?>(null)
    private val _dateFilter = MutableStateFlow<Long?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(ioDispatcher) {
            refreshNow()
            combine(
                ledgerEventDao.observeLedgerHistory(),
                categoryDao.observeAll(),
                frugalityGoalDao.observeAllActive(),
                _searchQuery,
                _typeFilter,
                _categoryFilter,
                _necessityFilter,
                _dateFilter
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val allEvents = values[0] as List<TransactionEntity>
                @Suppress("UNCHECKED_CAST")
                val allCategories = values[1] as List<CategoryEntity>
                @Suppress("UNCHECKED_CAST")
                val activeGoals = values[2] as List<FrugalityGoalEntity>
                val query = values[3] as String
                val typeF = values[4] as DiaryEntryType?
                val catF = values[5] as String?
                val necF = values[6] as NecessityClassification?
                val dateF = values[7] as Long?

                val activeCategories = allCategories.filter { !it.isArchived }
                val accounts = accountDao.getAllActive()
                val settings = appSettingDao.getAll().associate { it.key to it.value }

                processData(
                    allEvents = allEvents,
                    allCategories = allCategories,
                    activeCategories = activeCategories,
                    activeGoals = activeGoals,
                    accounts = accounts,
                    settings = settings,
                    query = query,
                    typeF = typeF,
                    catF = catF,
                    necF = necF,
                    dateF = dateF
                )
            }.collect()
        }
    }

    suspend fun refreshNow() {
        val allEvents = ledgerEventDao.getAllNonDeleted()
        val allCategories = categoryDao.getAll()
        val activeCategories = categoryDao.getAllActive()
        val activeGoals = frugalityGoalDao.getAllActive()
        val accounts = accountDao.getAllActive()
        val settings = appSettingDao.getAll().associate { it.key to it.value }

        processData(
            allEvents = allEvents,
            allCategories = allCategories,
            activeCategories = activeCategories,
            activeGoals = activeGoals,
            accounts = accounts,
            settings = settings,
            query = _searchQuery.value,
            typeF = _typeFilter.value,
            catF = _categoryFilter.value,
            necF = _necessityFilter.value,
            dateF = _dateFilter.value
        )
    }

    private fun processData(
        allEvents: List<TransactionEntity>,
        allCategories: List<CategoryEntity>,
        activeCategories: List<CategoryEntity>,
        activeGoals: List<FrugalityGoalEntity>,
        accounts: List<com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity>,
        settings: Map<String, String>,
        query: String,
        typeF: DiaryEntryType?,
        catF: String?,
        necF: NecessityClassification?,
        dateF: Long?
    ) {
        val catMap = activeCategories.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }

        val isSetupDone = settings["first_launch_completed"]?.toBooleanStrictOrNull() ?: false
        val useBnDigits = settings["numeral_system"] != "en"
        val reminderEnabled = settings["reminder_daily_enabled"]?.toBooleanStrictOrNull() ?: false
        val reminderTime = settings["reminder_daily_time"] ?: "21:00"
        val diaryName = settings["diary_name"] ?: "কড়ি"
        val themeMode = settings["theme_mode"] ?: "SYSTEM"
        val colorIdentityId = settings["color_identity"] ?: com.aistudio.moneydiary.mndytr.ui.theme.KoriColorIdentity.INK_AND_PAPER.name
        val colorIdentity = try {
            com.aistudio.moneydiary.mndytr.ui.theme.KoriColorIdentity.valueOf(colorIdentityId)
        } catch (_: Exception) {
            com.aistudio.moneydiary.mndytr.ui.theme.KoriColorIdentity.INK_AND_PAPER
        }

        // Transform TransactionEntity into DiaryEntry
        val nonDeletedEvents = allEvents.filter { !it.isDeleted && it.confirmationStatus != ConfirmationStatus.REVERSED.name }
        val diaryEntries = nonDeletedEvents.map { event ->
            val cat = event.categoryId?.let { catMap[it] }
            val type = if (event.eventCode == LedgerEventCode.TX_01_INCOME.name ||
                event.eventCode == LedgerEventCode.TX_03_ADVANCE_RECEIVED.name ||
                event.eventCode == LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name ||
                event.eventCode == LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION.name
            ) {
                DiaryEntryType.INCOME
            } else {
                DiaryEntryType.EXPENSE
            }

            val sourceText = event.sourceDescription
                ?: event.sourceAccountId?.let { accMap[it]?.name }
                ?: event.destinationAccountId?.let { accMap[it]?.name }

            val categoryName = cat?.nameBn ?: "অন্যান্য"
            val iconKey = cat?.iconName ?: CategoryIconResolver.suggestIconKey(event.description)

            DiaryEntry(
                id = event.id,
                type = type,
                amountPaisa = event.amountPaisa,
                categoryId = event.categoryId,
                categoryNameBn = categoryName,
                categoryIconKey = iconKey,
                sourceDescription = sourceText,
                occurrenceDateEpochMs = event.eventTimestampEpochMs,
                description = event.description,
                notes = event.notes,
                necessity = NecessityClassification.fromLabel(event.necessity),
                confirmationStatus = ConfirmationStatus.valueOf(event.confirmationStatus),
                createdAtEpochMs = event.createdAtEpochMs,
                updatedAtEpochMs = event.updatedAtEpochMs
            )
        }

        // Group by Day
        val dayGrouped = groupEntriesByDay(diaryEntries, useBnDigits)

        // Filtered dayGrouped
        val filteredEntries = diaryEntries.filter { entry ->
            val matchesQuery = query.isBlank() ||
                    entry.description.contains(query, ignoreCase = true) ||
                    (entry.notes?.contains(query, ignoreCase = true) == true) ||
                    (entry.sourceDescription?.contains(query, ignoreCase = true) == true) ||
                    entry.categoryNameBn.contains(query, ignoreCase = true)

            val matchesType = typeF == null || entry.type == typeF
            val matchesCategory = catF == null || entry.categoryId == catF
            val matchesNecessity = necF == null || entry.necessity == necF
            val matchesDate = dateF == null || isSameDay(entry.occurrenceDateEpochMs, dateF)

            matchesQuery && matchesType && matchesCategory && matchesNecessity && matchesDate
        }
        val filteredDayGrouped = groupEntriesByDay(filteredEntries, useBnDigits)

        // Today's summary & entries
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val todayEntries = diaryEntries.filter { it.occurrenceDateEpochMs >= todayStart }
        val todayIncome = todayEntries.filter { it.type == DiaryEntryType.INCOME }.sumOf { it.amountPaisa }
        val todayExpense = todayEntries.filter { it.type == DiaryEntryType.EXPENSE }.sumOf { it.amountPaisa }
        val todaySummary = TodaySummary(
            todayIncomePaisa = todayIncome,
            todayExpensePaisa = todayExpense,
            todayNetChangePaisa = todayIncome - todayExpense,
            hasEntriesToday = todayEntries.isNotEmpty()
        )

        // Analysis Insights
        val now = System.currentTimeMillis()
        val yesterdayStart = todayStart - 86400000L
        val yesterdayEntries = diaryEntries.filter { it.occurrenceDateEpochMs in yesterdayStart until todayStart }
        val todayVsYesterday = DiaryAnalysisEngine.comparePeriods(
            "আজ", "গতকাল", todayEntries, yesterdayEntries, useBnDigits
        )

        val weekStart = getStartOfWeek(now)
        val lastWeekStart = weekStart - (7 * 86400000L)
        val thisWeekEntries = diaryEntries.filter { it.occurrenceDateEpochMs >= weekStart }
        val lastWeekEntries = diaryEntries.filter { it.occurrenceDateEpochMs in lastWeekStart until weekStart }
        val weekVsLastWeek = DiaryAnalysisEngine.comparePeriods(
            "এই সপ্তাহ", "গত সপ্তাহ", thisWeekEntries, lastWeekEntries, useBnDigits
        )

        val monthStart = getStartOfMonth(now)
        val lastMonthStart = getLastMonthStart(now)
        val thisMonthEntries = diaryEntries.filter { it.occurrenceDateEpochMs >= monthStart }
        val lastMonthEntries = diaryEntries.filter { it.occurrenceDateEpochMs in lastMonthStart until monthStart }
        val monthVsLastMonth = DiaryAnalysisEngine.comparePeriods(
            "এই মাস", "গত মাস", thisMonthEntries, lastMonthEntries, useBnDigits
        )

        val yearStart = getStartOfYear(now)
        val lastYearStart = getLastYearStart(now)
        val thisYearEntries = diaryEntries.filter { it.occurrenceDateEpochMs >= yearStart }
        val lastYearEntries = diaryEntries.filter { it.occurrenceDateEpochMs in lastYearStart until yearStart }
        val yearVsLastYear = DiaryAnalysisEngine.comparePeriods(
            "এই বছর", "গত বছর", thisYearEntries, lastYearEntries, useBnDigits
        )

        val categoryShares = DiaryAnalysisEngine.calculateCategoryShares(thisMonthEntries.ifEmpty { diaryEntries })
        val highestSpending = categoryShares.firstOrNull()
        val repeatedSmall = DiaryAnalysisEngine.findRepeatedSmallExpenses(thisMonthEntries.ifEmpty { diaryEntries })

        val necessityBreakdown = diaryEntries.filter { it.type == DiaryEntryType.EXPENSE }
            .groupBy { it.necessity }
            .mapValues { it.value.sumOf { entry -> entry.amountPaisa } }

        val domainGoals = activeGoals.map { g ->
            FrugalityGoal(
                id = g.id,
                title = g.title,
                goalType = g.goalType,
                categoryId = g.categoryId,
                categoryNameBn = g.categoryId?.let { catMap[it]?.nameBn },
                targetAmountPaisa = g.targetAmountPaisa,
                targetReductionPercentage = g.targetReductionPercentage,
                periodType = g.periodType,
                status = g.status,
                notes = g.notes,
                createdAtEpochMs = g.createdAtEpochMs,
                updatedAtEpochMs = g.updatedAtEpochMs
            )
        }
        val goalProgresses = domainGoals.map { g ->
            DiaryAnalysisEngine.calculateGoalProgress(g, diaryEntries, useBnDigits)
        }

        val shortcuts = shortcutDao.getAll()

        val singleHighlight = DiaryAnalysisEngine.generateTodayInsight(diaryEntries, useBnDigits)

        val analysisInsights = AnalysisInsights(
            todayVsYesterday = todayVsYesterday,
            weekVsLastWeek = weekVsLastWeek,
            monthVsLastMonth = monthVsLastMonth,
            yearVsLastYear = yearVsLastYear,
            highestSpendingCategory = highestSpending,
            fastestGrowingCategory = monthVsLastMonth?.topContributingCategories?.firstOrNull { it.isIncrease },
            mostFrequentExpenseCategory = null,
            repeatedSmallExpenses = repeatedSmall,
            categoryShares = categoryShares,
            necessityBreakdown = necessityBreakdown,
            frugalityGoalsProgress = goalProgresses,
            singleHighlightInsight = singleHighlight
        )

        // Legacy summaries calculated safely
        val balances = FinancialCalculationEngine.calculateAccountBalances(accounts, nonDeletedEvents)
        val accountsWithBal = accounts.map { acc ->
            AccountWithBalance(acc, balances[acc.id] ?: 0L)
        }
        val periodSummary = FinancialCalculationEngine.calculatePeriodFinancialSummary(monthStart, now, nonDeletedEvents)
        val dashboardSummary = DashboardSummary(
            totalLiquidCashPaisa = balances.values.sum(),
            todayExpensePaisa = todayExpense,
            monthExpensePaisa = periodSummary.incurredExpensePaisa,
            monthEarnedIncomePaisa = periodSummary.earnedIncomePaisa,
            unearnedAdvancePaisa = periodSummary.unearnedAdvancePaisa,
            activeReceivablePaisa = 0L,
            activePayablePaisa = 0L
        )

        val legacyRecent = nonDeletedEvents.take(15).map { e ->
            TransactionDisplayItem(
                id = e.id,
                eventCode = e.eventCode,
                amountPaisa = e.amountPaisa,
                sourceAccountId = e.sourceAccountId,
                destinationAccountId = e.destinationAccountId,
                sourceAccountName = e.sourceAccountId?.let { accMap[it]?.name },
                destinationAccountName = e.destinationAccountId?.let { accMap[it]?.name },
                categoryId = e.categoryId,
                categoryNameBn = e.categoryId?.let { catMap[it]?.nameBn },
                categoryIcon = e.categoryId?.let { catMap[it]?.iconName },
                description = e.description,
                notes = e.notes,
                timestampEpochMs = e.eventTimestampEpochMs,
                confirmationStatus = e.confirmationStatus
            )
        }

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                isSetupCompleted = isSetupDone,
                useBengaliDigits = useBnDigits,
                todaySummary = todaySummary,
                todayEntries = todayEntries,
                allDiaryEntries = diaryEntries,
                dayGroupedEntries = dayGrouped,
                filteredDayGroupedEntries = filteredDayGrouped,
                searchQuery = query,
                selectedTypeFilter = typeF,
                selectedCategoryIdFilter = catF,
                selectedNecessityFilter = necF,
                selectedDateEpochMs = dateF,
                analysisInsights = analysisInsights,
                todayHighlightInsight = singleHighlight,
                frugalityGoals = domainGoals,
                frugalityGoalProgresses = goalProgresses,
                isDailyReminderEnabled = reminderEnabled,
                dailyReminderTime = reminderTime,
                diaryName = diaryName,
                themeMode = themeMode,
                colorIdentity = colorIdentity,
                categories = allCategories,
                activeCategories = activeCategories,
                accountsWithBalance = accountsWithBal,
                frequentShortcuts = shortcuts,
                dashboardSummary = dashboardSummary,
                recentTransactions = legacyRecent,
                allTransactions = legacyRecent
            )
        }
    }

    private fun groupEntriesByDay(entries: List<DiaryEntry>, useBnDigits: Boolean): List<DayGroupedDiary> {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("bn-BD"))
        return entries.groupBy { getStartOfDay(it.occurrenceDateEpochMs) }
            .map { (dayStart, dayEntries) ->
                val income = dayEntries.filter { it.type == DiaryEntryType.INCOME }.sumOf { it.amountPaisa }
                val expense = dayEntries.filter { it.type == DiaryEntryType.EXPENSE }.sumOf { it.amountPaisa }
                val label = BengaliFormatter.formatBengaliDate(dayStart)
                DayGroupedDiary(
                    dateEpochMs = dayStart,
                    dateLabelBn = label,
                    totalIncomePaisa = income,
                    totalExpensePaisa = expense,
                    netChangePaisa = income - expense,
                    entries = dayEntries.sortedByDescending { it.occurrenceDateEpochMs }
                )
            }.sortedByDescending { it.dateEpochMs }
    }

    private fun getStartOfDay(epochMs: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun isSameDay(ms1: Long, ms2: Long): Boolean {
        return getStartOfDay(ms1) == getStartOfDay(ms2)
    }

    private fun getStartOfWeek(epochMs: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun getStartOfMonth(epochMs: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun getLastMonthStart(epochMs: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = epochMs
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun getStartOfYear(epochMs: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun getLastYearStart(epochMs: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = epochMs
            add(Calendar.YEAR, -1)
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    // --- Navigation & UI Dialog State ---

    fun selectTab(tab: AppNavTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openDialog(dialog: ActiveDialog) {
        _uiState.update { it.copy(activeDialog = dialog) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.None) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // --- Search & Filters ---

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: DiaryEntryType?) {
        _typeFilter.value = type
    }

    fun setCategoryFilter(categoryId: String?) {
        _categoryFilter.value = categoryId
    }

    fun setNecessityFilter(necessity: NecessityClassification?) {
        _necessityFilter.value = necessity
    }

    fun setDateFilter(dateEpochMs: Long?) {
        _dateFilter.value = dateEpochMs
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _typeFilter.value = null
        _categoryFilter.value = null
        _necessityFilter.value = null
        _dateFilter.value = null
    }

    // --- Diary Entry Operations ---

    fun recordDiaryEntry(
        type: DiaryEntryType,
        amountPaisa: Long,
        categoryId: String?,
        sourceDescription: String?,
        description: String,
        notes: String? = null,
        necessity: NecessityClassification = NecessityClassification.UNSPECIFIED,
        occurrenceDateEpochMs: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch(ioDispatcher) {
            DiagnosticService.addCheckpoint("VIEWMODEL_SUBMIT_STARTED", type.name)
            if (amountPaisa <= 0) {
                _uiState.update { it.copy(snackbarMessage = "টাকার পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।") }
                return@launch
            }
            if (description.isBlank()) {
                _uiState.update { it.copy(snackbarMessage = "হিসাবের বিবরণ লিখুন।") }
                return@launch
            }

            val result = financialService.recordDiaryEntry(
                type = type,
                amountPaisa = amountPaisa,
                categoryId = categoryId,
                sourceDescription = sourceDescription,
                description = description,
                notes = notes,
                necessity = necessity,
                timestampEpochMs = occurrenceDateEpochMs
            )

            when (result) {
                is FinancialResult.Success -> {
                    _uiState.update {
                        it.copy(
                            activeDialog = ActiveDialog.None,
                            snackbarMessage = "${type.banglaLabel} হিসাব সফলভাবে সংরক্ষিত হয়েছে।",
                            lastAddedEventId = result.data.id
                        )
                    }
                }
                is FinancialResult.Error -> {
                    _uiState.update { it.copy(snackbarMessage = result.message) }
                }
            }
            refreshNow()
        }
    }

    fun updateDiaryEntry(
        id: String,
        amountPaisa: Long,
        categoryId: String?,
        sourceDescription: String?,
        description: String,
        notes: String?,
        necessity: NecessityClassification = NecessityClassification.UNSPECIFIED,
        occurrenceDateEpochMs: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch(ioDispatcher) {
            val result = financialService.updateDiaryEntry(
                id = id,
                amountPaisa = amountPaisa,
                categoryId = categoryId,
                sourceDescription = sourceDescription,
                description = description,
                notes = notes,
                necessity = necessity,
                timestampEpochMs = occurrenceDateEpochMs
            )
            when (result) {
                is FinancialResult.Success -> {
                    _uiState.update {
                        it.copy(
                            activeDialog = ActiveDialog.None,
                            snackbarMessage = "হিসাবটি আপডেট করা হয়েছে।"
                        )
                    }
                }
                is FinancialResult.Error -> {
                    _uiState.update { it.copy(snackbarMessage = result.message) }
                }
            }
            refreshNow()
        }
    }

    fun deleteDiaryEntry(id: String, reason: String? = null) {
        viewModelScope.launch(ioDispatcher) {
            val result = financialService.deleteOrReverseDiaryEntry(id, reason)
            when (result) {
                is FinancialResult.Success -> {
                    _uiState.update {
                        it.copy(
                            activeDialog = ActiveDialog.None,
                            snackbarMessage = "হিসাবটি মুছে ফেলা হয়েছে।"
                        )
                    }
                }
                is FinancialResult.Error -> {
                    _uiState.update { it.copy(snackbarMessage = result.message) }
                }
            }
            refreshNow()
        }
    }

    // --- Category Management ---

    fun createCategory(
        nameBn: String,
        iconName: String? = null,
        isIncome: Boolean = false
    ) {
        viewModelScope.launch(ioDispatcher) {
            if (nameBn.isBlank()) {
                _uiState.update { it.copy(snackbarMessage = "খাতের নাম লিখুন।") }
                return@launch
            }
            val icon = iconName ?: CategoryIconResolver.suggestIconKey(nameBn)
            val newCat = CategoryEntity(
                id = UUID.randomUUID().toString(),
                nameBn = nameBn.trim(),
                nameEn = nameBn.trim(),
                iconName = icon,
                colorHex = if (isIncome) "#2F7556" else "#234E45",
                isIncomeCategory = isIncome,
                isSystemDefault = false,
                displayOrder = 99
            )
            categoryDao.insert(newCat)
            _uiState.update { it.copy(snackbarMessage = "নতুন খাত যোগ করা হয়েছে।") }
            refreshNow()
        }
    }

    fun archiveCategory(id: String, isArchived: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            categoryDao.setArchived(id, isArchived)
            _uiState.update {
                it.copy(snackbarMessage = if (isArchived) "খাতটি আর্কাইভ করা হয়েছে।" else "খাতটি সক্রিয় করা হয়েছে।")
            }
            refreshNow()
        }
    }

    // --- Frugality Goals Management ---

    fun addFrugalityGoal(
        title: String,
        goalType: String,
        categoryId: String? = null,
        targetAmountPaisa: Long,
        periodType: String = "MONTHLY",
        notes: String? = null
    ) {
        viewModelScope.launch(ioDispatcher) {
            if (title.isBlank()) {
                _uiState.update { it.copy(snackbarMessage = "লক্ষ্যের একটি নাম লিখুন।") }
                return@launch
            }
            if (targetAmountPaisa <= 0) {
                _uiState.update { it.copy(snackbarMessage = "লক্ষ্যের টাকার পরিমাণ লিখুন।") }
                return@launch
            }

            val goal = FrugalityGoalEntity(
                title = title.trim(),
                goalType = goalType,
                categoryId = categoryId,
                targetAmountPaisa = targetAmountPaisa,
                periodType = periodType,
                notes = notes?.trim(),
                status = "ACTIVE"
            )
            frugalityGoalDao.insert(goal)
            _uiState.update {
                it.copy(
                    activeDialog = ActiveDialog.None,
                    snackbarMessage = "মিতব্যয়ী লক্ষ্য সফলভাবে যুক্ত হয়েছে।"
                )
            }
            refreshNow()
        }
    }

    fun deleteFrugalityGoal(id: String) {
        viewModelScope.launch(ioDispatcher) {
            frugalityGoalDao.softDelete(id)
            _uiState.update { it.copy(snackbarMessage = "লক্ষ্যটি মুছে ফেলা হয়েছে।") }
            refreshNow()
        }
    }

    // --- Settings & First Launch Setup ---

    fun completeFirstLaunchSetup(
        useBengaliDigits: Boolean,
        accounts: List<SetupAccountDraft> = emptyList(),
        language: String = "bn",
        currency: String = "BDT",
        theme: String = "SYSTEM",
        reminderEnabled: Boolean = false,
        diaryName: String = "কড়ি"
    ) {
        viewModelScope.launch(ioDispatcher) {
            appSettingDao.set("first_launch_completed", "true")
            appSettingDao.set("numeral_system", if (useBengaliDigits) "bn" else "en")
            appSettingDao.set("language", language)
            appSettingDao.set("currency", currency)
            appSettingDao.set("theme_mode", theme)
            appSettingDao.set("reminder_daily_enabled", reminderEnabled.toString())
            appSettingDao.set("diary_name", diaryName)

            // Seed default categories if empty
            com.aistudio.moneydiary.mndytr.data.local.DatabaseInitializer.seedCategoriesIfEmpty(db)

            // Support legacy drafts if passed from older tests
            for (draft in accounts.filter { it.isEnabled }) {
                val acc = com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity(
                    name = draft.name,
                    type = draft.type,
                    currency = currency,
                    initialBalancePaisa = 0L
                )
                accountDao.insert(acc)
                val balPaisa = BengaliFormatter.parseAmountToPaisa(draft.openingBalanceText)
                if (balPaisa > 0) {
                    financialService.createOpeningBalance(acc.id, balPaisa)
                }
            }

            _uiState.update {
                it.copy(
                    isSetupCompleted = true,
                    useBengaliDigits = useBengaliDigits,
                    diaryName = diaryName,
                    themeMode = theme,
                    isDailyReminderEnabled = reminderEnabled
                )
            }
            refreshNow()
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch(ioDispatcher) {
            appSettingDao.set(key, value)
            refreshNow()
        }
    }

    fun setColorIdentity(identity: com.aistudio.moneydiary.mndytr.ui.theme.KoriColorIdentity) {
        viewModelScope.launch(ioDispatcher) {
            appSettingDao.set("color_identity", identity.name)
            _uiState.update { it.copy(colorIdentity = identity) }
            refreshNow()
        }
    }

    fun setAppLocked(locked: Boolean) {
        _uiState.update { it.copy(isAppLocked = locked) }
    }

    fun setLatestGeneratedRecoveryKey(key: String?) {
        _uiState.update { it.copy(latestGeneratedRecoveryKey = key) }
    }

    fun updateSecurityConfig(config: com.aistudio.moneydiary.mndytr.domain.service.AppSecurityConfig) {
        _uiState.update { it.copy(securityConfig = config) }
    }

    // --- Diagnostic Report Deletion ---

    fun deleteCrashReport(reportFile: File) {
        viewModelScope.launch(ioDispatcher) {
            DiagnosticService.deleteReport(reportFile)
            _uiState.update {
                it.copy(
                    activeDialog = ActiveDialog.None,
                    snackbarMessage = "ক্র্যাশ রিপোর্ট মুছে ফেলা হয়েছে।"
                )
            }
        }
    }

    // --- Legacy / Backward Compatible Methods for Unit Test Invariants ---

    fun addExpense(
        accountId: String,
        amountPaisa: Long,
        categoryId: String,
        description: String,
        notes: String? = null,
        isMicroExpense: Boolean = false
    ) {
        viewModelScope.launch(ioDispatcher) {
            val cat = categoryDao.getById(categoryId)
            financialService.recordDiaryEntry(
                type = DiaryEntryType.EXPENSE,
                amountPaisa = amountPaisa,
                categoryId = categoryId,
                sourceDescription = accountDao.getById(accountId)?.name,
                description = description,
                notes = notes
            )
            refreshNow()
        }
    }

    fun addIncome(
        destinationAccountId: String,
        amountPaisa: Long,
        categoryId: String? = null,
        description: String,
        notes: String? = null
    ) {
        viewModelScope.launch(ioDispatcher) {
            financialService.recordDiaryEntry(
                type = DiaryEntryType.INCOME,
                amountPaisa = amountPaisa,
                categoryId = categoryId,
                sourceDescription = accountDao.getById(destinationAccountId)?.name,
                description = description,
                notes = notes
            )
            refreshNow()
        }
    }

    fun addWorkAdvance(
        clientName: String,
        workTitle: String,
        agreedTotalPaisa: Long,
        advanceReceivedPaisa: Long,
        destinationAccountId: String
    ) {
        viewModelScope.launch(ioDispatcher) {
            val person = personDao.getAll().firstOrNull { it.name == clientName }
                ?: com.aistudio.moneydiary.mndytr.data.local.entity.PersonEntity(name = clientName).also { personDao.insert(it) }

            val work = com.aistudio.moneydiary.mndytr.data.local.entity.WorkRecordEntity(
                clientId = person.id,
                title = workTitle,
                agreedTotalPricePaisa = agreedTotalPaisa,
                workStatus = WorkStatus.CONFIRMED.name,
                paymentStatus = PaymentStatus.UNPAID.name,
                preferredAccountId = destinationAccountId
            )
            workDao.insert(work)

            if (advanceReceivedPaisa > 0) {
                financialService.receiveWorkAdvance(work.id, destinationAccountId, advanceReceivedPaisa)
            }
            refreshNow()
        }
    }

    fun settleWork(workId: String, destinationAccountId: String, finalPaymentPaisa: Long = 0L) {
        viewModelScope.launch(ioDispatcher) {
            financialService.settleWorkCompleted(workId, destinationAccountId)
            refreshNow()
        }
    }

    fun transferFunds(sourceAccountId: String, destinationAccountId: String, amountPaisa: Long, feePaisa: Long = 0L) {
        viewModelScope.launch(ioDispatcher) {
            financialService.transferBetweenAccounts(sourceAccountId, destinationAccountId, amountPaisa, feePaisa)
            refreshNow()
        }
    }

    fun allocateSavings(sourceAccountId: String, destinationAccountId: String, amountPaisa: Long, note: String? = null) {
        viewModelScope.launch(ioDispatcher) {
            financialService.allocateToSavings(
                sourceAccountId = sourceAccountId,
                savingsAccountId = destinationAccountId,
                amountPaisa = amountPaisa,
                description = note ?: "সঞ্চয়ে স্থানান্তর"
            )
            refreshNow()
        }
    }

    fun executeShortcut(shortcut: FrequentShortcutEntity) {
        viewModelScope.launch(ioDispatcher) {
            shortcutDao.incrementUsage(shortcut.id)
            financialService.recordDiaryEntry(
                type = DiaryEntryType.EXPENSE,
                amountPaisa = shortcut.defaultAmountPaisa,
                categoryId = shortcut.defaultCategoryId,
                sourceDescription = accountDao.getById(shortcut.defaultAccountId)?.name,
                description = shortcut.labelBn,
                notes = null
            )
            refreshNow()
        }
    }

    fun undoLastAction() {
        val lastId = _uiState.value.lastAddedEventId ?: return
        viewModelScope.launch(ioDispatcher) {
            financialService.deleteOrReverseDiaryEntry(lastId, "পূর্বাবস্থায় ফিরিয়ে নেওয়া হয়েছে")
            _uiState.update { it.copy(lastAddedEventId = null, snackbarMessage = "হিসাবটি মুছে দেওয়া হয়েছে।") }
            refreshNow()
        }
    }

    fun reverseTransaction(transactionId: String, reason: String) {
        viewModelScope.launch(ioDispatcher) {
            financialService.deleteOrReverseDiaryEntry(transactionId, reason)
            refreshNow()
        }
    }

    private var clipboardClearJob: Job? = null

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun exportLocalBackup(context: android.content.Context, includeNotes: Boolean): String {
        return com.aistudio.moneydiary.mndytr.domain.service.LocalBackupRestoreService.exportBackup(context, db, includeNotes)
    }

    fun prepareBackupForClipboard(context: android.content.Context, json: String) {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("kori_backup", json)
        
        // Mark as sensitive if supported (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            clip.description.extras = android.os.PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
        
        clipboard.setPrimaryClip(clip)
        showSnackbar("ব্যাকআপ কোড কপি করা হয়েছে। ১ মিনিট পর এটি ক্লিপবোর্ড থেকে মুছে যাবে।")
        
        // Schedule auto-clear
        clipboardClearJob?.cancel()
        clipboardClearJob = viewModelScope.launch {
            delay(60000L) // 60 seconds
            try {
                val currentClip = clipboard.primaryClip
                if (currentClip != null && currentClip.getItemAt(0).text == json) {
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
                    showSnackbar("নিরাপত্তার স্বার্থে ক্লিপবোর্ড থেকে ব্যাকআপ কোড মুছে ফেলা হয়েছে।")
                }
            } catch (e: Exception) {
                // Ignore failures to clear
            }
        }
    }

    fun shareBackupAsFile(context: android.content.Context, json: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val backupDir = File(context.cacheDir, "backups")
                if (!backupDir.exists()) backupDir.mkdirs()
                
                val fileName = "Kori_Backup_${System.currentTimeMillis()}.kori"
                val file = File(backupDir, fileName)
                file.writeText(json)
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                withContext(Dispatchers.Main) {
                    context.startActivity(android.content.Intent.createChooser(intent, "ব্যাকআপ শেয়ার করুন"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showSnackbar("ফাইল শেয়ার করতে সমস্যা হয়েছে।")
                }
            }
        }
    }

    fun previewLocalRestore(context: android.content.Context, backupJson: String) {
        val summary = com.aistudio.moneydiary.mndytr.domain.service.LocalBackupRestoreService.parseAndPreviewBackup(context, db, backupJson)
        if (summary != null) {
            openDialog(ActiveDialog.RestorePreview(summary))
        } else {
            showSnackbar("দুঃখিত, ব্যাকআপ ফাইলটি সঠিক নয় বা দুর্নীতিগ্রস্ত।")
        }
    }

    fun confirmRestore(context: android.content.Context, summary: com.aistudio.moneydiary.mndytr.domain.model.BackupSummary) {
        viewModelScope.launch(ioDispatcher) {
            val result = com.aistudio.moneydiary.mndytr.domain.service.LocalBackupRestoreService.executeSafeRestore(context, db, summary.rawJson)
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.aistudio.moneydiary.mndytr.domain.model.RestoreResult.Success -> {
                        viewModelScope.launch(ioDispatcher) {
                            refreshNow()
                        }
                        dismissDialog()
                        showSnackbar("সফলভাবে ব্যাকআপ পুনরুদ্ধার করা হয়েছে।")
                    }
                    is com.aistudio.moneydiary.mndytr.domain.model.RestoreResult.Failure -> {
                        showSnackbar(result.messageBn)
                    }
                }
            }
        }
    }

    fun restoreLocalBackup(context: android.content.Context, backupJson: String): Boolean {
        // Legacy method for tests or simple flow, but now we prefer the preview flow
        previewLocalRestore(context, backupJson)
        return true
    }
}


class DiaryViewModelFactory(
    private val db: AppDatabase,
    private val financialService: FinancialService = FinancialService(db),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            return DiaryViewModel(db, financialService, ioDispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

