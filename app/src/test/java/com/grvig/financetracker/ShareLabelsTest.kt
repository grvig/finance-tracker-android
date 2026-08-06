package com.grvig.financetracker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ShareLabelsTest {

    @Test
    fun `month key becomes a readable label`() {
        assertEquals("July 2026", formatMonthLabel("2026-07"))
        assertEquals("January 2025", formatMonthLabel("2025-01"))
    }

    @Test
    fun `unparseable month falls back to the raw value`() {
        assertEquals("not-a-month", formatMonthLabel("not-a-month"))
    }

    @Test
    fun `default filters describe themselves as everything`() {
        assertEquals("All expenses", ExpenseFilters().describe())
    }

    @Test
    fun `sort alone is not treated as a filter`() {
        assertEquals(
            "All expenses",
            ExpenseFilters(sort = ExpenseSort.HIGHEST).describe()
        )
    }

    @Test
    fun `active filters are joined in order`() {

        val filters = ExpenseFilters(
            dateRange = DateRange.THIS_MONTH,
            category = "Food",
            paymentMethod = "Cash",
            search = "lunch"
        )

        assertEquals(
            "This month · Food · Cash · \"lunch\"",
            filters.describe()
        )
    }

    @Test
    fun `custom range describes the chosen dates`() {

        val filters = ExpenseFilters(
            dateRange = DateRange.CUSTOM,
            customStart = LocalDate.of(2026, 8, 1),
            customEnd = LocalDate.of(2026, 8, 5)
        )

        assertEquals("1 Aug - 5 Aug", filters.describe())
    }

    @Test
    fun `custom range without dates falls back to the generic label`() {

        val filters = ExpenseFilters(dateRange = DateRange.CUSTOM)

        assertEquals("Custom range", filters.dateLabel())
    }
}
