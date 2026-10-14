package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CsvImportServiceTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testBengaliNumeralsConversion() {
        val bengali = "১২৩৪৫৬৭৮৯০"
        val english = CsvImportService.convertBengaliDigitsToEnglish(bengali)
        assertEquals("1234567890", english)

        val mixed = "৳ ৫,২৫০.৭৫ BDT"
        val converted = CsvImportService.convertBengaliDigitsToEnglish(mixed)
        assertEquals("৳ 5,250.75 BDT", converted)
    }

    @Test
    fun testBengaliCsvPreviewAndImport() {
        val existingCat = CategoryEntity(
            id = "cat_salary",
            nameBn = "বেতন",
            nameEn = "Salary",
            iconName = "work",
            colorHex = "#059669",
            isIncomeCategory = true,
            isArchived = false,
            displayOrder = 0
        )
        db.categoryDao().insert(existingCat)

        val csvContent = """
            Taxonomy,Type,Amount_BDT,Category,Source_Party,Date_Time,Description,Necessity,Private_Notes
            INCOME,Income,৫০০০.০০,বেতন,অফিস,2026-10-14 10:00:00,অক্টোবর মাসের বেতন,প্রয়োজনীয়,বোনাস সহ
            EXPENSE,Expense,২৫০.৫০,দৈনিক বাজার,নগদ,2026-10-14 11:30:00,চাল ও ডাল কেনা,অপরিহার্য,তাজা পণ্য
            EXPENSE,Expense,invalid_amount,ভুল সারি,নগদ,2026-10-14 12:00:00,ভুল তথ্য,অনির্ধারিত,
        """.trimIndent()

        val preview = CsvImportService.previewCsv(db, csvContent)

        assertEquals(3, preview.totalRows)
        assertEquals(2, preview.validRows.size)
        assertEquals(1, preview.invalidRows.size)
        assertEquals(4, preview.invalidRows[0].rowNumber) // line 4 of CSV file
        assertEquals(500000L, preview.totalIncomePaisa)
        assertEquals(25050L, preview.totalExpensePaisa)

        // Execute import
        val result = CsvImportService.executeImport(context, db, preview, skipDuplicates = true)
        assertTrue(result is CsvImportResult.Success)
        val success = result as CsvImportResult.Success
        assertEquals(2, success.importedCount)
        assertEquals(0, success.skippedDuplicates)
        assertEquals(1, success.invalidRowCount)

        // Verify transactions persisted in database
        val savedTxs = db.ledgerEventDao().getAllNonDeleted()
        assertEquals(2, savedTxs.size)

        val incomeTx = savedTxs.find { it.eventKind == "INCOME" }
        assertNotNull(incomeTx)
        assertEquals(500000L, incomeTx?.amountPaisa)
        assertEquals("অক্টোবর মাসের বেতন", incomeTx?.description)
        assertEquals("অফিস", incomeTx?.sourceDescription)
        assertEquals("বোনাস সহ", incomeTx?.notes)

        val expenseTx = savedTxs.find { it.eventKind == "EXPENSE" }
        assertNotNull(expenseTx)
        assertEquals(25050L, expenseTx?.amountPaisa)
        assertEquals("চাল ও ডাল কেনা", expenseTx?.description)

        // Verify duplicate detection on second preview
        val secondPreview = CsvImportService.previewCsv(db, csvContent)
        assertEquals(2, secondPreview.duplicateRows.size)
        assertEquals(0, secondPreview.newRows.size)

        // Second import with skipDuplicates = true should skip duplicates safely
        val secondResult = CsvImportService.executeImport(context, db, secondPreview, skipDuplicates = true)
        assertTrue(secondResult is CsvImportResult.Success)
        val secondSuccess = secondResult as CsvImportResult.Success
        assertEquals(0, secondSuccess.importedCount)
        assertEquals(2, secondSuccess.skippedDuplicates)
    }
}
