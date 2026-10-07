package com.mrbikel.kori.service

import com.mrbikel.kori.model.BudgetSummary
import com.mrbikel.kori.repository.FinanceRepository
import java.time.YearMonth

class FinanceService(
    private val repository: FinanceRepository,
    private val monthlyBudget: Double,
) {
    init {
        require(monthlyBudget >= 0.0) { "Monthly budget cannot be negative." }
    }

    fun summarize(month: YearMonth): BudgetSummary {
        val totalIncome = repository.totalIncomeForMonth(month)
        val totalExpenses = repository.totalExpensesForMonth(month)
        val remaining = monthlyBudget - totalExpenses
        val net = totalIncome - totalExpenses
        val categories = repository.categoryBreakdownForMonth(month)
        val topCategory = categories.maxByOrNull { it.value }?.toPair()
        val riskLevel = when {
            totalExpenses > monthlyBudget -> "Critical"
            totalExpenses >= monthlyBudget * 0.8 -> "Watch"
            else -> "Healthy"
        }

        return BudgetSummary(
            month = month.toString(),
            monthlyBudget = monthlyBudget,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            net = net,
            remaining = remaining,
            categories = categories,
            riskLevel = riskLevel,
            topCategory = topCategory,
        )
    }

    fun formatCurrency(amount: Double): String {
        return "$%.2f".format(amount)
    }
}
