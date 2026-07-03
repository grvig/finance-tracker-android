package com.grvig.financetracker.repository

import com.grvig.financetracker.dao.RecurringExpenseDao
import com.grvig.financetracker.data.RecurringExpense

class RecurringExpenseRepository(
    private val recurringExpenseDao: RecurringExpenseDao
) {

    suspend fun insertRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        recurringExpenseDao.insertRecurringExpense(recurringExpense)
    }

    suspend fun updateRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        recurringExpenseDao.updateRecurringExpense(recurringExpense)
    }

    suspend fun deleteRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        recurringExpenseDao.deleteRecurringExpense(recurringExpense)
    }

    suspend fun getAllRecurringExpenses():
            List<RecurringExpense> {
        return recurringExpenseDao.getAllRecurringExpenses()
    }
}
