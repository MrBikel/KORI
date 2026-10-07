package com.aistudio.moneydiary.mndytr.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.domain.model.*
import com.aistudio.moneydiary.mndytr.domain.service.CategoryIconResolver
import com.aistudio.moneydiary.mndytr.domain.service.DiaryAnalysisEngine
import com.aistudio.moneydiary.mndytr.domain.service.FinancialService
import com.aistudio.moneydiary.mndytr.domain.service.LocalNotificationHelper
import androidx.compose.ui.graphics.toArgb
import com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog
import com.aistudio.moneydiary.mndytr.ui.state.AppNavTab
import com.aistudio.moneydiary.mndytr.ui.theme.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import com.aistudio.moneydiary.mndytr.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Stage1DiaryFirstTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DiaryViewModel {
        return DiaryViewModel(db = db, financialService = FinancialService(db), ioDispatcher = testDispatcher)
    }

    // 1. Empty-first experience: No entries, clean state
    @Test
    fun test01_emptyFirstExperienceHasNoFakeData() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSetupCompleted)
        assertFalse(state.todaySummary.hasEntriesToday)
        assertEquals(0L, state.todaySummary.todayIncomePaisa)
        assertEquals(0L, state.todaySummary.todayExpensePaisa)
        assertEquals(0L, state.todaySummary.todayNetChangePaisa)
        assertTrue(state.allDiaryEntries.isEmpty())
        assertTrue(state.todayEntries.isEmpty())
    }

    // 2. No mandatory wallet account selection required to record income or expense
    @Test
    fun test02_noMandatoryWalletForEntry() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 15000L, // ৳150
            categoryId = null,
            sourceDescription = null, // No source required
            description = "সকালের নাস্তা",
            notes = null,
            necessity = NecessityClassification.ESSENTIAL
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.allDiaryEntries.size)
        val entry = state.allDiaryEntries.first()
        assertEquals(15000L, entry.amountPaisa)
        assertEquals("সকালের নাস্তা", entry.description)
        assertNull(entry.sourceDescription)
        assertEquals(NecessityClassification.ESSENTIAL, entry.necessity)
    }

    // 3. Free-text source works as descriptive annotation
    @Test
    fun test03_freeTextSourceDescription() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 45000L, // ৳450
            categoryId = null,
            sourceDescription = "স্ত্রীর দেওয়া টাকা",
            description = "ওষুধ কেনা",
            notes = "প্যারাসিটামল ও গ্যাস্ট্রিকের ওষুধ",
            necessity = NecessityClassification.EMERGENCY
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val entry = state.allDiaryEntries.first { it.description == "ওষুধ কেনা" }
        assertEquals("স্ত্রীর দেওয়া টাকা", entry.sourceDescription)
        assertEquals(NecessityClassification.EMERGENCY, entry.necessity)
    }

    // 4. Automatic deterministic category icon suggestion
    @Test
    fun test04_automaticDeterministicIconSuggestion() {
        assertEquals("home", CategoryIconResolver.suggestIconKey("বাড়িভাড়া"))
        assertEquals("medication", CategoryIconResolver.suggestIconKey("ওষুধ"))
        assertEquals("local_hospital", CategoryIconResolver.suggestIconKey("চিকিৎসা ও ডাক্তার"))
        assertEquals("bolt", CategoryIconResolver.suggestIconKey("বিদ্যুৎ বিল"))
        assertEquals("wifi", CategoryIconResolver.suggestIconKey("Wi-Fi বিল"))
        assertEquals("phone_android", CategoryIconResolver.suggestIconKey("মোবাইল রিচার্জ"))
        assertEquals("favorite", CategoryIconResolver.suggestIconKey("স্ত্রী"))
        assertEquals("groups", CategoryIconResolver.suggestIconKey("পরিবার"))
        assertEquals("school", CategoryIconResolver.suggestIconKey("শিক্ষা"))
        assertEquals("local_cafe", CategoryIconResolver.suggestIconKey("চা"))
        assertEquals("smoking_rooms", CategoryIconResolver.suggestIconKey("সিগারেট"))
        assertEquals("payments", CategoryIconResolver.suggestIconKey("বেতন"))
        assertEquals("laptop", CategoryIconResolver.suggestIconKey("অনলাইন কাজ"))
        assertEquals("category", CategoryIconResolver.suggestIconKey("অজানা কোনো নাম"))
    }

    // 5. Custom category creation and archiving
    @Test
    fun test05_categoryCreationAndArchiving() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        viewModel.createCategory(nameBn = "গাছপালা ও বাগান", isIncome = false)
        advanceUntilIdle()
        viewModel.refreshNow()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val cat = state.categories.firstOrNull { it.nameBn == "গাছপালা ও বাগান" }
        assertNotNull(cat)

        // Archive category
        viewModel.archiveCategory(cat!!.id, true)
        advanceUntilIdle()

        val stateAfterArchive = viewModel.uiState.value
        assertFalse(stateAfterArchive.activeCategories.any { it.id == cat.id })
    }

    // 6. Necessity classifications
    @Test
    fun test06_necessityClassifications() {
        assertEquals("প্রয়োজনীয়", NecessityClassification.ESSENTIAL.banglaLabel)
        assertEquals("নিয়মিত", NecessityClassification.REGULAR.banglaLabel)
        assertEquals("ইচ্ছাধীন", NecessityClassification.DISCRETIONARY.banglaLabel)
        assertEquals("জরুরি", NecessityClassification.EMERGENCY.banglaLabel)
        assertEquals("অনির্ধারিত", NecessityClassification.UNSPECIFIED.banglaLabel)

        assertEquals(NecessityClassification.DISCRETIONARY, NecessityClassification.fromLabel("ইচ্ছাধীন"))
        assertEquals(NecessityClassification.EMERGENCY, NecessityClassification.fromLabel("জরুরি"))
    }

    // 7. Daily summary calculation
    @Test
    fun test07_dailySummaryCalculation() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.recordDiaryEntry(
            type = DiaryEntryType.INCOME,
            amountPaisa = 50000L, // ৳500
            categoryId = null,
            sourceDescription = "অনলাইন কাজ",
            description = "কনটেন্ট লেখা"
        )
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 46000L, // ৳460
            categoryId = null,
            sourceDescription = "পকেট",
            description = "ওষুধ ও চা"
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.todaySummary.hasEntriesToday)
        assertEquals(50000L, state.todaySummary.todayIncomePaisa)
        assertEquals(46000L, state.todaySummary.todayExpensePaisa)
        assertEquals(4000L, state.todaySummary.todayNetChangePaisa) // +৳40
    }

    // 8. Period comparison and mathematical denominator safety
    @Test
    fun test08_periodComparisonAndMathSafety() {
        val curEntries = listOf(
            DiaryEntry(
                id = UUID.randomUUID().toString(),
                type = DiaryEntryType.EXPENSE,
                amountPaisa = 120000L, // ৳1200
                categoryNameBn = "ওষুধ",
                description = "ওষুধ"
            )
        )
        val prevEntries = emptyList<DiaryEntry>()

        val comparison = DiaryAnalysisEngine.comparePeriods("এই মাস", "গত মাস", curEntries, prevEntries)
        assertNotNull(comparison)
        assertEquals(120000L, comparison!!.currentExpensePaisa)
        assertEquals(0L, comparison.previousExpensePaisa)
        assertNull(comparison.percentageDifference) // Division by zero safe
        assertTrue(comparison.narrative.contains("এই মাস মোট ব্যয়"))
    }

    // 9. Frugality goal progress and non-judgmental guidance
    @Test
    fun test09_frugalityGoalProgressNonJudgmental() {
        val goal = FrugalityGoal(
            id = UUID.randomUUID().toString(),
            title = "মাসিক চা-নাস্তা বাজেট",
            goalType = "TOTAL_LIMIT",
            targetAmountPaisa = 100000L, // ৳1000
            periodType = "MONTHLY"
        )

        // Spending ৳1200 (exceeded)
        val entries = listOf(
            DiaryEntry(
                id = UUID.randomUUID().toString(),
                type = DiaryEntryType.EXPENSE,
                amountPaisa = 120000L,
                description = "চা ও নাস্তা"
            )
        )

        val progress = DiaryAnalysisEngine.calculateGoalProgress(goal, entries)
        assertTrue(progress.isExceeded)
        assertEquals(120000L, progress.currentSpendingPaisa)
        assertEquals(0L, progress.remainingAllowancePaisa)
        assertFalse(progress.guidanceMessageBn.contains("ব্যর্থ"))
        assertFalse(progress.guidanceMessageBn.contains("অপচয়কারী"))
        assertTrue(progress.guidanceMessageBn.contains("সীমা অতিক্রম করেছে"))
    }

    // 10. Repeated small expenses detector
    @Test
    fun test10_repeatedSmallExpensesDetection() {
        val entries = listOf(
            DiaryEntry(id = "1", type = DiaryEntryType.EXPENSE, amountPaisa = 1000L, categoryNameBn = "চা", description = "রং চা"),
            DiaryEntry(id = "2", type = DiaryEntryType.EXPENSE, amountPaisa = 1000L, categoryNameBn = "চা", description = "দুধ চা"),
            DiaryEntry(id = "3", type = DiaryEntryType.EXPENSE, amountPaisa = 2000L, categoryNameBn = "চা", description = "মালাই চা")
        )

        val repeated = DiaryAnalysisEngine.findRepeatedSmallExpenses(entries)
        assertEquals(1, repeated.size)
        assertTrue(repeated.first().categoryNameBn.contains("চা (3 বার)"))
        assertEquals(4000L, repeated.first().amountPaisa)
    }

    // 11. 4 Primary navigation destinations
    @Test
    fun test11_fourPrimaryNavigationDestinations() {
        val destinations = AppNavTab.values()
        assertEquals(4, destinations.size)
        assertEquals(AppNavTab.TODAY, destinations[0])
        assertEquals(AppNavTab.DIARY, destinations[1])
        assertEquals(AppNavTab.ANALYSIS, destinations[2])
        assertEquals(AppNavTab.MORE, destinations[3])
    }

    // 12. Theme color contrast definitions (Theme A: Ink and Paper)
    @Test
    fun test12_themeColorDefinitions() {
        fun androidx.compose.ui.graphics.Color.argbInt(): Int = (this.value.toLong() ushr 32).toInt()

        assertEquals(0xFFF7F3EA.toInt(), InkAndPaperLight.canvas.argbInt())
        assertEquals(0xFFFFFCF6.toInt(), InkAndPaperLight.surface.argbInt())
        assertEquals(0xFFFFFFFF.toInt(), InkAndPaperLight.raisedSurface.argbInt())
        assertEquals(0xFF263238.toInt(), InkAndPaperLight.primary.argbInt())
        assertEquals(0xFFE8ECEB.toInt(), InkAndPaperLight.primarySoft.argbInt())
        assertEquals(0xFF202624.toInt(), InkAndPaperLight.textMain.argbInt())
        assertEquals(0xFF69716E.toInt(), InkAndPaperLight.textMuted.argbInt())
        assertEquals(0xFFDFE3DF.toInt(), InkAndPaperLight.border.argbInt())
        assertEquals(0xFF42765B.toInt(), InkAndPaperLight.income.argbInt())
        assertEquals(0xFFA4524D.toInt(), InkAndPaperLight.expense.argbInt())
        assertEquals(0xFF536F82.toInt(), InkAndPaperLight.insight.argbInt())
        assertEquals(0xFF956A2F.toInt(), InkAndPaperLight.warning.argbInt())

        assertEquals(0xFF151817.toInt(), InkAndPaperDark.canvas.argbInt())
        assertEquals(0xFF1D2220.toInt(), InkAndPaperDark.surface.argbInt())
        assertEquals(0xFF252B28.toInt(), InkAndPaperDark.raisedSurface.argbInt())
        assertEquals(0xFFBFCBC6.toInt(), InkAndPaperDark.primary.argbInt())
        assertEquals(0xFF303835.toInt(), InkAndPaperDark.primarySoft.argbInt())
        assertEquals(0xFFF5F3ED.toInt(), InkAndPaperDark.textMain.argbInt())
        assertEquals(0xFFADB5B1.toInt(), InkAndPaperDark.textMuted.argbInt())
        assertEquals(0xFF323A36.toInt(), InkAndPaperDark.border.argbInt())
        assertEquals(0xFF7DB091.toInt(), InkAndPaperDark.income.argbInt())
        assertEquals(0xFFD1847E.toInt(), InkAndPaperDark.expense.argbInt())
        assertEquals(0xFF91AABD.toInt(), InkAndPaperDark.insight.argbInt())
        assertEquals(0xFFD1A56B.toInt(), InkAndPaperDark.warning.argbInt())
    }

    // 13. Room Schema Version 2 and Migration 1->2
    @Test
    fun test13_roomSchemaVersion2AndMigration() {
        assertEquals(2, AppDatabase.getInstance(context).openHelper.readableDatabase.version)
        assertNotNull(AppDatabase.MIGRATION_1_2)
    }

    // 14. Local notification channel creation safe execution
    @Test
    fun test14_localNotificationChannelCreation() {
        LocalNotificationHelper.createNotificationChannel(context)
        // Verified executing safely without exception
    }

    // 15. Local backup creation and verified checksum restore
    @Test
    fun test15_localBackupAndRestore() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 12000L,
            categoryId = null,
            sourceDescription = "বেতন",
            description = "টেস্ট খরচ"
        )
        advanceUntilIdle()

        // 1. Export backup
        val backupJson = viewModel.exportLocalBackup(context, includeNotes = true)
        assertNotNull(backupJson)
        assertTrue(backupJson.contains("schema_version"))
        assertTrue(backupJson.contains("checksum"))

        // 2. Restore into a new database instance
        val newDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val result = com.aistudio.moneydiary.mndytr.domain.service.LocalBackupRestoreService.executeSafeRestore(context, newDb, backupJson)
        assertTrue(result is com.aistudio.moneydiary.mndytr.domain.model.RestoreResult.Success)

        val restoredEntries = newDb.ledgerEventDao().getAllNonDeleted()
        assertEquals(1, restoredEntries.size)
        assertEquals(12000L, restoredEntries.first().amountPaisa)
        assertEquals("টেস্ট খরচ", restoredEntries.first().description)
        newDb.close()
    }
}
