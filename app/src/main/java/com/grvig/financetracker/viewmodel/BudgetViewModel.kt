package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.Budget
import com.grvig.financetracker.repository.BudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    val budgets: StateFlow<List<Budget>> = SessionManager.householdId
        .flatMapLatest { householdId ->
            repository.observeBudgets(householdId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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

}