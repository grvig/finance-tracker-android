package com.grvig.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.grvig.financetracker.data.Household
import com.grvig.financetracker.data.UserProfile
import com.grvig.financetracker.repository.HouseholdRepository

class HouseholdViewModel(
    private val repository: HouseholdRepository
) : ViewModel() {

    suspend fun getUserProfile(
        userId: String
    ): UserProfile? {
        return repository.getUserProfile(userId)
    }

    suspend fun saveUserProfile(
        userId: String,
        email: String
    ) {
        repository.saveUserProfile(userId, email)
    }

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

    suspend fun getMemberEmails(
        householdId: String
    ): Map<String, String> {
        return repository.getMemberEmails(householdId)
    }

    suspend fun leaveHousehold(
        householdId: String,
        userId: String
    ): Result<Unit> {
        return repository.leaveHousehold(householdId, userId)
    }
}
