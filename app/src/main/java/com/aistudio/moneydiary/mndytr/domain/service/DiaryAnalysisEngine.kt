package com.aistudio.moneydiary.mndytr.domain.service

import com.aistudio.moneydiary.mndytr.domain.model.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import java.util.Calendar

data class AnalysisInsights(
    val todayVsYesterday: PeriodComparison?,
    val weekVsLastWeek: PeriodComparison?,
    val monthVsLastMonth: PeriodComparison?,
    val yearVsLastYear: PeriodComparison?,
    val highestSpendingCategory: CategoryShare?,
    val fastestGrowingCategory: CategoryDifferenceContribution?,
    val mostFrequentExpenseCategory: Pair<String, Int>?,
    val repeatedSmallExpenses: List<CategoryShare>,
    val categoryShares: List<CategoryShare>,
    val necessityBreakdown: Map<NecessityClassification, Long>,
    val frugalityGoalsProgress: List<FrugalityGoalProgress>,
    val singleHighlightInsight: String?
)

object DiaryAnalysisEngine {

    /**
     * Categorizes whether a category is inherently essential/vital (medical, education, housing, bills, family)
     * so that spending increases are described neutrally rather than as "wasteful".
     */
    fun isEssentialVitalCategory(categoryName: String): Boolean {
        val norm = categoryName.lowercase()
        return norm.contains("ওষুধ") || norm.contains("চিকিৎসা") ||
                norm.contains("শিক্ষা") || norm.contains("বই") ||
                norm.contains("বাড়ি") || norm.contains("বাসা") ||
                norm.contains("বিদ্যুৎ") || norm.contains("গ্যাস") ||
                norm.contains("পানি") || norm.contains("পরিবার")
    }

    /**
     * Builds comparison between two sets of entries (e.g. current month vs previous month).
     */
    fun comparePeriods(
        currentPeriodLabel: String,
        previousPeriodLabel: String,
        currentEntries: List<DiaryEntry>,
        previousEntries: List<DiaryEntry>,
        useBengaliDigits: Boolean = true
    ): PeriodComparison? {
        if (currentEntries.isEmpty() && previousEntries.isEmpty()) {
            return null
        }

        val currentExpense = currentEntries.filter { it.type == DiaryEntryType.EXPENSE }.sumOf { it.amountPaisa }
        val previousExpense = previousEntries.filter { it.type == DiaryEntryType.EXPENSE }.sumOf { it.amountPaisa }
        val currentIncome = currentEntries.filter { it.type == DiaryEntryType.INCOME }.sumOf { it.amountPaisa }
        val previousIncome = previousEntries.filter { it.type == DiaryEntryType.INCOME }.sumOf { it.amountPaisa }

        val diffPaisa = currentExpense - previousExpense
        val absDiffPaisa = Math.abs(diffPaisa)
        val isIncrease = diffPaisa > 0

        // Mathematical validity: cannot divide by 0
        val percentageDiff: Double? = if (previousExpense > 0) {
            ((diffPaisa.toDouble() / previousExpense.toDouble()) * 100.0)
        } else {
            null
        }

        // Category breakdown differences
        val currentByCat = currentEntries.filter { it.type == DiaryEntryType.EXPENSE }
            .groupBy { it.categoryNameBn }
            .mapValues { entry -> entry.value.sumOf { it.amountPaisa } }

        val prevByCat = previousEntries.filter { it.type == DiaryEntryType.EXPENSE }
            .groupBy { it.categoryNameBn }
            .mapValues { entry -> entry.value.sumOf { it.amountPaisa } }

        val allCategories = (currentByCat.keys + prevByCat.keys).toSet()
        val contributions = allCategories.map { cat ->
            val cur = currentByCat[cat] ?: 0L
            val prev = prevByCat[cat] ?: 0L
            val catDiff = cur - prev
            CategoryDifferenceContribution(
                categoryNameBn = cat,
                differencePaisa = catDiff,
                isIncrease = catDiff > 0
            )
        }.filter { it.differencePaisa != 0L }
            .sortedByDescending { Math.abs(it.differencePaisa) }
            .take(3)

        // Generate respectful Bengali narrative
        val narrative = buildNarrative(
            currentPeriodLabel = currentPeriodLabel,
            isIncrease = isIncrease,
            absDiffPaisa = absDiffPaisa,
            contributions = contributions,
            useBengaliDigits = useBengaliDigits
        )

        return PeriodComparison(
            currentPeriodLabel = currentPeriodLabel,
            previousPeriodLabel = previousPeriodLabel,
            currentExpensePaisa = currentExpense,
            previousExpensePaisa = previousExpense,
            currentIncomePaisa = currentIncome,
            previousIncomePaisa = previousIncome,
            absoluteDifferencePaisa = absDiffPaisa,
            isExpenseIncrease = isIncrease,
            percentageDifference = percentageDiff,
            topContributingCategories = contributions,
            narrative = narrative
        )
    }

