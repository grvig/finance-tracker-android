package com.grvig.financetracker.data

data class RecurringExpense(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val paymentMethod: String = "",
    val cardName: String? = null,
    val notes: String = "",
    val frequency: String = "",
    val nextDueDate: String = "",
    val isActive: Boolean = true,
    val householdId: String = ""
)
