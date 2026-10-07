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

data class BudgetSummary(
    val month: String,
    val monthlyBudget: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val net: Double,
    val remaining: Double,
    val categories: Map<String, Double>,
    val riskLevel: String,
    val topCategory: Pair<String, Double>?,
)
