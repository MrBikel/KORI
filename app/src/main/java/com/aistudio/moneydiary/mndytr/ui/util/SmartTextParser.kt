package com.aistudio.moneydiary.mndytr.ui.util

import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode

data class ParsedSmartEntry(
    val rawText: String,
    val eventCode: String,
    val amountPaisa: Long,
    val suggestedCategory: CategoryEntity? = null,
    val suggestedAccount: AccountEntity? = null,
    val personName: String? = null,
    val workTitle: String? = null,
    val description: String,
    val isWorkAdvance: Boolean = false,
    val isValid: Boolean = true,
    val validationError: String? = null
)

object SmartTextParser {

    private val BENGALI_DIGITS = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    private val CASH_ALIASES = listOf("নগদ", "ক্যাশ", "cash", "nogod", "nagad cash", "হাতে")
    private val BKASH_ALIASES = listOf("বিকাশ", "bkash", "bikash", "b-kash")
    private val NAGAD_ALIASES = listOf("নগদ একাউন্ট", "nagad", "nogod account")
    private val BANK_ALIASES = listOf("ব্যাংক", "bank", "সিটি ব্যাংক", "ডাচ বাংলা", "brac", "ebl")

    private val WORK_ADVANCE_KEYWORDS = listOf("টোকেন", "অগ্রিম", "advance", "token", "বায়না", "কাজের টোকেন", "কাজের অগ্রিম")
    private val SALARY_KEYWORDS = listOf("বেতন", "salary", "honorarium", "সম্মানী")

    /**
     * Parses Bengali/English natural text entry into structured financial transaction suggestions.
     */
    fun parse(
        input: String,
        availableAccounts: List<AccountEntity>,
        availableCategories: List<CategoryEntity>
    ): ParsedSmartEntry {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ParsedSmartEntry(
                rawText = input,
                eventCode = LedgerEventCode.TX_02_EXPENSE.name,
                amountPaisa = 0L,
                description = "",
                isValid = false,
                validationError = "অনুগ্রহ করে বিবরণ ও টাকার পরিমাণ লিখুন"
            )
        }

        // 1. Extract tokens
        val tokens = trimmed.split(Regex("\\s+"))

        // 2. Find amount token
        var amountPaisa: Long? = null
        var amountTokenIndex = -1

        for ((idx, token) in tokens.withIndex()) {
            val normalizedToken = normalizeNumerals(token.replace(",", "").replace("৳", "").replace("$", ""))
            val parsedLong = normalizedToken.toLongOrNull()
            if (parsedLong != null && parsedLong > 0) {
                amountPaisa = parsedLong * 100L
                amountTokenIndex = idx
                break
            }
        }

        if (amountPaisa == null || amountPaisa <= 0L) {
            return ParsedSmartEntry(
                rawText = input,
                eventCode = LedgerEventCode.TX_02_EXPENSE.name,
                amountPaisa = 0L,
                description = trimmed,
                isValid = false,
                validationError = "টাকার পরিমাণ শনাক্ত করা যায়নি (উদাঃ চা ১০ নগদ)"
            )
        }

        // 3. Detect account
        var matchedAccount: AccountEntity? = null
        val lowerInput = trimmed.lowercase()

        for (acc in availableAccounts) {
            val accNameLower = acc.name.lowercase()
            val accTypeLower = acc.type.lowercase()
            if (lowerInput.contains(accNameLower) || (accTypeLower == "cash" && CASH_ALIASES.any { lowerInput.contains(it) })
                || (accNameLower.contains("bkash") && BKASH_ALIASES.any { lowerInput.contains(it) })
                || (accNameLower.contains("nagad") && NAGAD_ALIASES.any { lowerInput.contains(it) })
                || (accTypeLower == "bank" && BANK_ALIASES.any { lowerInput.contains(it) })
            ) {
                matchedAccount = acc
                break
            }
        }

        // Default to first active Cash or available account if not found
        if (matchedAccount == null) {
            matchedAccount = availableAccounts.firstOrNull { it.type == "CASH" } ?: availableAccounts.firstOrNull()
        }

