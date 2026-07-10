package com.grvig.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val monthlyLimit: Double,
    val warningPercent: Int,
    val householdId: String = ""
)