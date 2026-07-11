package com.grvig.financetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grvig.financetracker.dao.BudgetDao
import com.grvig.financetracker.dao.RecurringExpenseDao
import com.grvig.financetracker.data.Budget
import com.grvig.financetracker.data.RecurringExpense

@Database(
    entities = [
        Budget::class,
        RecurringExpense::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao

    abstract fun recurringExpenseDao(): RecurringExpenseDao
}
