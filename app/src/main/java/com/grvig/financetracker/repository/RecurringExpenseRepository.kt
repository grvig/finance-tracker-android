package com.grvig.financetracker.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.RecurringExpense
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RecurringExpenseRepository {

    private val db = FirebaseFirestore.getInstance()

    /** Emits again on every remote change. See ExpenseRepository.observeExpenses. */
    fun observeRecurringExpenses(
        householdId: String
    ): Flow<List<RecurringExpense>> = callbackFlow {

        if (householdId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = db.collection("households")
            .document(householdId)
            .collection("recurringExpenses")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                trySend(
                    snapshot?.documents?.mapNotNull {
                        it.toObject(RecurringExpense::class.java)
                    } ?: emptyList()
                )
            }

        awaitClose { registration.remove() }
    }

    private fun recurringExpenses() =
        db.collection("households")
            .document(SessionManager.currentHouseholdId)
            .collection("recurringExpenses")

    suspend fun insertRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        try {
            val docRef = recurringExpenses().document()

            docRef.set(
                recurringExpense.copy(
                    id = docRef.id,
                    householdId = SessionManager.currentHouseholdId,
                    addedBy = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )
            ).await()
        } catch (e: Exception) {
        }
    }

    suspend fun updateRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        try {
            recurringExpenses().document(recurringExpense.id)
                .set(recurringExpense)
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun deleteRecurringExpense(
        recurringExpense: RecurringExpense
    ) {
        try {
            recurringExpenses().document(recurringExpense.id)
                .delete()
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun getAllRecurringExpenses():
            List<RecurringExpense> {
        return try {
            recurringExpenses()
                .get()
                .await()
                .documents
                .mapNotNull {
                    it.toObject(RecurringExpense::class.java)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
