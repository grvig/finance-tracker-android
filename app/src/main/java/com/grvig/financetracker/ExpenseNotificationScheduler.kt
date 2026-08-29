package com.grvig.financetracker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

/**
 * Turns the notification poll on or off. Callers use [refresh] after anything
 * that could change whether polling should run: sign-in, app start, or a change
 * on the notification settings screen.
 */
object ExpenseNotificationScheduler {

    private const val WORK_NAME = "expense_notifications"
    private const val WORK_NAME_NOW = "expense_notifications_now"

    // WorkManager's floor for periodic work is 15 minutes; Doze can stretch it.
    private const val INTERVAL_MINUTES = 15L

    fun refresh(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        val shouldRun = uid.isNotBlank() &&
            NotificationPreferences.isEnabled(context, uid) &&
            NotificationPreferences.followedUsers(context, uid).isNotEmpty()

        if (shouldRun) {
            schedule(context)
            // The periodic poll can be up to an interval away. Run one check
            // straight away so turning the setting on, or following someone
            // new, does not appear to do nothing.
            runNow(context)
        } else {
            cancel(context)
        }
    }

    /** Enqueues a single immediate check, independent of the periodic poll. */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<ExpenseNotificationWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME_NOW,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Marks everything up to now as already seen, so switching notifications on
     * does not fire an alert for the whole backlog.
     */
    fun markCaughtUp(context: Context) {
        NotificationPreferences.setLastSeenCreatedAt(
            context,
            System.currentTimeMillis()
        )
    }

    private fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ExpenseNotificationWorker>(
            INTERVAL_MINUTES, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
