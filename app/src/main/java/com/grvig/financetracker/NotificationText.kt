package com.grvig.financetracker

import com.grvig.financetracker.data.Expense

/**
 * The wording of an expense notification. Pure so the phrasing can be unit
 * tested; the poster only supplies the money formatting and the icon.
 */
object NotificationText {

    private const val UNKNOWN_MEMBER = "A household member"

    /** "priya" out of "priya@example.com", falling back to something neutral. */
    fun displayName(email: String?): String {
        val name = email?.substringBefore("@")?.trim().orEmpty()
        return if (name.isBlank()) UNKNOWN_MEMBER else name
    }

    fun title(email: String?, formattedAmount: String): String {
        return "${displayName(email)} added $formattedAmount"
    }

    /**
     * The category alone when there is no description. Repeating it either side
     * of the separator read as "Food · Food" on the device.
     */
    /** Title of the bundle shown when several expenses land in one poll. */
    fun summaryTitle(count: Int): String {
        return if (count == 1) "1 new expense" else "$count new expenses"
    }

    fun body(expense: Expense): String {
        val description = expense.description.trim()
        val category = expense.category.trim()

        return when {
            description.isBlank() -> category
            description.equals(category, ignoreCase = true) -> category
            category.isBlank() -> description
            else -> "$description · $category"
        }
    }
}
