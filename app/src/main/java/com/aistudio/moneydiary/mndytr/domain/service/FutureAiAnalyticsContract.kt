package com.aistudio.moneydiary.mndytr.domain.service

import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry

/**
 * Disabled-by-default interface contract for future AI analytics integration.
 * In Version 2.0 (Offline-First Diary Product), all analytics and insights
 * are 100% deterministic, offline, and private.
 */
interface FutureAiAnalyticsContract {
    val isEnabled: Boolean get() = false

    suspend fun generateDeepInsights(entries: List<DiaryEntry>): List<String> {
        return emptyList()
    }
}

object DefaultFutureAiAnalytics : FutureAiAnalyticsContract
