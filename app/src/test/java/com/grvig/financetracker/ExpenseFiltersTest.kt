package com.grvig.financetracker

import com.grvig.financetracker.data.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExpenseFiltersTest {

    private val today = LocalDate.of(2026, 8, 5)

    private fun expense(
        amount: Double,
        category: String = "Food",
        paymentMethod: String = "UPI",
        description: String = "",
        notes: String = "",
        date: String = "2026-08-05",
        time: String = "10:00:00"
    ) = Expense(
        amount = amount,
        category = category,
        paymentMethod = paymentMethod,
        description = description,
        notes = notes,
        date = date,
        time = time
    )

    private val sample = listOf(
        expense(100.0, category = "Food", date = "2026-08-05", description = "Lunch"),
        expense(250.0, category = "Travel", paymentMethod = "Cash", date = "2026-08-01"),
        expense(50.0, category = "Food", date = "2026-07-20", notes = "old snack"),
        expense(900.0, category = "Bills", date = "2026-06-15")
    )

    @Test
    fun `default filters keep everything newest first`() {

        val result = sample.applyFilters(ExpenseFilters(), today)

        assertEquals(4, result.size)
        assertEquals("2026-08-05", result.first().date)
        assertEquals("2026-06-15", result.last().date)
    }

    @Test
    fun `category filter narrows to one category`() {

        val result = sample.applyFilters(
            ExpenseFilters(category = "Food"),
            today
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "Food" })
    }

    @Test
    fun `payment method filter narrows to one method`() {

        val result = sample.applyFilters(
            ExpenseFilters(paymentMethod = "Cash"),
            today
        )

        assertEquals(1, result.size)
        assertEquals(250.0, result.first().amount, 0.001)
    }

    @Test
    fun `search matches description notes and category`() {

        assertEquals(
            1,
            sample.applyFilters(ExpenseFilters(search = "lunch"), today).size
        )

        assertEquals(
            1,
            sample.applyFilters(ExpenseFilters(search = "snack"), today).size
        )

        assertEquals(
            2,
            sample.applyFilters(ExpenseFilters(search = "food"), today).size
        )
    }

    @Test
    fun `this month range excludes earlier months`() {

        val result = sample.applyFilters(
            ExpenseFilters(dateRange = DateRange.THIS_MONTH),
            today
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.date.startsWith("2026-08") })
    }

    @Test
    fun `last thirty days includes the boundary day`() {

        val boundary = expense(10.0, date = today.minusDays(29).toString())
        val outside = expense(20.0, date = today.minusDays(30).toString())

        val result = listOf(boundary, outside).applyFilters(
            ExpenseFilters(dateRange = DateRange.LAST_30_DAYS),
            today
        )

        assertEquals(1, result.size)
        assertEquals(10.0, result.first().amount, 0.001)
    }

    @Test
    fun `custom range is inclusive on both ends`() {

        val result = sample.applyFilters(
            ExpenseFilters(
                dateRange = DateRange.CUSTOM,
                customStart = LocalDate.of(2026, 7, 20),
                customEnd = LocalDate.of(2026, 8, 1)
            ),
            today
        )

        assertEquals(2, result.size)
    }

    @Test
    fun `custom range swaps reversed dates`() {

        val filters = ExpenseFilters(
            dateRange = DateRange.CUSTOM,
            customStart = LocalDate.of(2026, 8, 1),
            customEnd = LocalDate.of(2026, 7, 20)
        )

        assertEquals(
            LocalDate.of(2026, 7, 20) to LocalDate.of(2026, 8, 1),
            filters.dateBounds(today)
        )
    }

    @Test
    fun `amount sorts order by value`() {

        val highest = sample.applyFilters(
            ExpenseFilters(sort = ExpenseSort.HIGHEST),
            today
        )

        val lowest = sample.applyFilters(
            ExpenseFilters(sort = ExpenseSort.LOWEST),
            today
        )

        assertEquals(900.0, highest.first().amount, 0.001)
        assertEquals(50.0, lowest.first().amount, 0.001)
    }

    @Test
    fun `filters combine instead of overriding each other`() {

        val result = sample.applyFilters(
            ExpenseFilters(
                category = "Food",
                dateRange = DateRange.THIS_MONTH
            ),
            today
        )

        assertEquals(1, result.size)
        assertEquals(100.0, result.first().amount, 0.001)
    }

    @Test
    fun `active count tracks only the narrowing filters`() {

        assertEquals(0, ExpenseFilters().activeCount())
        assertTrue(ExpenseFilters().isDefault())

        assertEquals(
            0,
            ExpenseFilters(sort = ExpenseSort.LOWEST).activeCount()
        )

        assertEquals(
            2,
            ExpenseFilters(
                category = "Food",
                dateRange = DateRange.THIS_WEEK
            ).activeCount()
        )
    }

    @Test
    fun `unparseable dates drop out of a bounded range`() {

        val broken = expense(10.0, date = "not-a-date")

        val result = listOf(broken).applyFilters(
            ExpenseFilters(dateRange = DateRange.THIS_MONTH),
            today
        )

        assertTrue(result.isEmpty())
    }
}
