package com.grvig.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val cardName: String?,
    val notes: String,
    val frequency: String,
    val nextDueDate: String,
    val isActive: Boolean,
    val householdId: String = ""
)