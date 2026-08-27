package com.grvig.financetracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.grvig.financetracker.repository.ExpenseRepository
import com.grvig.financetracker.repository.HouseholdRepository

/**
 * Periodically checks for new expenses from followed household members and
 * posts a notification for each. Runs on WorkManager's schedule, so the delay
 * between an expense being added and the alert is roughly the poll interval.
 */
class ExpenseNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (uid.isBlank()) return Result.success()

        if (!NotificationPreferences.isEnabled(applicationContext, uid)) {
            return Result.success()
        }

        val followed = NotificationPreferences.followedUsers(applicationContext, uid)
        if (followed.isEmpty()) return Result.success()

        val householdId = SessionManager.currentHouseholdId
        if (householdId.isBlank()) return Result.success()

        return try {

            val expenses = ExpenseRepository().getExpensesOnce(householdId)

            val selection = NotificationSelector.select(
                expenses = expenses,
                followedUsers = followed,
                currentUserId = uid,
                lastSeenCreatedAt = NotificationPreferences
                    .lastSeenCreatedAt(applicationContext)
            )

            if (selection.toNotify.isNotEmpty()) {
                val emails = HouseholdRepository().getMemberEmails(householdId)
                NotificationPoster.post(applicationContext, selection.toNotify, emails)
            }

            NotificationPreferences.setLastSeenCreatedAt(
                applicationContext,
                selection.newMarker
            )

            Result.success()

        } catch (e: Exception) {
            // A network blip; let WorkManager try again on its backoff.
            Result.retry()
        }
    }
}
