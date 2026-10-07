package com.mrbikel.kori.repository

import com.mrbikel.kori.model.Expense

class BudgetRepository(
    private val expenses: MutableList<Expense> = mutableListOf(),
) {
    fun addExpense(expense: Expense) {
        expenses.add(expense)
    }

    fun addExpenses(vararg expenseList: Expense) {
        expenses.addAll(expenseList)
    }

    fun getExpenses(): List<Expense> = expenses.toList()

    fun totalSpent(): Double = expenses.sumOf { it.amount }

    fun expensesByCategory(): Map<String, Double> = expenses
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toSortedMap()
}
