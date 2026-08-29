package com.grvig.financetracker

import com.grvig.financetracker.data.Expense
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationTextTest {

    private fun expense(description: String, category: String) =
        Expense(description = description, category = category)

    @Test
    fun `title uses the part of the email before the at sign`() {
        assertEquals(
            "priya added ₹500",
            NotificationText.title("priya@example.com", "₹500")
        )
    }

    @Test
    fun `title falls back when the email is unknown`() {
        assertEquals(
            "A household member added ₹500",
            NotificationText.title(null, "₹500")
        )
    }

    @Test
    fun `body keeps both parts when they differ`() {
        assertEquals(
            "Lunch · Food",
            NotificationText.body(expense("Lunch", "Food"))
        )
    }

    @Test
    fun `body shows the category once when there is no description`() {
        assertEquals("Food", NotificationText.body(expense("", "Food")))
    }

    @Test
    fun `body shows the category once when the description repeats it`() {
        assertEquals("Food", NotificationText.body(expense("food", "Food")))
    }

    @Test
    fun `body falls back to the description when there is no category`() {
        assertEquals("Lunch", NotificationText.body(expense("Lunch", "")))
    }
}
