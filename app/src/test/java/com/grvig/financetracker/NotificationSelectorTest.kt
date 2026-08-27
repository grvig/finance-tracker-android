package com.grvig.financetracker

import com.grvig.financetracker.data.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationSelectorTest {

    private fun expense(
        id: String,
        addedBy: String,
        createdAt: Long,
        amount: Double = 100.0
    ) = Expense(id = id, addedBy = addedBy, createdAt = createdAt, amount = amount)

    private val me = "me"
    private val wife = "wife"
    private val kid = "kid"

    @Test
    fun `notifies for a followed user's new expense`() {
        val expenses = listOf(expense("1", wife, 100))
        val result = NotificationSelector.select(expenses, setOf(wife), me, 50)
        assertEquals(1, result.toNotify.size)
        assertEquals("1", result.toNotify.first().id)
    }

    @Test
    fun `ignores your own expenses even if you follow yourself`() {
        val expenses = listOf(expense("1", me, 100))
        val result = NotificationSelector.select(expenses, setOf(me, wife), me, 50)
        assertTrue(result.toNotify.isEmpty())
    }

    @Test
    fun `ignores users you do not follow`() {
        val expenses = listOf(expense("1", kid, 100))
        val result = NotificationSelector.select(expenses, setOf(wife), me, 50)
        assertTrue(result.toNotify.isEmpty())
    }

    @Test
    fun `ignores expenses at or below the marker`() {
        val expenses = listOf(
            expense("old", wife, 50),
            expense("edge", wife, 100),
            expense("new", wife, 150)
        )
        val result = NotificationSelector.select(expenses, setOf(wife), me, 100)
        assertEquals(listOf("new"), result.toNotify.map { it.id })
    }

    @Test
    fun `advances marker past every expense seen, not only notified ones`() {
        val expenses = listOf(
            expense("followed", wife, 120),
            expense("unfollowed", kid, 300)
        )
        val result = NotificationSelector.select(expenses, setOf(wife), me, 100)
        assertEquals(listOf("followed"), result.toNotify.map { it.id })
        assertEquals(300, result.newMarker)
    }

    @Test
    fun `marker never moves backwards`() {
        val expenses = listOf(expense("1", wife, 40))
        val result = NotificationSelector.select(expenses, setOf(wife), me, 500)
        assertTrue(result.toNotify.isEmpty())
        assertEquals(500, result.newMarker)
    }

    @Test
    fun `results are ordered oldest first`() {
        val expenses = listOf(
            expense("c", wife, 300),
            expense("a", wife, 100),
            expense("b", wife, 200)
        )
        val result = NotificationSelector.select(expenses, setOf(wife), me, 50)
        assertEquals(listOf("a", "b", "c"), result.toNotify.map { it.id })
    }

    @Test
    fun `empty expense list keeps the existing marker`() {
        val result = NotificationSelector.select(emptyList(), setOf(wife), me, 250)
        assertTrue(result.toNotify.isEmpty())
        assertEquals(250, result.newMarker)
    }
}