    private fun buildNarrative(
        currentPeriodLabel: String,
        isIncrease: Boolean,
        absDiffPaisa: Long,
        contributions: List<CategoryDifferenceContribution>,
        useBengaliDigits: Boolean
    ): String {
        if (absDiffPaisa == 0L) {
            return "$currentPeriodLabel ব্যয়ের পরিমাণ অপরিবর্তিত রয়েছে।"
        }

        val diffFormatted = BengaliFormatter.formatPaisa(absDiffPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)
        val sb = StringBuilder()
        if (isIncrease) {
            sb.append("$currentPeriodLabel মোট ব্যয় $diffFormatted বেড়েছে।")
        } else {
            sb.append("$currentPeriodLabel মোট ব্যয় $diffFormatted কম হয়েছে।")
        }

        if (contributions.isNotEmpty()) {
            sb.append("\nপ্রধান কারণ:")
            contributions.forEach { c ->
                val sign = if (c.isIncrease) "+" else "−"
                val catFormatted = BengaliFormatter.formatPaisa(Math.abs(c.differencePaisa), useBengaliDigits = useBengaliDigits, showDecimals = false)
                sb.append("\n${c.categoryNameBn}: $sign$catFormatted")
            }

            // Check if discretionary categories grew
            val discretionaryGrowth = contributions.filter { it.isIncrease && !isEssentialVitalCategory(it.categoryNameBn) }
            if (discretionaryGrowth.isNotEmpty()) {
                val names = discretionaryGrowth.joinToString(", ") { it.categoryNameBn }
                sb.append("\n\nইচ্ছাধীন খাতগুলোর মধ্যে $names ব্যয় পর্যালোচনা করতে পারেন।")
            }
        }

        return sb.toString()
    }

    /**
     * Calculates shares of total expenses by category.
     */
    fun calculateCategoryShares(entries: List<DiaryEntry>): List<CategoryShare> {
        val expenseEntries = entries.filter { it.type == DiaryEntryType.EXPENSE }
        val totalExpense = expenseEntries.sumOf { it.amountPaisa }
        if (totalExpense == 0L) return emptyList()

        return expenseEntries.groupBy { it.categoryNameBn }
            .map { (catName, list) ->
                val sum = list.sumOf { it.amountPaisa }
                val iconKey = list.firstOrNull()?.categoryIconKey ?: "category"
                val pct = (sum.toDouble() / totalExpense.toDouble()) * 100.0
                CategoryShare(
                    categoryNameBn = catName,
                    iconKey = iconKey,
                    amountPaisa = sum,
                    percentageOfTotal = pct
                )
            }.sortedByDescending { it.amountPaisa }
    }

    /**
     * Detects repeated small expenses (such as tea, cigarettes, rickshaws under ৳100 with >= 3 occurrences).
     */
    fun findRepeatedSmallExpenses(entries: List<DiaryEntry>): List<CategoryShare> {
        val expenseEntries = entries.filter { it.type == DiaryEntryType.EXPENSE }
        val smallThresholdPaisa = 10000L // ৳100

        return expenseEntries.filter { it.amountPaisa <= smallThresholdPaisa }
            .groupBy { it.categoryNameBn }
            .filter { it.value.size >= 2 }
            .map { (name, list) ->
                val total = list.sumOf { it.amountPaisa }
                val iconKey = list.firstOrNull()?.categoryIconKey ?: "category"
                CategoryShare(
                    categoryNameBn = "$name (${list.size} বার)",
                    iconKey = iconKey,
                    amountPaisa = total,
                    percentageOfTotal = 0.0
                )
            }.sortedByDescending { it.amountPaisa }
    }

