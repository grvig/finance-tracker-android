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
        val docRef = expenses().document()

        docRef.set(
            expense.copy(
                id = docRef.id,
                householdId = SessionManager.currentHouseholdId,
                addedBy = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            )
        ).await()
    }

    suspend fun updateExpense(
        expense: Expense
    ) {
        expenses().document(expense.id)
            .set(expense)
            .await()
    }

    suspend fun deleteExpense(
        expense: Expense
    ) {
        expenses().document(expense.id)
            .delete()
            .await()
    }

    suspend fun getAllExpenses():
            List<Expense> {
        return expenses()
            .get()
            .await()
            .documents
            .mapNotNull {
                it.toObject(Expense::class.java)
            }
    }
}
