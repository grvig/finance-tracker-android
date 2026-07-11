package com.grvig.financetracker.data

data class Expense(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val paymentMethod: String = "",
    val cardName: String? = null,
    val description: String = "",
    val notes: String = "",
    val date: String = "",
    val time: String = "",
    val isRecurring: Boolean = false,
    val householdId: String = ""
)
