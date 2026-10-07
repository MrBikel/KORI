package com.aistudio.moneydiary.mndytr.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification
import com.aistudio.moneydiary.mndytr.domain.service.FinancialService
import com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog
import com.aistudio.moneydiary.mndytr.ui.state.AppNavTab
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryViewModelRobolectricTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
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

    @Test
    fun testFirstLaunchSetupFlow() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSetupCompleted)
        assertFalse(state.todaySummary.hasEntriesToday)
        assertEquals(0L, state.todaySummary.todayExpensePaisa)
        assertEquals(0L, state.todaySummary.todayIncomePaisa)
    }

    @Test
    fun testRecordExpenseAndIncomeFlow() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        // Record income
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.INCOME,
            amountPaisa = 100000L, // ৳1000
            categoryId = null,
            sourceDescription = "বেতন থেকে",
            description = "মাসিক বেতন"
        )
        advanceUntilIdle()

        // Record expense
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 15000L, // ৳150
            categoryId = null,
            sourceDescription = "পকেটের টাকা",
            description = "ওষুধ কেনা",
            necessity = NecessityClassification.ESSENTIAL
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.todaySummary.hasEntriesToday)
        assertEquals(100000L, state.todaySummary.todayIncomePaisa)
        assertEquals(15000L, state.todaySummary.todayExpensePaisa)
        assertEquals(85000L, state.todaySummary.todayNetChangePaisa)
        assertEquals(2, state.allDiaryEntries.size)
    }

    @Test
    fun testCustomCategoryCreation() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        viewModel.createCategory(nameBn = "ফটোশুট ও ভিডিও", isIncome = true)
        advanceUntilIdle()
        viewModel.refreshNow()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val customCat = state.categories.firstOrNull { it.nameBn == "ফটোশুট ও ভিডিও" }
        assertNotNull(customCat)
        assertTrue(customCat!!.isIncomeCategory)
    }

    @Test
    fun testBengaliFormatterAndDigits() {
        val bengaliFormatted = BengaliFormatter.formatPaisa(125000L, useBengaliDigits = true, showDecimals = false)
        assertEquals("৳ ১,২৫০", bengaliFormatted)

        val englishFormatted = BengaliFormatter.formatPaisa(125000L, useBengaliDigits = false, showDecimals = false)
        assertEquals("৳ 1,250", englishFormatted)

        val parsedFromBengali = BengaliFormatter.parseAmountToPaisa("৳ ১,২৫০")
        assertEquals(125000L, parsedFromBengali)

        val parsedFromEnglish = BengaliFormatter.parseAmountToPaisa("1250")
        assertEquals(125000L, parsedFromEnglish)
    }

    @Test
    fun testTabNavigationAndDialogState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(AppNavTab.TODAY, viewModel.uiState.value.selectedTab)
        viewModel.selectTab(AppNavTab.DIARY)
        assertEquals(AppNavTab.DIARY, viewModel.uiState.value.selectedTab)

        viewModel.openDialog(ActiveDialog.AddExpense)
        assertEquals(ActiveDialog.AddExpense, viewModel.uiState.value.activeDialog)

        viewModel.dismissDialog()
        assertEquals(ActiveDialog.None, viewModel.uiState.value.activeDialog)
    }

    @Test
    fun testEntryDeletionAndRestoration() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 2000L,
            categoryId = null,
            sourceDescription = null,
            description = "চা খোর"
        )
        advanceUntilIdle()
        viewModel.refreshNow()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val entry = state.allDiaryEntries.first { it.description == "চা খোর" }

        // Delete entry
        viewModel.deleteDiaryEntry(entry.id)
        advanceUntilIdle()
        viewModel.refreshNow()
        advanceUntilIdle()

        val afterDeleteState = viewModel.uiState.value
        assertTrue(afterDeleteState.allDiaryEntries.none { it.id == entry.id })
    }
}
