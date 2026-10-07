package com.mrbikel.kori

import com.mrbikel.kori.model.Expense
import com.mrbikel.kori.repository.BudgetRepository
import com.mrbikel.kori.service.BudgetAnalyzer

fun main() {
    val repository = BudgetRepository()
    repository.addExpenses(
        Expense("Groceries", "Food", 320.0, "2026-10"),
        Expense("Rent", "Housing", 900.0, "2026-10"),
        Expense("Internet", "Utilities", 60.0, "2026-10"),
        Expense("Ride share", "Transport", 110.0, "2026-10"),
        Expense("Streaming", "Lifestyle", 25.0, "2026-10"),
    )

    val analyzer = BudgetAnalyzer(monthlyBudget = 1800.0, repository = repository)
    val summary = analyzer.summarize()
    val topCategory = analyzer.topCategory()

    println("KORI Budget Summary")
    println("Monthly budget: $${analyzer.formatCurrency(summary.monthlyBudget)}")
    println("Total spent: $${analyzer.formatCurrency(summary.totalSpent)}")
    println("Remaining: $${analyzer.formatCurrency(summary.remaining)}")
    println("Risk level: ${summary.riskLevel}")

    if (topCategory != null) {
        println("Top category: ${topCategory.first} - $${analyzer.formatCurrency(topCategory.second)}")
    }
}
