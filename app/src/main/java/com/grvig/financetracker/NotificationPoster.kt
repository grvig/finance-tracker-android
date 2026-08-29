package com.grvig.financetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.grvig.financetracker.data.Expense

/** Builds and posts the "someone added an expense" notifications. */
object NotificationPoster {

    const val CHANNEL_ID = "household_expenses"
    const val EXTRA_OPEN_EXPENSE_LIST = "open_expense_list"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Household expenses",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "When a household member you follow adds an expense"
        }

        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    /** Posts one notification per expense so each can be tapped through. */
    fun post(
        context: Context,
        expenses: List<Expense>,
        memberEmails: Map<String, String>
    ) {
        if (expenses.isEmpty()) return

        ensureChannel(context)

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(EXTRA_OPEN_EXPENSE_LIST, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        expenses.forEach { expense ->

            val who = memberEmails[expense.addedBy]

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(
                    NotificationText.title(who, formatMoneyFull(expense.amount))
                )
                .setContentText(NotificationText.body(expense))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            // Stable per expense so the same row cannot notify twice.
            try {
                manager.notify(expense.id.hashCode(), notification)
            } catch (e: SecurityException) {
                // Permission revoked between the check above and here.
            }
        }
    }
}
