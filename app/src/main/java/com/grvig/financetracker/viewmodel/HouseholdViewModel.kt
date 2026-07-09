package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.grvig.financetracker.data.Household
import com.grvig.financetracker.repository.HouseholdRepository

class HouseholdViewModel(
    private val repository: HouseholdRepository
) : ViewModel() {

    suspend fun createHousehold(
        userId: String
    ): Result<Household> {
        return repository.createHousehold(userId)
    }

    suspend fun joinHousehold(
        code: String,
        userId: String
    ): Result<Household> {
        return repository.joinHousehold(code, userId)
    }

    suspend fun getHousehold(
        householdId: String
    ): Household? {
        return repository.getHousehold(householdId)
    }
}
