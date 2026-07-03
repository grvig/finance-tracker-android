package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grvig.financetracker.repository.RecurringExpenseRepository

class RecurringExpenseViewModelFactory(
    private val repository: RecurringExpenseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                RecurringExpenseViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return RecurringExpenseViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
