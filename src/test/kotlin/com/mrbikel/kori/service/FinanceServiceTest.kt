package com.mrbikel.kori.service

import com.mrbikel.kori.model.FinancialTransaction
import com.mrbikel.kori.model.TransactionType
import com.mrbikel.kori.repository.FinanceRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class FinanceServiceTest {
    @Test
    fun `summarize returns correct financial metrics`() {
        val repository = FinanceRepository().apply {
            addTransactions(
                FinancialTransaction(
                    id = "inc-1",
                    title = "Salary",
                    category = "Income",
                    amount = 5000.0,
                    type = TransactionType.INCOME,
                    date = LocalDate.of(2026, 10, 1),
                ),
                FinancialTransaction(
                    id = "exp-1",
                    title = "Rent",
                    category = "Housing",
                    amount = 1250.0,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 10, 2),
                ),
                FinancialTransaction(
                    id = "exp-2",
                    title = "Groceries",
                    category = "Food",
                    amount = 620.0,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 10, 5),
                ),
            )
        }

        val service = FinanceService(repository = repository, monthlyBudget = 4200.0)
        val summary = service.summarize(YearMonth.of(2026, 10))

        assertEquals(5000.0, summary.totalIncome)
        assertEquals(1870.0, summary.totalExpenses)
        assertEquals(3130.0, summary.net)
        assertEquals(2330.0, summary.remaining)
        assertEquals("Healthy", summary.riskLevel)
    }

    @Test
    fun `top category is reported correctly`() {
        val repository = FinanceRepository().apply {
            addTransactions(
                FinancialTransaction(
                    id = "exp-1",
                    title = "Rent",
                    category = "Housing",
                    amount = 1250.0,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 10, 2),
                ),
                FinancialTransaction(
                    id = "exp-2",
                    title = "Groceries",
                    category = "Food",
                    amount = 620.0,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 10, 5),
                ),
            )
        }

        val service = FinanceService(repository = repository, monthlyBudget = 4200.0)
        val summary = service.summarize(YearMonth.of(2026, 10))

        assertEquals("Housing", summary.topCategory?.first)
        assertEquals(1250.0, summary.topCategory?.second)
    }
}
