package com.aistudio.moneydiary.mndytr.domain.service

import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsAndExportUnitTest {

    @Test
    fun testBkashReceivedSmsParsing() {
        val sms = "You have received Tk 500.00 from 01712345678. Fee Tk 0.00. Balance Tk 1,200.00. TrxID 9K34M8KL90"
        val parsed = SmsTransactionParser.parse(sms)

        assertNotNull(parsed)
        assertEquals(DiaryEntryType.INCOME, parsed?.type)
        assertEquals(50000L, parsed?.amountPaisa)
        assertEquals("bKash", parsed?.medium)
        assertEquals("9K34M8KL90", parsed?.trxId)
    }

    @Test
    fun testBkashPaymentSmsParsing() {
        val sms = "Payment Tk 350.00 to Merchant Shop. Fee Tk 0.00. Balance Tk 850.00. TrxID 8A2B3C4D"
        val parsed = SmsTransactionParser.parse(sms)

        assertNotNull(parsed)
        assertEquals(DiaryEntryType.EXPENSE, parsed?.type)
        assertEquals(35000L, parsed?.amountPaisa)
        assertEquals("bKash", parsed?.medium)
        assertEquals("8A2B3C4D", parsed?.trxId)
    }

    @Test
    fun testNagadCashInSmsParsing() {
        val sms = "Nagad: Cash In Tk 1,000.00 from 01987654321. Fee Tk 0.00. Balance Tk 2,500.00."
        val parsed = SmsTransactionParser.parse(sms)

        assertNotNull(parsed)
        assertEquals(DiaryEntryType.INCOME, parsed?.type)
        assertEquals(100000L, parsed?.amountPaisa)
        assertEquals("Nagad", parsed?.medium)
    }

    @Test
    fun testBankDebitSmsParsing() {
        val sms = "Your A/C ending 4567 is debited by BDT 1,500.00 on 12-Oct-2026. Ref: ATM-092."
        val parsed = SmsTransactionParser.parse(sms)

        assertNotNull(parsed)
        assertEquals(DiaryEntryType.EXPENSE, parsed?.type)
        assertEquals(150000L, parsed?.amountPaisa)
        assertEquals("Bank", parsed?.medium)
    }

    @Test
    fun testPrintableHtmlStatementGeneration() {
        val entries = listOf(
            DiaryEntry(
                id = "1",
                type = DiaryEntryType.INCOME,
                eventCode = "TX_01_INCOME",
                amountPaisa = 50000L,
                categoryId = null,
                categoryNameBn = "বেতন",
                sourceDescription = "অফিস",
                description = "মাসিক বেতন",
                notes = "বোনাস সহ",
                occurrenceDateEpochMs = 1728691200000L,
                necessity = NecessityClassification.REGULAR
            ),
            DiaryEntry(
                id = "2",
                type = DiaryEntryType.EXPENSE,
                eventCode = "TX_02_EXPENSE",
                amountPaisa = 15000L,
                categoryId = null,
                categoryNameBn = "খাবার",
                sourceDescription = "নগদ",
                description = "বাজার খরচ",
                notes = null,
                occurrenceDateEpochMs = 1728691200000L,
                necessity = NecessityClassification.ESSENTIAL
            )
        )

        val html = DataExportService.generatePrintableHtmlStatement(entries, includePrivateNotes = true)
        assertTrue(html.contains("কড়ি (KORI)"))
        assertTrue(html.contains("মাসিক বেতন"))
        assertTrue(html.contains("বাজার খরচ"))
        assertTrue(html.contains("মোট আয়"))
        assertTrue(html.contains("মোট ব্যয়"))
        assertTrue(html.contains("৳500.00"))
        assertTrue(html.contains("৳150.00"))
        assertTrue(html.contains("bonus সহ") || html.contains("বোনাস সহ"))
    }
}
