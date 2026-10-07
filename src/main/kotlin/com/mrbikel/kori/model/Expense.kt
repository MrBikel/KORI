package com.mrbikel.kori.model

import java.util.Locale

data class Expense(
    val name: String,
    val category: String,
    val amount: Double,
    val month: String,
) {
    init {
        require(name.isNotBlank()) { "Expense name cannot be blank." }
        require(category.isNotBlank()) { "Expense category cannot be blank." }
        require(amount >= 0.0) { "Expense amount cannot be negative." }
        require(month.isNotBlank()) { "Expense month cannot be blank." }
    }

    val normalizedCategory: String
        get() = category.trim().lowercase(Locale.getDefault())
}
