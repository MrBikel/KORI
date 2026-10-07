package com.mrbikel.kori.service

import com.mrbikel.kori.repository.BudgetRepository
import kotlin.math.roundToInt

data class BudgetSummary(
    val monthlyBudget: Double,
    val totalSpent: Double,
    val remaining: Double,
    val categories: Map<String, Double>,
    val riskLevel: String,
)

class BudgetAnalyzer(
    private val monthlyBudget: Double,
    private val repository: BudgetRepository,
) {
    init {
        require(monthlyBudget >= 0.0) { "Monthly budget cannot be negative." }
    }

    fun summarize(): BudgetSummary {
        val totalSpent = repository.totalSpent()
        val remaining = monthlyBudget - totalSpent
        val categories = repository.expensesByCategory()
        val riskLevel = when {
            totalSpent > monthlyBudget -> "High"
            totalSpent >= monthlyBudget * 0.8 -> "Moderate"
            else -> "Low"
        }

        return BudgetSummary(
            monthlyBudget = monthlyBudget,
            totalSpent = totalSpent,
            remaining = remaining,
            categories = categories,
            riskLevel = riskLevel,
        )
    }

    fun topCategory(): Pair<String, Double>? {
        return repository.expensesByCategory().maxByOrNull { it.value }?.toPair()
    }

    fun formatCurrency(amount: Double): String {
        val rounded = (amount * 100.0).roundToInt() / 100.0
        return "$%.2f".format(rounded)
    }
}
