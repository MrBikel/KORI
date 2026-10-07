package com.aistudio.moneydiary.mndytr.ui.util

import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.domain.model.MoneyPaisa
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BengaliFormatter {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val bengaliMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        return MoneyPaisa.normalizeDigits(input)
    }

    fun formatNumber(number: Int, useBengaliDigits: Boolean = true): String {
        val str = number.toString()
        return if (useBengaliDigits) toBengaliDigits(str) else str
    }

    fun formatPaisa(
        paisa: Long,
        useBengaliDigits: Boolean = true,
        showDecimals: Boolean = false,
        includeCurrencySymbol: Boolean = true
    ): String {
        val money = MoneyPaisa.fromPaisa(paisa)
        val formatted = if (useBengaliDigits) {
            money.toBengaliFormattedTaka(showDecimals)
        } else {
            money.toFormattedTaka(showDecimals)
        }
        return if (includeCurrencySymbol) formatted else formatted.replace("৳ ", "")
    }

    fun parseAmountToPaisa(input: String): Long {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return 0L
        return try {
            MoneyPaisa.parse(trimmed).paisa
        } catch (_: Exception) {
            0L
        }
    }

    fun formatBengaliDate(timestampMs: Long): String {
        return formatDate(timestampMs, true)
    }

    fun formatDate(timestampMs: Long, useBengaliDigits: Boolean = true): String {
        val eventCal = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val day = eventCal.get(Calendar.DAY_OF_MONTH)
        val month = eventCal.get(Calendar.MONTH)
        val year = eventCal.get(Calendar.YEAR)
        return if (useBengaliDigits) {
            "${toBengaliDigits(day.toString())} ${bengaliMonths[month]} ${toBengaliDigits(year.toString())}"
        } else {
            "$day ${bengaliMonths[month]} $year"
        }
    }

    fun formatDisplayDate(timestampMs: Long, useBengaliDigits: Boolean = true): String {
        val now = Calendar.getInstance()
        val eventCal = Calendar.getInstance().apply { timeInMillis = timestampMs }

        val isToday = now.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterdayCal.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                yesterdayCal.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(timestampMs))
            .replace("AM", "সকাল")
            .replace("PM", "সন্ধ্যা")

        val displayTime = if (useBengaliDigits) toBengaliDigits(timeStr) else timeStr

        return when {
            isToday -> "আজ, $displayTime"
            isYesterday -> "গতকাল, $displayTime"
            else -> {
                val day = eventCal.get(Calendar.DAY_OF_MONTH)
                val month = eventCal.get(Calendar.MONTH)
                val year = eventCal.get(Calendar.YEAR)
                val datePart = if (useBengaliDigits) {
                    "${toBengaliDigits(day.toString())} ${bengaliMonths[month]} ${toBengaliDigits(year.toString())}"
                } else {
                    "$day ${bengaliMonths[month]} $year"
                }
                "$datePart, $displayTime"
            }
        }
    }

    fun formatShortDate(timestampMs: Long, useBengaliDigits: Boolean = true): String {
        return formatDate(timestampMs, useBengaliDigits)
    }

    fun getEventCodeBn(code: String): String {
        return when (code) {
            LedgerEventCode.TX_00_OPENING_BALANCE.name -> "প্রারম্ভিক স্থিতি"
            LedgerEventCode.TX_01_INCOME.name -> "সরাসরি আয়"
            LedgerEventCode.TX_02_EXPENSE.name -> "দৈনন্দিন খরচ"
            LedgerEventCode.TX_03_ADVANCE_RECEIVED.name -> "কাজের অগ্রিম গ্রহণ"
            LedgerEventCode.TX_04_ADVANCE_RETURNED.name -> "অগ্রিম ফেরত প্রদান"
            LedgerEventCode.TX_05_RECEIVABLE_CREATED.name -> "পাওনা তৈরি"
            LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name -> "বকেয়া আদায়"
            LedgerEventCode.TX_07_BORROWED_MONEY.name -> "হাওলাত / ঋণ গ্রহণ"
            LedgerEventCode.TX_08_EXPENSE_PAYABLE_CREATED.name -> "বকেয়া খরচ দায়ের স্বীকৃতি"
            LedgerEventCode.TX_09_DEBT_REPAID.name -> "ঋণ পরিশোধ"
            LedgerEventCode.TX_10_LENT_MONEY.name -> "ধার / ঋণ প্রদান"
            LedgerEventCode.TX_11_LOAN_COLLECTED.name -> "ধারের টাকা ফেরত গ্রহণ"
            LedgerEventCode.TX_12_REFUND_RECEIVED.name -> "খরচ ফেরত গ্রহণ"
            LedgerEventCode.TX_13_REFUND_PAID.name -> "রিফান্ড প্রদান"
            LedgerEventCode.TX_14_TRANSFER.name -> "নিজস্ব স্থানান্তর"
            LedgerEventCode.TX_14F_TRANSFER_FEE.name -> "স্থানান্তর ফি"
            LedgerEventCode.TX_15_SAVINGS_ALLOCATION.name -> "সঞ্চয়ে স্থানান্তর"
            LedgerEventCode.TX_16_BALANCE_ADJUSTMENT.name -> "ব্যালেন্স সমন্বয়"
            LedgerEventCode.TX_17_REVERSAL.name -> "লেনদেন বাতিল / রিভার্সাল"
            LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name -> "অগ্রিম আয় স্বীকৃতি"
            LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION.name -> "বকেয়া বিল আয় স্বীকৃতি"
            LedgerEventCode.REC_03_BAD_DEBT_WRITEOFF.name -> "অনাদায়ী পাওনা অবলোপন"
            else -> code
        }
    }
}
