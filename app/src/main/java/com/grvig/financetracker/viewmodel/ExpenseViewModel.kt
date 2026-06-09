package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

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