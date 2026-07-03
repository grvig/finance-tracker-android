package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grvig.financetracker.data.RecurringExpense
import com.grvig.financetracker.repository.RecurringExpenseRepository
import kotlinx.coroutines.launch

class RecurringExpenseViewModel(
    private val repository: RecurringExpenseRepository
) : ViewModel() {

    fun insertRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        viewModelScope.launch {
            repository.insertRecurringExpense(recurringExpense)
        }
    }

    fun updateRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        viewModelScope.launch {
            repository.updateRecurringExpense(recurringExpense)
        }
    }

    fun deleteRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(recurringExpense)
        }
    }

    suspend fun getAllRecurringExpenses():
            List<RecurringExpense> {
        return repository.getAllRecurringExpenses()
    }
}
