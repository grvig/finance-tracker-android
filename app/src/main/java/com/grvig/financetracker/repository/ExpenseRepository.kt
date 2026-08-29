package com.grvig.financetracker.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
                    addedBy = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    createdAt = System.currentTimeMillis()
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
     * One-shot fetch for background work, which cannot hold a Flow. Takes the
     * household explicitly rather than reading the session singleton so it is
     * safe to call from a Worker.
     *
     * Only rows newer than the marker are read, so the notification poll costs
     * a handful of documents rather than the whole history, and a long spell
     * offline cannot produce a wall of alerts.
     */
    suspend fun getExpensesCreatedAfter(
        householdId: String,
        createdAfter: Long,
        limit: Long = 20
    ): List<Expense> {
        if (householdId.isBlank()) return emptyList()
        return try {
            db.collection("households")
                .document(householdId)
                .collection("expenses")
                .whereGreaterThan("createdAt", createdAfter)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Expense::class.java) }
        } catch (e: Exception) {
            emptyList()
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

}
