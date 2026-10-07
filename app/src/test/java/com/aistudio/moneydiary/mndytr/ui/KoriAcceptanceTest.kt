package com.aistudio.moneydiary.mndytr.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification
import com.aistudio.moneydiary.mndytr.domain.service.DataExportService
import com.aistudio.moneydiary.mndytr.domain.service.FinancialService
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
class KoriAcceptanceTest {

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

    /**
     * Section 28 Acceptance Criteria:
     * Expense 1: ৳450, ওষুধ, বেতন থেকে, প্রয়োজনীয়, জ্বরের ওষুধ
     * Income: ৳500, অনলাইন কাজ, গ্রাহকের দেওয়া টাকা
     * Expense 2: ৳10, চা, পকেটের টাকা
     * Expected:
     * Income: ৳500 (50,000 paisa)
     * Expense: ৳460 (46,000 paisa)
     * Net change: +৳40 (4,000 paisa)
     */
    @Test
    fun testSection28_standardAcceptanceFlow() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        // 1. Expense 1: ৳450, ওষুধ, বেতন থেকে, প্রয়োজনীয়, জ্বরের ওষুধ
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 45000L, // ৳450
            categoryId = null,
            sourceDescription = "বেতন থেকে",
            description = "জ্বরের ওষুধ",
            notes = "প্যারাসিটামল",
            necessity = NecessityClassification.ESSENTIAL
        )
        advanceUntilIdle()
        viewModel.refreshNow()
        advanceUntilIdle()

        // 2. Income: ৳500, অনলাইন কাজ, গ্রাহকের দেওয়া টাকা
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.INCOME,
            amountPaisa = 50000L, // ৳500
            categoryId = null,
            sourceDescription = "গ্রাহকের দেওয়া টাকা",
            description = "অনলাইন কাজ"
        )
        advanceUntilIdle()
        viewModel.refreshNow()
        advanceUntilIdle()

        // 3. Expense 2: ৳10, চা, পকেটের টাকা
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 1000L, // ৳10
            categoryId = null,
            sourceDescription = "পকেটের টাকা",
            description = "চা",
            necessity = NecessityClassification.REGULAR
        )
        advanceUntilIdle()
        viewModel.refreshNow()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        // Verify totals
        assertEquals(50000L, state.todaySummary.todayIncomePaisa)   // ৳500
        assertEquals(46000L, state.todaySummary.todayExpensePaisa)  // ৳460
        assertEquals(4000L, state.todaySummary.todayNetChangePaisa) // +৳40
        assertTrue(state.todaySummary.hasEntriesToday)

        // Verify entries appear in Today
        assertEquals(3, state.todayEntries.size)

        // Verify entries appear in Diary
        assertEquals(3, state.allDiaryEntries.size)

        // Verify source text is persisted
        val medEntry = state.allDiaryEntries.first { it.description == "জ্বরের ওষুধ" }
        assertEquals("বেতন থেকে", medEntry.sourceDescription)
        assertEquals(NecessityClassification.ESSENTIAL, medEntry.necessity)

        val incomeEntry = state.allDiaryEntries.first { it.description == "অনলাইন কাজ" }
        assertEquals("গ্রাহকের দেওয়া টাকা", incomeEntry.sourceDescription)

        val teaEntry = state.allDiaryEntries.first { it.description == "চা" }
        assertEquals("পকেটের টাকা", teaEntry.sourceDescription)

        // Verify CSV export preserves data
        val csv = DataExportService.generateCsv(state.allDiaryEntries)
        assertTrue(csv.contains("Income,500.0"))
        assertTrue(csv.contains("Expense,450.0"))
        assertTrue(csv.contains("Expense,10.0"))
        assertTrue(csv.contains("বেতন থেকে"))
        assertTrue(csv.contains("গ্রাহকের দেওয়া টাকা"))
        assertTrue(csv.contains("পকেটের টাকা"))

        // Verify JSON export preserves data
        val json = DataExportService.generateJson(state.allDiaryEntries)
        assertTrue(json.contains("\"amountPaisa\": 45000"))
        assertTrue(json.contains("\"amountPaisa\": 50000"))
        assertTrue(json.contains("\"amountPaisa\": 1000"))
    }
}
