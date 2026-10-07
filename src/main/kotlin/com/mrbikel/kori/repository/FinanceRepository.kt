package com.mrbikel.kori.repository

import com.mrbikel.kori.model.FinancialTransaction
import com.mrbikel.kori.model.TransactionType
import java.time.YearMonth

class FinanceRepository(
    private val transactions: MutableList<FinancialTransaction> = mutableListOf(),
) {
    fun addTransaction(transaction: FinancialTransaction) {
        transactions.add(transaction)
    }

    fun addTransactions(vararg transactionList: FinancialTransaction) {
        transactions.addAll(transactionList)
    }

    fun getTransactions(): List<FinancialTransaction> = transactions.toList()

    fun transactionsForMonth(month: YearMonth): List<FinancialTransaction> =
        transactions.filter { YearMonth.from(it.date) == month }

    fun totalIncomeForMonth(month: YearMonth): Double =
        transactionsForMonth(month)
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

    fun totalExpensesForMonth(month: YearMonth): Double =
        transactionsForMonth(month)
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

    fun categoryBreakdownForMonth(month: YearMonth): Map<String, Double> =
        transactionsForMonth(month)
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .toSortedMap()
}
