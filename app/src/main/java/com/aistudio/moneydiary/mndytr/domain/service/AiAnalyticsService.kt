package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import com.aistudio.moneydiary.mndytr.domain.model.*

enum class AiDataSharingPreference(val labelBn: String) {
    NEVER_SHARE("কখনোই শেয়ার নয়"),
    ASK_EVERY_TIME("প্রতিবার অনুমতি নিন")
}

data class AiAggregatedPrivacyPayload(
    val currentPeriodExpensePaisa: Long,
    val previousPeriodExpensePaisa: Long,
    val currentPeriodIncomePaisa: Long,
    val previousPeriodIncomePaisa: Long,
    val percentageChange: Double?,
    val topCategoryShares: List<Pair<String, Double>>, // Category Name & Percentage only
    val necessarySpendingPercentage: Double,
    val discretionarySpendingPercentage: Double,
    val activeGoalCount: Int,
    val goalsOnTrackCount: Int,
    val totalBorrowedOutstandingPaisa: Long,
    val totalLentOutstandingPaisa: Long,
    val totalEarnedOutstandingPaisa: Long,
    val overdueObligationsCount: Int
)

object AiAnalyticsService {

    private const val PREFS_NAME = "kori_ai_prefs"
    private const val KEY_AI_ENABLED = "ai_assistance_enabled"
    private const val KEY_DATA_SHARING = "ai_data_sharing_pref"

    fun isAiEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AI_ENABLED, false) // Default: OFF
    }

    fun setAiEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AI_ENABLED, enabled).apply()
    }

    fun getDataSharingPreference(context: Context): AiDataSharingPreference {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_DATA_SHARING, AiDataSharingPreference.NEVER_SHARE.name) ?: AiDataSharingPreference.NEVER_SHARE.name
        return try {
            AiDataSharingPreference.valueOf(name)
        } catch (_: Exception) {
            AiDataSharingPreference.NEVER_SHARE
        }
    }

    fun setDataSharingPreference(context: Context, pref: AiDataSharingPreference) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DATA_SHARING, pref.name).apply()
    }

    /**
     * Extracts an aggregated, privacy-hardened fact payload.
     * Guaranteed to EXCLUDE:
     * - Party names
     * - Client names
     * - Phone numbers
     * - Email addresses
     * - Free-text descriptions
     * - Private notes
     * - PIN / Passwords / Recovery keys
     * - Cloud / Backup tokens
     */
    fun createAggregatedPayload(
        insights: AnalysisInsights?,
        debtSummary: DebtReceivablesSummary
    ): AiAggregatedPrivacyPayload {
        val monthComp = insights?.monthVsLastMonth
        val currentExp = monthComp?.currentExpensePaisa ?: 0L
        val prevExp = monthComp?.previousExpensePaisa ?: 0L
        val currentInc = monthComp?.currentIncomePaisa ?: 0L
        val prevInc = monthComp?.previousIncomePaisa ?: 0L
        val pctChange = monthComp?.percentageDifference

        val topCats = insights?.categoryShares?.take(5)?.map {
            it.categoryNameBn to it.percentageOfTotal
        } ?: emptyList()

        val necMap = insights?.necessityBreakdown ?: emptyMap()
        val necTotal = (necMap[NecessityClassification.ESSENTIAL] ?: 0L) +
                (necMap[NecessityClassification.REGULAR] ?: 0L) +
                (necMap[NecessityClassification.EMERGENCY] ?: 0L)
        val discTotal = necMap[NecessityClassification.DISCRETIONARY] ?: 0L
        val classifiedSum = Math.max(1L, necTotal + discTotal)

        val necPct = (necTotal.toDouble() / classifiedSum.toDouble()) * 100.0
        val discPct = (discTotal.toDouble() / classifiedSum.toDouble()) * 100.0

        val goals = insights?.frugalityGoalsProgress ?: emptyList()
        val onTrack = goals.count { !it.isExceeded }

        return AiAggregatedPrivacyPayload(
            currentPeriodExpensePaisa = currentExp,
            previousPeriodExpensePaisa = prevExp,
            currentPeriodIncomePaisa = currentInc,
            previousPeriodIncomePaisa = prevInc,
            percentageChange = pctChange,
            topCategoryShares = topCats,
            necessarySpendingPercentage = necPct,
            discretionarySpendingPercentage = discPct,
            activeGoalCount = goals.size,
            goalsOnTrackCount = onTrack,
            totalBorrowedOutstandingPaisa = debtSummary.totalBorrowedOutstandingPaisa,
            totalLentOutstandingPaisa = debtSummary.totalLentOutstandingPaisa,
            totalEarnedOutstandingPaisa = debtSummary.totalEarnedOutstandingPaisa,
            overdueObligationsCount = debtSummary.overdueCount
        )
    }

    /**
     * Deterministic, respectful Bengali insight generated without any external AI call.
     * Always available even when offline or AI is disabled.
     */
    fun getDeterministicLocalReflection(
        insights: AnalysisInsights?,
        debtSummary: DebtReceivablesSummary
    ): String {
        val monthComp = insights?.monthVsLastMonth
        if (monthComp == null || monthComp.currentExpensePaisa == 0L) {
            return "নিয়মিত হিসাব লিখে রাখলে কড়ি আপনার ব্যয় বিশ্লেষণ ও মিতব্যয়িতার লক্ষ্য সাজিয়ে তুলবে।"
        }

        val sb = java.lang.StringBuilder()
        if (monthComp.isExpenseIncrease) {
            sb.append("চলতি মাসে আপনার মোট ব্যয় বিগত মাসের চেয়ে কিছুটা বেড়েছে।")
        } else {
            sb.append("বিগত মাসের তুলনায় চলতি মাসে আপনার ব্যয় নিয়ন্ত্রণে রয়েছে।")
        }

        if (debtSummary.totalBorrowedOutstandingPaisa > 0L) {
            sb.append(" একই সাথে আপনার চলমান ঋণ রয়েছে; ইচ্ছাধীন খরচগুলোতে কিছুটা নজর রাখা সহায়ক হতে পারে।")
        } else if (debtSummary.totalEarnedOutstandingPaisa > 0L) {
            sb.append(" কাজের বকেয়া পাওনা আদায়ের দিকে লক্ষ্য রাখতে পারেন।")
        }

        return sb.toString()
    }
}
