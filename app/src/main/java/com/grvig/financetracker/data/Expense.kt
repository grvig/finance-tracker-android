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
    val householdId: String = "",
    val addedBy: String = "",
    // When the row was created, epoch millis. Distinct from `date`, which the
    // user can backdate. Notifications key off this so a backdated expense
    // still alerts when it is entered.
    val createdAt: Long = 0L
)
