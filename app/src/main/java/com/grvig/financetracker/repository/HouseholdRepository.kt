package com.grvig.financetracker.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.grvig.financetracker.data.Household
import com.grvig.financetracker.data.UserProfile
import kotlinx.coroutines.tasks.await

class HouseholdRepository {

    private val db = FirebaseFirestore.getInstance()
    private val households = db.collection("households")
    private val users = db.collection("users")

    suspend fun createHousehold(userId: String): Result<Household> {
        return try {

            val docRef = households.document()

            val household = Household(
                id = docRef.id,
                code = generateCode(),
                memberIds = listOf(userId),
                createdBy = userId
            )

            docRef.set(household).await()
            linkUserToHousehold(userId, household.id)

            Result.success(household)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinHousehold(
        code: String,
        userId: String
    ): Result<Household> {
        return try {

            val snapshot = households
                .whereEqualTo("code", code)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()
                ?: return Result.failure(
                    Exception("No household found with that code")
                )

            val household = document.toObject(Household::class.java)!!
            val updatedMembers = household.memberIds + userId

            households.document(household.id)
                .update("memberIds", updatedMembers)
                .await()

            linkUserToHousehold(userId, household.id)

            Result.success(
                household.copy(memberIds = updatedMembers)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            users.document(userId)
                .get()
                .await()
                .toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getHousehold(householdId: String): Household? {
        return try {
            households.document(householdId)
                .get()
                .await()
                .toObject(Household::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun linkUserToHousehold(
        userId: String,
        householdId: String
    ) {
        users.document(userId)
            .set(
                UserProfile(
                    uid = userId,
                    householdId = householdId
                ),
                SetOptions.merge()
            )
            .await()
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
