package com.mrbikel.kori

import com.mrbikel.kori.model.FinancialTransaction
import com.mrbikel.kori.model.TransactionType
import com.mrbikel.kori.repository.FinanceRepository
import com.mrbikel.kori.service.FinanceService
import java.time.LocalDate
import java.time.YearMonth

fun main() {
    val repository = FinanceRepository().apply {
        addTransactions(
            FinancialTransaction(
                id = "txn-001",
                title = "Salary",
                category = "Income",
                amount = 5000.0,
                type = TransactionType.INCOME,
                date = LocalDate.of(2026, 10, 1),
            ),
            FinancialTransaction(
                id = "txn-002",
                title = "Rent",
                category = "Housing",
                amount = 1250.0,
                type = TransactionType.EXPENSE,
                date = LocalDate.of(2026, 10, 2),
            ),
            FinancialTransaction(
                id = "txn-003",
                title = "Groceries",
                category = "Food",
                amount = 620.0,
                type = TransactionType.EXPENSE,
                date = LocalDate.of(2026, 10, 5),
            ),
            FinancialTransaction(
                id = "txn-004",
                title = "Fuel",
                category = "Transport",
                amount = 370.0,
                type = TransactionType.EXPENSE,
                date = LocalDate.of(2026, 10, 10),
            ),
            FinancialTransaction(
                id = "txn-005",
                title = "Utilities",
                category = "Utilities",
                amount = 240.0,
                type = TransactionType.EXPENSE,
                date = LocalDate.of(2026, 10, 14),
            ),
            FinancialTransaction(
                id = "txn-006",
                title = "Streaming",
                category = "Lifestyle",
                amount = 190.0,
                type = TransactionType.EXPENSE,
                date = LocalDate.of(2026, 10, 18),
            ),
        )
    }

    val month = YearMonth.of(2026, 10)
    val service = FinanceService(repository = repository, monthlyBudget = 4200.0)
    val summary = service.summarize(month)

    println("KORI Finance Dashboard")
    println("Month: ${summary.month}")
    println("Monthly budget: $${service.formatCurrency(summary.monthlyBudget)}")
    println("Total income: $${service.formatCurrency(summary.totalIncome)}")
    println("Total expenses: $${service.formatCurrency(summary.totalExpenses)}")
    println("Net: $${service.formatCurrency(summary.net)}")
    println("Remaining: $${service.formatCurrency(summary.remaining)}")
    println("Risk level: ${summary.riskLevel}")

    if (summary.topCategory != null) {
        println("Top spending category: ${summary.topCategory.first} - $${service.formatCurrency(summary.topCategory.second)}")
    }

    println()
    println("Category breakdown:")
    summary.categories.forEach { (category, total) ->
        println("- $category: $${service.formatCurrency(total)}")
    }
}
