package com.grvig.financetracker.data

data class Budget(
    val id: Int,
    val category: String,
    val monthlyLimit: Double,
    val warningPercent: Int
)