package com.grvig.financetracker.repository

import com.grvig.financetracker.dao.ExpenseDao
import com.grvig.financetracker.data.Expense

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {

    suspend fun insertExpense(
        expense: Expense
    ) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(
        expense: Expense
    ) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(
        expense: Expense
    ) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun getAllExpenses():
            List<Expense> {
        return expenseDao.getAllExpenses()
    }
}