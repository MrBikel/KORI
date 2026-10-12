package com.mrbikel.kori.service

import com.mrbikel.kori.model.BudgetSummary
import com.mrbikel.kori.repository.BudgetRepository
import kotlin.math.roundToInt

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
            totalExpenses = totalSpent,
            totalSpent = totalSpent,
            remaining = remaining,
            categories = categories,
            riskLevel = riskLevel,
            topCategory = topCategory(),
        )
    }

    fun topCategory(): Pair<String, Double>? {
        return repository.expensesByCategory().maxByOrNull { it.value }?.toPair()
    }

    fun formatCurrency(amount: Double): String {
        return "$%,.2f".format(java.util.Locale.US, amount)
    }
}
