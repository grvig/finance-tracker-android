package com.grvig.financetracker.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.grvig.financetracker.data.Household
import com.grvig.financetracker.data.UserProfile
import com.grvig.financetracker.data.defaultCategories
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

    suspend fun leaveHousehold(
        householdId: String,
        userId: String
    ): Result<Unit> {
        return try {

            val household = getHousehold(householdId)
                ?: return Result.failure(
                    Exception("Household not found")
                )

            val updatedMembers = household.memberIds - userId

            households.document(householdId)
                .update("memberIds", updatedMembers)
                .await()

            users.document(userId)
                .set(
                    mapOf("householdId" to ""),
                    SetOptions.merge()
                )
                .await()

            Result.success(Unit)
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

    suspend fun getMemberEmails(
        householdId: String
    ): Map<String, String> {
        return try {

            val household = getHousehold(householdId)
                ?: return emptyMap()

            val emails = mutableMapOf<String, String>()

            for (memberId in household.memberIds) {
                val profile = getUserProfile(memberId)
                if (profile != null) {
                    emails[memberId] = profile.email
                }
            }

            emails
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getCategories(
        householdId: String
    ): List<String> {
        val household = getHousehold(householdId)
        val categories = household?.categories ?: emptyList()
        return categories.ifEmpty { defaultCategories }
    }

    suspend fun addCategory(
        householdId: String,
        category: String
    ): Result<Unit> {
        return try {

            val current = getCategories(householdId)

            if (current.contains(category)) {
                return Result.failure(
                    Exception("Category already exists")
                )
            }

            households.document(householdId)
                .update("categories", current + category)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCategory(
        householdId: String,
        category: String
    ): Result<Unit> {
        return try {

            val current = getCategories(householdId)

            if (current.size <= 1) {
                return Result.failure(
                    Exception("At least one category is required")
                )
            }

            households.document(householdId)
                .update("categories", current - category)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(
        userId: String,
        email: String
    ) {
        try {
            users.document(userId)
                .set(
                    UserProfile(
                        uid = userId,
                        email = email
                    )
                )
                .await()
        } catch (e: Exception) {
        }
    }

    private suspend fun linkUserToHousehold(
        userId: String,
        householdId: String
    ) {
        users.document(userId)
            .set(
                mapOf("householdId" to householdId),
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
