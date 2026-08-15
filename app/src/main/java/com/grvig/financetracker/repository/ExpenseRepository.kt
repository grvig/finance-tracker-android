package com.grvig.financetracker.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.Expense
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    /**
     * Emits the household's expenses and then again on every remote change, so
     * one member's edit shows up for the others without a manual refresh.
     */
    fun observeExpenses(householdId: String): Flow<List<Expense>> = callbackFlow {

        if (householdId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = db.collection("households")
            .document(householdId)
            .collection("expenses")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                trySend(
                    snapshot?.documents?.mapNotNull {
                        it.toObject(Expense::class.java)
                    } ?: emptyList()
                )
            }

        awaitClose { registration.remove() }
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