    /**
     * Calculates progress for a frugality goal.
     */
    fun calculateGoalProgress(
        goal: FrugalityGoal,
        allEntries: List<DiaryEntry>,
        useBengaliDigits: Boolean = true
    ): FrugalityGoalProgress {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Filter entries relevant to this goal period
        val periodEntries = when (goal.periodType) {
            "DAILY" -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                allEntries.filter { it.occurrenceDateEpochMs >= start }
            }
            "WEEKLY" -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                allEntries.filter { it.occurrenceDateEpochMs >= start }
            }
            else -> { // MONTHLY
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                allEntries.filter { it.occurrenceDateEpochMs >= start }
            }
        }

        // Apply category filter if specified
        val relevantExpenses = periodEntries.filter { it.type == DiaryEntryType.EXPENSE }
            .filter { goal.categoryId == null || it.categoryId == goal.categoryId }

        val currentSpending = relevantExpenses.sumOf { it.amountPaisa }
        val remainingAllowance = Math.max(0L, goal.targetAmountPaisa - currentSpending)
        val isExceeded = currentSpending > goal.targetAmountPaisa
        val pctUsed = if (goal.targetAmountPaisa > 0) {
            (currentSpending.toDouble() / goal.targetAmountPaisa.toDouble()) * 100.0
        } else 0.0

        // Construct non-judgmental Bengali guidance message
        val guidanceMessage = when {
            isExceeded -> "সীমা অতিক্রম করেছে। চাইলে খাতটি পর্যালোচনা করতে পারেন।"
            pctUsed >= 85.0 -> "লক্ষ্য থেকে কিছুটা বেশি। ছোট পরিবর্তনে লক্ষ্যটির কাছাকাছি থাকা সম্ভব।"
            pctUsed >= 50.0 -> "পরিকল্পনা অনুযায়ী হিসাব এগিয়ে চলছে।"
            else -> "খরচ নিয়ন্ত্রণে রয়েছে।"
        }

        return FrugalityGoalProgress(
            goal = goal,
            currentSpendingPaisa = currentSpending,
            remainingAllowancePaisa = remainingAllowance,
            percentageUsed = pctUsed,
            isExceeded = isExceeded,
            guidanceMessageBn = guidanceMessage
        )
    }

    /**
     * Produces single evidence-based insight for Today Screen if enough data exists.
     */
    fun generateTodayInsight(
        allEntries: List<DiaryEntry>,
        useBengaliDigits: Boolean = true
    ): String? {
        val expenses = allEntries.filter { it.type == DiaryEntryType.EXPENSE }
        if (expenses.size < 3) return null

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Today start
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        // Yesterday start
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStart = calendar.timeInMillis
        val yesterdayEnd = todayStart - 1

        val todayExpenses = expenses.filter { it.occurrenceDateEpochMs >= todayStart }
        val yesterdayExpenses = expenses.filter { it.occurrenceDateEpochMs in yesterdayStart..yesterdayEnd }

        val todayTotal = todayExpenses.sumOf { it.amountPaisa }
        val yesterdayTotal = yesterdayExpenses.sumOf { it.amountPaisa }

        if (yesterdayTotal > 0 && todayTotal > 0) {
            val diff = todayTotal - yesterdayTotal
            val absDiff = Math.abs(diff)
            val formatted = BengaliFormatter.formatPaisa(absDiff, useBengaliDigits = useBengaliDigits, showDecimals = false)
            return if (diff < 0) {
                "আজ গতকালের তুলনায় $formatted কম ব্যয় হয়েছে।"
            } else if (diff > 0) {
                "আজ গতকালের চেয়ে $formatted বেশি খরচ হয়েছে।"
            } else {
                "আজ ও গতকাল ব্যয়ের পরিমাণ সমান ছিল।"
            }
        }

        // Small repeated expenses insight fallback
        val smallRepeated = findRepeatedSmallExpenses(allEntries)
        if (smallRepeated.isNotEmpty()) {
            val first = smallRepeated.first()
            val formatted = BengaliFormatter.formatPaisa(first.amountPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)
            return "ছোট খাতের হিসাব: ${first.categoryNameBn} বাবদ মোট $formatted ব্যয় হয়েছে।"
        }

        return null
    }
}
