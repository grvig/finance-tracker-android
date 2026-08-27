package com.grvig.financetracker

import com.grvig.financetracker.data.Expense

data class NotificationSelection(
    val toNotify: List<Expense>,
    val newMarker: Long
)

/**
 * Decides which expenses deserve a notification for the current user, and how
 * far to advance the high-water marker. Pure so it can be unit tested without
 * Firestore, WorkManager or Android.
 */
object NotificationSelector {

    fun select(
        expenses: List<Expense>,
        followedUsers: Set<String>,
        currentUserId: String,
        lastSeenCreatedAt: Long
    ): NotificationSelection {

        val toNotify = expenses.filter { expense ->
            expense.createdAt > lastSeenCreatedAt &&
                expense.addedBy != currentUserId &&
                expense.addedBy in followedUsers
        }.sortedBy { it.createdAt }

        // Advance past everything seen this poll, not just the notified rows, so
        // an unfollowed member's expense is never reconsidered later.
        val highest = expenses.maxOfOrNull { it.createdAt } ?: lastSeenCreatedAt
        val newMarker = maxOf(lastSeenCreatedAt, highest)

        return NotificationSelection(toNotify, newMarker)
    }
}
