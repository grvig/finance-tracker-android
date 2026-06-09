package com.grvig.financetracker.data

data class RecurringExpense(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val cardName: String?,
    val notes: String,
    val frequency: String,
    val nextDueDate: String,
    val isActive: Boolean
)