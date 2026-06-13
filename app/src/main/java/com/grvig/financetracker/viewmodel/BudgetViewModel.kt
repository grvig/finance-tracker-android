package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grvig.financetracker.data.Budget
import com.grvig.financetracker.repository.BudgetRepository
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    fun insertBudget(
        budget: Budget
    ) {
        viewModelScope.launch {
            repository.insertBudget(budget)
        }
    }

    fun updateBudget(
        budget: Budget
    ) {
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun deleteBudget(
        budget: Budget
    ) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    suspend fun getAllBudgets():
            List<Budget> {
        return repository.getAllBudgets()
    }
}