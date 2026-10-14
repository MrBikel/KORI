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

    fun loadFromJson(jsonString: String) {
        val entryRegex = Regex("""\{[^}]*?"amount(?:Taka|Paisa)"[^}]*?\}""")
        val matches = entryRegex.findAll(jsonString)

        for (match in matches) {
            val chunk = match.value
            val id = Regex(""""id"\s*:\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1) ?: "txn-${System.nanoTime()}"
            val desc = Regex(""""description"\s*:\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1) ?: "Entry"
            val cat = Regex(""""category(?:NameBn)?"\s*:\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1) ?: "General"
            val typeStr = Regex(""""type"\s*:\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1) ?: "EXPENSE"
            val type = if (typeStr.contains("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE

            val amtTaka = Regex(""""amountTaka"\s*:\s*([0-9.]+)""").find(chunk)?.groupValues?.get(1)?.toDoubleOrNull()
            val amtPaisa = Regex(""""amountPaisa"\s*:\s*([0-9]+)""").find(chunk)?.groupValues?.get(1)?.toLongOrNull()
            val amount = amtTaka ?: (amtPaisa?.let { it / 100.0 } ?: 0.0)

            val dateStr = Regex(""""(?:dateTime|date)"\s*:\s*"([^"]+)"""").find(chunk)?.groupValues?.get(1)
            val date = try {
                if (dateStr != null && dateStr.length >= 10) {
                    java.time.LocalDate.parse(dateStr.substring(0, 10))
                } else {
                    java.time.LocalDate.now()
                }
            } catch (_: Exception) {
                java.time.LocalDate.now()
            }

            if (amount > 0.0) {
                addTransaction(
                    FinancialTransaction(
                        id = id,
                        title = desc,
                        category = cat,
                        amount = amount,
                        type = type,
                        date = date
                    )
                )
            }
        }
    }
}
