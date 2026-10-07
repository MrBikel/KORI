package com.aistudio.moneydiary.mndytr.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyPaisaUnitTest {

    @Test
    fun testMoneyParsingWithBengaliNumerals() {
        val parsed = MoneyPaisa.parse("৳ ৫০০")
        assertEquals(50000L, parsed.paisa)

        val withDecimals = MoneyPaisa.parse("১২৫০.৫০")
        assertEquals(125050L, withDecimals.paisa)

        val teaBengali = MoneyPaisa.parse("১০")
        assertEquals(1000L, teaBengali.paisa)
    }

    @Test
    fun testMoneyParsingWithEnglishNumerals() {
        val parsed = MoneyPaisa.parse("500")
        assertEquals(50000L, parsed.paisa)

        val withDecimals = MoneyPaisa.parse("1250.50")
        assertEquals(125050L, withDecimals.paisa)

        val withCommas = MoneyPaisa.parse("৳ 1,00,000.75")
        assertEquals(10000075L, withCommas.paisa)
    }

    @Test
    fun testPaisaPrecisionNoFloatRoundingError() {
        val tenPaisa = MoneyPaisa(10L)
        val twentyPaisa = MoneyPaisa(20L)
        val sum = tenPaisa + twentyPaisa
        assertEquals(30L, sum.paisa)
        assertEquals("৳ 0.30", sum.toFormattedTaka())
        assertEquals("৳ ০.৩০", sum.toBengaliFormattedTaka())
    }

    @Test(expected = ArithmeticException::class)
    fun testOverflowRejection() {
        val huge = MoneyPaisa(Long.MAX_VALUE - 10L)
        val more = MoneyPaisa(100L)
        val overflow = huge + more
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeRejection() {
        MoneyPaisa(-50L)
    }

    @Test
    fun testComparisonAndFormatting() {
        val a = MoneyPaisa(10000L)
        val b = MoneyPaisa(20000L)
        assertTrue(a < b)
        assertFalse(a == b)
        assertEquals("৳ 100.00", a.toFormattedTaka())
        assertEquals("৳ ১০০.০০", a.toBengaliFormattedTaka())
    }
}
