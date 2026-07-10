package com.grvig.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val cardName: String?,
    val description: String,
    val notes: String,
    val date: String,
    val time: String,
    val isRecurring: Boolean,
    val householdId: String = ""
)