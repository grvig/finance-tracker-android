package com.grvig.financetracker.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.grvig.financetracker.SessionManager
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.data.RecurringExpense
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime

private fun nextDueDateAfter(
    currentDueDate: String,
    frequency: String
): String {

    val dueDate = LocalDate.parse(currentDueDate)

    val advancedDate = if (frequency == "Weekly") {
        dueDate.plusWeeks(1)
    } else {
        dueDate.plusMonths(1)
    }

    return advancedDate.toString()
}

class RecurringExpenseRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Turns due recurring expenses into real expenses.
     *
     * Each one is claimed in a transaction that re-reads the due date and bails
     * if another member already advanced it. Without that, two people opening
     * the app together would both log the same bill.
     *
     * Advances one period per run, so a bill several periods overdue catches up
     * over successive opens.
     *
     * @return how many expenses this call actually created.
     */
    suspend fun generateDueExpenses(
        householdId: String,
        today: String
    ): Int {

        if (householdId.isBlank()) {
            return 0
        }

        return try {

            val household = db.collection("households").document(householdId)
            val recurringRef = household.collection("recurringExpenses")
            val expensesRef = household.collection("expenses")

            val addedBy = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            // Filtered client side so this needs no composite index.
            val candidates = recurringRef
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(RecurringExpense::class.java) }
                .filter { it.isActive && it.nextDueDate <= today && it.id.isNotBlank() }

            var generated = 0

            candidates.forEach { candidate ->

                val claimed = db.runTransaction { transaction ->

                    val docRef = recurringRef.document(candidate.id)
                    val snapshot = transaction.get(docRef)

                    val currentDue = snapshot.getString("nextDueDate")
                    val stillActive = snapshot.getBoolean("isActive") ?: false
                    val frequency = snapshot.getString("frequency") ?: "Monthly"

                    if (currentDue == null || !stillActive || currentDue > today) {
                        return@runTransaction false
                    }

                    val expenseRef = expensesRef.document()

                    transaction.set(
                        expenseRef,
                        Expense(
                            id = expenseRef.id,
                            amount = snapshot.getDouble("amount") ?: 0.0,
                            category = snapshot.getString("category") ?: "",
                            paymentMethod = snapshot.getString("paymentMethod") ?: "",
                            cardName = snapshot.getString("cardName"),
                            description = snapshot.getString("title") ?: "",
                            notes = snapshot.getString("notes") ?: "",
                            date = currentDue,
                            time = LocalTime.now().toString(),
                            isRecurring = true,
                            householdId = householdId,
                            addedBy = addedBy
                        )
                    )

                    transaction.update(
                        docRef,
                        "nextDueDate",
                        nextDueDateAfter(currentDue, frequency)
                    )

                    true
                }.await()

                if (claimed) {
                    generated++
                }
            }

            generated

        } catch (e: Exception) {
            0
        }
    }

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
