package com.aistudio.moneydiary.mndytr.domain.service

import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType

data class ParsedFinancialSms(
    val rawText: String,
    val type: DiaryEntryType,
    val amountPaisa: Long,
    val feePaisa: Long = 0L,
    val senderOrReceiver: String? = null,
    val medium: String, // "bKash", "Nagad", "Rocket", "Bank", "General"
    val trxId: String? = null,
    val balancePaisa: Long? = null,
    val suggestedDescription: String,
    val isValid: Boolean = true
)

object SmsTransactionParser {

    private val BENGALI_DIGITS = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    fun parse(smsText: String): ParsedFinancialSms? {
        val text = smsText.trim()
        if (text.isEmpty()) return null

        val lower = text.lowercase()

        val medium = when {
            lower.contains("nagad") || lower.contains("নগদ") -> "Nagad"
            lower.contains("rocket") || lower.contains("রকেট") -> "Rocket"
            lower.contains("bkash") || lower.contains("বিকাশ") -> "bKash"
            lower.contains("bank") || lower.contains("ব্যাংক") || lower.contains("a/c") -> "Bank"
            else -> "bKash"
        }

        // 1. Received / Cash In
        if (lower.contains("you have received") || lower.contains("received tk") || lower.contains("cash in tk") || lower.contains("credited by")) {
            val amount = extractAmountPaisa(text) ?: return null
            val sender = extractSenderOrParty(text)
            val trxId = extractTrxId(text)
            val desc = if (sender != null) "$medium এ প্রাপ্তি ($sender)" else "$medium এ প্রাপ্তি"

            return ParsedFinancialSms(
                rawText = text,
                type = DiaryEntryType.INCOME,
                amountPaisa = amount,
                senderOrReceiver = sender,
                medium = medium,
                trxId = trxId,
                suggestedDescription = desc
            )
        }

        // 2. Payment or Send Money
        if (lower.contains("payment tk") || lower.contains("send money tk") || lower.contains("paid tk")) {
            val amount = extractAmountPaisa(text) ?: return null
            val receiver = extractSenderOrParty(text)
            val trxId = extractTrxId(text)
            val fee = extractFeePaisa(text)
            val desc = if (receiver != null) "$medium পেমেন্ট ($receiver)" else "$medium পেমেন্ট"

            return ParsedFinancialSms(
                rawText = text,
                type = DiaryEntryType.EXPENSE,
                amountPaisa = amount,
                feePaisa = fee,
                senderOrReceiver = receiver,
                medium = medium,
                trxId = trxId,
                suggestedDescription = desc
            )
        }

        // 3. Cash Out
        if (lower.contains("cash out tk")) {
            val amount = extractAmountPaisa(text) ?: return null
            val trxId = extractTrxId(text)
            val fee = extractFeePaisa(text)

            return ParsedFinancialSms(
                rawText = text,
                type = DiaryEntryType.EXPENSE,
                amountPaisa = amount,
                feePaisa = fee,
                medium = medium,
                trxId = trxId,
                suggestedDescription = "$medium ক্যাশ আউট"
            )
        }

        // 4. Bank Debit
        if (lower.contains("debited") || lower.contains("withdrawn") || lower.contains("pos purchase")) {
            val amount = extractAmountPaisa(text) ?: return null
            return ParsedFinancialSms(
                rawText = text,
                type = DiaryEntryType.EXPENSE,
                amountPaisa = amount,
                medium = "Bank",
                trxId = extractTrxId(text),
                suggestedDescription = "ব্যাংক অ্যাকাউন্ট থেকে ব্যয়"
            )
        }

        // 5. Fallback: Check if there's Tk/BDT or টাকা with amount
        val fallbackAmount = extractAmountPaisa(text)
        if (fallbackAmount != null && fallbackAmount > 0L) {
            val isIncome = lower.contains("পেলাম") || lower.contains("জমা") || lower.contains("বেতন") || lower.contains("প্রাপ্তি")
            return ParsedFinancialSms(
                rawText = text,
                type = if (isIncome) DiaryEntryType.INCOME else DiaryEntryType.EXPENSE,
                amountPaisa = fallbackAmount,
                medium = medium,
                suggestedDescription = text.take(40)
            )
        }

        return null
    }

    private fun extractAmountPaisa(text: String): Long? {
        val normalized = normalizeDigits(text)
        // 1. Explicit currency prefix/suffix: "Tk 500.00", "BDT 1,500.00", "debited by BDT 1,500.00"
        val explicitRegex = Regex("""(?:tk\.?|bdt|টাকা|by|for)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(?:tk|bdt|টাকা)?""", RegexOption.IGNORE_CASE)
        val explicitMatch = explicitRegex.find(normalized)
        if (explicitMatch != null) {
            val numStr = explicitMatch.groupValues[1].replace(",", "").trim()
            val numDouble = numStr.toDoubleOrNull()
            if (numDouble != null && numDouble > 0.0) {
                return (numDouble * 100.0).toLong()
            }
        }

        // 2. Fallback: Any number with decimal point like "1,500.00"
        val decimalRegex = Regex("""([0-9]{1,3}(?:,[0-9]{3})+(?:\.[0-9]{1,2})?|[0-9]+\.[0-9]{1,2})""")
        val decimalMatch = decimalRegex.find(normalized)
        if (decimalMatch != null) {
            val numStr = decimalMatch.groupValues[1].replace(",", "").trim()
            val numDouble = numStr.toDoubleOrNull()
            if (numDouble != null && numDouble > 0.0) {
                return (numDouble * 100.0).toLong()
            }
        }

        return null
    }

    private fun extractFeePaisa(text: String): Long {
        val normalized = normalizeDigits(text)
        val regex = Regex("""fee\s*(?:tk\.?)?\s*([0-9]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
        val match = regex.find(normalized) ?: return 0L
        val feeVal = match.groupValues[1].toDoubleOrNull() ?: 0.0
        return (feeVal * 100.0).toLong()
    }

    private fun extractTrxId(text: String): String? {
        val regex = Regex("""(?:trxid|trx\s*id|tran\s*id|reference)[\s:]*([A-Za-z0-9_-]+)""", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.getOrNull(1)
    }

    private fun extractSenderOrParty(text: String): String? {
        val regexFrom = Regex("""from\s+([0-9A-Za-z\s_-]{3,20})(?:\.|\s|fee)""", RegexOption.IGNORE_CASE)
        val regexTo = Regex("""to\s+([0-9A-Za-z\s_-]{3,20})(?:\.|\s|fee)""", RegexOption.IGNORE_CASE)
        return regexFrom.find(text)?.groupValues?.getOrNull(1)?.trim()
            ?: regexTo.find(text)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            val eng = BENGALI_DIGITS[c]
            if (eng != null) {
                sb.append(eng)
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}
