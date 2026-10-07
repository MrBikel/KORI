package com.aistudio.moneydiary.mndytr.ui

import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.ui.util.SmartTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmartTextParserUnitTest {

    private lateinit var accounts: List<AccountEntity>
    private lateinit var categories: List<CategoryEntity>

    @Before
    fun setUp() {
        accounts = listOf(
            AccountEntity(id = "acc_cash", name = "নগদ", type = "CASH"),
            AccountEntity(id = "acc_bkash", name = "bKash", type = "MFS_BKASH"),
            AccountEntity(id = "acc_nagad", name = "Nagad", type = "MFS_NAGAD"),
            AccountEntity(id = "acc_bank", name = "ব্যাংক", type = "BANK"),
            AccountEntity(id = "acc_savings", name = "সঞ্চয়", type = "SAVINGS")
        )

        categories = listOf(
            CategoryEntity(id = "cat_tea", nameBn = "চা ও নাশতা", nameEn = "Tea & Snacks", iconName = "local_cafe", colorHex = "#795548", isIncomeCategory = false),
            CategoryEntity(id = "cat_cig", nameBn = "সিগারেট ও তামাক", nameEn = "Cigarettes", iconName = "smoking_rooms", colorHex = "#607D8B", isIncomeCategory = false),
            CategoryEntity(id = "cat_transport", nameBn = "যাতায়াত ও ভ্রমণ", nameEn = "Transport", iconName = "directions_bike", colorHex = "#FF9800", isIncomeCategory = false),
            CategoryEntity(id = "cat_salary", nameBn = "বেতন ও পারিশ্রমিক", nameEn = "Salary", iconName = "payments", colorHex = "#1B5E20", isIncomeCategory = true),
            CategoryEntity(id = "cat_general_income", nameBn = "অন্যান্য আয়", nameEn = "Other Income", iconName = "attach_money", colorHex = "#388E3C", isIncomeCategory = true)
        )
    }

    @Test
    fun testSmartEntryTea() {
        val result = SmartTextParser.parse("চা ১০ নগদ", accounts, categories)
        assertTrue(result.isValid)
        assertEquals(1000L, result.amountPaisa) // ৳10
        assertEquals(LedgerEventCode.TX_02_EXPENSE.name, result.eventCode)
        assertEquals("acc_cash", result.suggestedAccount?.id)
        assertTrue(result.description.contains("চা"))
    }

    @Test
    fun testSmartEntryCigarette() {
        val result = SmartTextParser.parse("সিগারেট ২০", accounts, categories)
        assertTrue(result.isValid)
        assertEquals(2000L, result.amountPaisa) // ৳20
        assertEquals(LedgerEventCode.TX_02_EXPENSE.name, result.eventCode)
        assertFalse(result.isWorkAdvance)
    }

    @Test
    fun testSmartEntryRickshawBkash() {
        val result = SmartTextParser.parse("রিকশা ৩০ বিকাশ", accounts, categories)
        assertTrue(result.isValid)
        assertEquals(3000L, result.amountPaisa) // ৳30
        assertEquals(LedgerEventCode.TX_02_EXPENSE.name, result.eventCode)
        assertEquals("acc_bkash", result.suggestedAccount?.id)
    }

    @Test
    fun testSmartEntryWorkAdvance() {
        val result = SmartTextParser.parse("রহিম অনলাইন কাজের টোকেন ১০০ বিকাশ", accounts, categories)
        assertTrue(result.isValid)
        assertTrue(result.isWorkAdvance)
        assertEquals(10000L, result.amountPaisa) // ৳100
        assertEquals(LedgerEventCode.TX_03_ADVANCE_RECEIVED.name, result.eventCode)
        assertEquals("acc_bkash", result.suggestedAccount?.id)
        assertEquals("রহিম", result.personName)
        // Work advance must not be direct income
        assertTrue(result.eventCode != LedgerEventCode.TX_01_INCOME.name)
    }

    @Test
    fun testSmartEntryEnglishSalary() {
        val result = SmartTextParser.parse("Salary 25000 Bank", accounts, categories)
        assertTrue(result.isValid)
        assertEquals(2500000L, result.amountPaisa) // ৳25,000
        assertEquals(LedgerEventCode.TX_01_INCOME.name, result.eventCode)
        assertEquals("acc_bank", result.suggestedAccount?.id)
    }

    @Test
    fun testSmartEntryAmbiguousInputRejection() {
        val emptyResult = SmartTextParser.parse("", accounts, categories)
        assertFalse(emptyResult.isValid)
        assertNotNull(emptyResult.validationError)

        val noAmountResult = SmartTextParser.parse("শুধু চা এবং বিস্কুট", accounts, categories)
        assertFalse(noAmountResult.isValid)
        assertNotNull(noAmountResult.validationError)
    }
}
