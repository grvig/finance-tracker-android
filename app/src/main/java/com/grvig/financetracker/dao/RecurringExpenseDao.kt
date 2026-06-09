package com.grvig.financetracker.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.grvig.financetracker.data.RecurringExpense

@Dao
interface RecurringExpenseDao {

    @Insert
    suspend fun insertRecurringExpense(
        recurringExpense: RecurringExpense
    )

    @Update
    suspend fun updateRecurringExpense(
        recurringExpense: RecurringExpense
    )

    @Delete
    suspend fun deleteRecurringExpense(
        recurringExpense: RecurringExpense
    )

    @Query("SELECT * FROM recurring_expenses")
    suspend fun getAllRecurringExpenses():
            List<RecurringExpense>
}