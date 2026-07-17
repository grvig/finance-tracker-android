package com.grvig.financetracker.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.Expense
import kotlinx.coroutines.tasks.await

class ExpenseRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun expenses() =
        db.collection("households")
            .document(SessionManager.currentHouseholdId)
            .collection("expenses")

    suspend fun insertExpense(
        expense: Expense
    ) {
        try {
            val docRef = expenses().document()

            docRef.set(
                expense.copy(
                    id = docRef.id,
                    householdId = SessionManager.currentHouseholdId,
                    addedBy = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )
            ).await()
        } catch (e: Exception) {
        }
    }

    suspend fun updateExpense(
        expense: Expense
    ) {
        try {
            expenses().document(expense.id)
                .set(expense)
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun deleteExpense(
        expense: Expense
    ) {
        try {
            expenses().document(expense.id)
                .delete()
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getAllExpenses():
            List<Expense> {
        return try {
            expenses()
                .get()
                .await()
                .documents
                .mapNotNull {
                    it.toObject(Expense::class.java)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
