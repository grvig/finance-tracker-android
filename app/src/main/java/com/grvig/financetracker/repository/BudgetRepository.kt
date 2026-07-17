package com.grvig.financetracker.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.Budget
import kotlinx.coroutines.tasks.await

class BudgetRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun budgets() =
        db.collection("households")
            .document(SessionManager.currentHouseholdId)
            .collection("budgets")

    suspend fun insertBudget(
        budget: Budget
    ) {
        try {
            val docRef = budgets().document()

            docRef.set(
                budget.copy(
                    id = docRef.id,
                    householdId = SessionManager.currentHouseholdId
                )
            ).await()
        } catch (e: Exception) {
        }
    }

    suspend fun updateBudget(
        budget: Budget
    ) {
        try {
            budgets().document(budget.id)
                .set(budget)
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun deleteBudget(
        budget: Budget
    ) {
        try {
            budgets().document(budget.id)
                .delete()
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getAllBudgets():
            List<Budget> {
        return try {
            budgets()
                .get()
                .await()
                .documents
                .mapNotNull {
                    it.toObject(Budget::class.java)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
