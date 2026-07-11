package com.grvig.financetracker.data

data class Budget(
    val id: String = "",
    val category: String = "",
    val monthlyLimit: Double = 0.0,
    val warningPercent: Int = 0,
    val householdId: String = ""
)
