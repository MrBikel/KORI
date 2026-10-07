package com.mrbikel.kori.service

import com.mrbikel.kori.model.Expense
import com.mrbikel.kori.repository.BudgetRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class BudgetAnalyzerTest {
    @Test
    fun `summarize calculates total spent and remaining budget`() {
        val repository = BudgetRepository().apply {
            addExpenses(
                Expense("Groceries", "Food", 300.0, "2026-10"),
                Expense("Rent", "Housing", 900.0, "2026-10"),
                Expense("Transport", "Travel", 150.0, "2026-10"),
            )
        }

        val analyzer = BudgetAnalyzer(monthlyBudget = 1500.0, repository = repository)
        val summary = analyzer.summarize()

        assertEquals(1350.0, summary.totalSpent)
        assertEquals(150.0, summary.remaining)
        assertEquals("Moderate", summary.riskLevel)
    }

    @Test
    fun `top category returns highest spending category`() {
        val repository = BudgetRepository().apply {
            addExpenses(
                Expense("Groceries", "Food", 250.0, "2026-10"),
                Expense("Rent", "Housing", 800.0, "2026-10"),
                Expense("Movies", "Lifestyle", 40.0, "2026-10"),
            )
        }

        val analyzer = BudgetAnalyzer(monthlyBudget = 2000.0, repository = repository)

        assertEquals("Housing", analyzer.topCategory()?.first)
        assertEquals(800.0, analyzer.topCategory()?.second)
    }
}
