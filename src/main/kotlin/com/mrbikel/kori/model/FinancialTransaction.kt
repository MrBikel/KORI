package com.mrbikel.kori.model

import java.time.LocalDate

enum class TransactionType {
    INCOME,
    EXPENSE,
}

data class FinancialTransaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val type: TransactionType,
    val date: LocalDate,
) {
    init {
        require(id.isNotBlank()) { "Transaction id cannot be blank." }
        require(title.isNotBlank()) { "Transaction title cannot be blank." }
        require(category.isNotBlank()) { "Transaction category cannot be blank." }
        require(amount >= 0.0) { "Transaction amount cannot be negative." }
    }
}

/**
 * Unified budget and financial summary model shared across KORI services and analyzers.
 */
data class BudgetSummary(
    val month: String = "",
    val monthlyBudget: Double,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSpent: Double = totalExpenses,
    val net: Double = totalIncome - totalExpenses,
    val remaining: Double = monthlyBudget - (if (totalExpenses > 0.0) totalExpenses else totalSpent),
    val categories: Map<String, Double> = emptyMap(),
    val riskLevel: String,
    val topCategory: Pair<String, Double>? = null,
)
