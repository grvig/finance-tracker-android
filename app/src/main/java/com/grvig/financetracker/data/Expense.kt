package com.grvig.financetracker.data

data class Expense(
    val id: Int,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val cardName: String?,
    val description: String,
    val notes: String,
    val date: String,
    val time: String,
    val isRecurring: Boolean
)