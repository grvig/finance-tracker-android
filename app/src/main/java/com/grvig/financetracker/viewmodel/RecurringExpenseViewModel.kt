package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.RecurringExpense
import com.grvig.financetracker.repository.RecurringExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringExpenseViewModel(
    private val repository: RecurringExpenseRepository
) : ViewModel() {

    val recurringExpenses: StateFlow<List<RecurringExpense>> =
        SessionManager.householdId
            .flatMapLatest { householdId ->
                repository.observeRecurringExpenses(householdId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

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
