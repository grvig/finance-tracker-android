package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    /**
     * Live household expenses. Rebuilt when the household changes, and only
     * listening while a screen is actually collecting.
     */
    val expenses: StateFlow<List<Expense>> = SessionManager.householdId
        .flatMapLatest { householdId ->
            repository.observeExpenses(householdId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun insertExpense(
        expense: Expense
    ) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun updateExpense(
        expense: Expense
    ) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(
        expense: Expense
    ) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

}