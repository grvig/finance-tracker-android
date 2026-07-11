package com.grvig.financetracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grvig.financetracker.dao.RecurringExpenseDao
import com.grvig.financetracker.data.RecurringExpense

@Database(
    entities = [
        RecurringExpense::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recurringExpenseDao(): RecurringExpenseDao
}