        // 4. Detect if Work Advance
        val isWorkAdvance = WORK_ADVANCE_KEYWORDS.any { lowerInput.contains(it) }
        if (isWorkAdvance) {
            // Extract person name and work title
            var personName: String? = null
            var workTitle: String = "অনলাইন কাজ"

            // Look for name before "কাজ" or "অনলাইন"
            val beforeAmount = tokens.take(amountTokenIndex).joinToString(" ")
            val afterAmount = tokens.drop(amountTokenIndex + 1).joinToString(" ")

            val textWithoutKeywords = (beforeAmount + " " + afterAmount)
                .replace("টোকেন", "")
                .replace("অগ্রিম", "")
                .replace("বিকাশ", "")
                .replace("নগদ", "")
                .replace("ব্যাংক", "")
                .replace("bkash", "")
                .replace("cash", "")
                .trim()

            val words = textWithoutKeywords.split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.isNotEmpty()) {
                personName = words.first()
                if (words.size > 1) {
                    workTitle = words.drop(1).joinToString(" ")
                }
            }

            return ParsedSmartEntry(
                rawText = input,
                eventCode = LedgerEventCode.TX_03_ADVANCE_RECEIVED.name,
                amountPaisa = amountPaisa,
                suggestedAccount = matchedAccount,
                personName = personName ?: "ক্লায়েন্ট",
                workTitle = workTitle,
                description = "কাজের অগ্রিম: $workTitle (${personName ?: "ক্লায়েন্ট"})",
                isWorkAdvance = true,
                isValid = true
            )
        }

        // 5. Detect if Salary / Direct Income
        val isSalaryOrIncome = SALARY_KEYWORDS.any { lowerInput.contains(it) } || lowerInput.contains("আয়") || lowerInput.contains("income")
        if (isSalaryOrIncome) {
            val incomeCat = availableCategories.firstOrNull { it.isIncomeCategory && (it.nameBn.contains("বেতন") || it.nameBn.contains("আয়")) }
                ?: availableCategories.firstOrNull { it.isIncomeCategory }

            return ParsedSmartEntry(
                rawText = input,
                eventCode = LedgerEventCode.TX_01_INCOME.name,
                amountPaisa = amountPaisa,
                suggestedCategory = incomeCat,
                suggestedAccount = matchedAccount,
                description = if (lowerInput.contains("salary") || lowerInput.contains("বেতন")) "বেতন আয়" else "সাধারণ আয়",
                isWorkAdvance = false,
                isValid = true
            )
        }

        // 6. Otherwise: Expense (Tea, Cigarette, Rickshaw, Breakfast, Recharge, etc.)
        val remainingTokens = tokens.filterIndexed { index, _ -> index != amountTokenIndex }
        val remainingText = remainingTokens.joinToString(" ")

        var matchedCategory: CategoryEntity? = null
        for (cat in availableCategories.filter { !it.isIncomeCategory }) {
            val nameLower = cat.nameBn.lowercase()
            if (lowerInput.contains(nameLower) || (cat.nameBn.contains("চা") && lowerInput.contains("চা"))
                || (cat.nameBn.contains("সিগারেট") && lowerInput.contains("সিগারেট"))
                || (cat.nameBn.contains("যাতায়াত") && (lowerInput.contains("রিকশা") || lowerInput.contains("বাস") || lowerInput.contains("ভাড়া")))
                || (cat.nameBn.contains("খাবার") && (lowerInput.contains("নাশতা") || lowerInput.contains("নাস্তা") || lowerInput.contains("দুপুর") || lowerInput.contains("রাত")))
                || (cat.nameBn.contains("মোবাইল") && (lowerInput.contains("রিচার্জ") || lowerInput.contains("recharge")))
            ) {
                matchedCategory = cat
                break
            }
        }

        if (matchedCategory == null) {
            matchedCategory = availableCategories.firstOrNull { !it.isIncomeCategory }
        }

        val description = if (remainingText.isNotEmpty()) {
            remainingText.replace("বিকাশ", "").replace("নগদ", "").replace("ব্যাংক", "").replace("bkash", "").replace("cash", "").trim()
        } else {
            matchedCategory?.nameBn ?: "দৈনন্দিন খরচ"
        }

        return ParsedSmartEntry(
            rawText = input,
            eventCode = LedgerEventCode.TX_02_EXPENSE.name,
            amountPaisa = amountPaisa,
            suggestedCategory = matchedCategory,
            suggestedAccount = matchedAccount,
            description = if (description.isEmpty()) (matchedCategory?.nameBn ?: "দৈনন্দিন খরচ") else description,
            isWorkAdvance = false,
            isValid = true
        )
    }

    private fun normalizeNumerals(str: String): String {
        val sb = StringBuilder()
        for (ch in str) {
            if (BENGALI_DIGITS.containsKey(ch)) {
                sb.append(BENGALI_DIGITS[ch])
            } else if (ch.isDigit()) {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
