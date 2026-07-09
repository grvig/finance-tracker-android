package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grvig.financetracker.repository.HouseholdRepository

class HouseholdViewModelFactory(
    private val repository: HouseholdRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                HouseholdViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return HouseholdViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
