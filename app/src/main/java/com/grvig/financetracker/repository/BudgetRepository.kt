package com.grvig.financetracker.repository

import com.grvig.financetracker.dao.BudgetDao
import com.grvig.financetracker.data.Budget

class BudgetRepository(
    private val budgetDao: BudgetDao
) {

    suspend fun insertBudget(
        budget: Budget
    ) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(
        budget: Budget
    ) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(
        budget: Budget
    ) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun getAllBudgets():
            List<Budget> {
        return budgetDao.getAllBudgets()
    }
}