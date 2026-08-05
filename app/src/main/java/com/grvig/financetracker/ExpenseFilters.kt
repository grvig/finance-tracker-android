package com.grvig.financetracker

import com.grvig.financetracker.data.Expense
import java.time.LocalDate

const val FILTER_ALL = "All"

val PAYMENT_METHODS = listOf(
    "Cash",
    "UPI",
    "Debit Card",
    "Credit Card"
)

enum class DateRange(val label: String) {
    ALL_TIME("All time"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    LAST_30_DAYS("Last 30 days"),
    CUSTOM("Custom range")
}

enum class ExpenseSort(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    HIGHEST("Highest amount"),
    LOWEST("Lowest amount")
}

data class ExpenseFilters(
    val search: String = "",
    val category: String = FILTER_ALL,
    val paymentMethod: String = FILTER_ALL,
    val dateRange: DateRange = DateRange.ALL_TIME,
    val customStart: LocalDate? = null,
    val customEnd: LocalDate? = null,
    val sort: ExpenseSort = ExpenseSort.NEWEST
)

fun ExpenseFilters.activeCount(): Int {

    var count = 0

    if (search.isNotBlank()) count++
    if (category != FILTER_ALL) count++
    if (paymentMethod != FILTER_ALL) count++
    if (dateRange != DateRange.ALL_TIME) count++

    return count
}

/** Chip label for the date filter, showing real dates for a custom range. */
fun ExpenseFilters.dateLabel(): String {

    val start = customStart
    val end = customEnd

    return when {
        dateRange == DateRange.ALL_TIME -> "Any date"

        dateRange == DateRange.CUSTOM && start != null && end != null ->
            "${formatExpenseDate(start.toString())} - ${formatExpenseDate(end.toString())}"

        else -> dateRange.label
    }
}

fun ExpenseFilters.isDefault(): Boolean {
    return activeCount() == 0 && sort == ExpenseSort.NEWEST
}

/**
 * Inclusive start and end dates for the selected range, or null when the
 * range covers everything.
 */
fun ExpenseFilters.dateBounds(today: LocalDate): Pair<LocalDate, LocalDate>? {

    return when (dateRange) {

        DateRange.ALL_TIME -> null

        DateRange.THIS_WEEK -> {
            val start = today.minusDays(
                (today.dayOfWeek.value % 7).toLong()
            )
            start to today
        }

        DateRange.THIS_MONTH -> today.withDayOfMonth(1) to today

        DateRange.LAST_30_DAYS -> today.minusDays(29) to today

        DateRange.CUSTOM -> {
            val start = customStart
            val end = customEnd
            if (start == null || end == null) {
                null
            } else if (start.isAfter(end)) {
                end to start
            } else {
                start to end
            }
        }
    }
}

private fun Expense.matchesSearch(query: String): Boolean {
    return description.contains(query, ignoreCase = true) ||
        notes.contains(query, ignoreCase = true) ||
        category.contains(query, ignoreCase = true)
}

private fun Expense.isWithin(bounds: Pair<LocalDate, LocalDate>?): Boolean {

    if (bounds == null) {
        return true
    }

    val parsed = try {
        LocalDate.parse(date)
    } catch (e: Exception) {
        return false
    }

    return !parsed.isBefore(bounds.first) && !parsed.isAfter(bounds.second)
}

fun List<Expense>.applyFilters(
    filters: ExpenseFilters,
    today: LocalDate = LocalDate.now()
): List<Expense> {

    val bounds = filters.dateBounds(today)

    val matching = filter { expense ->

        (filters.search.isBlank() || expense.matchesSearch(filters.search)) &&
            (filters.category == FILTER_ALL || expense.category == filters.category) &&
            (filters.paymentMethod == FILTER_ALL || expense.paymentMethod == filters.paymentMethod) &&
            expense.isWithin(bounds)
    }

    return when (filters.sort) {
        ExpenseSort.OLDEST -> matching.sortedBy { it.date + it.time }
        ExpenseSort.HIGHEST -> matching.sortedByDescending { it.amount }
        ExpenseSort.LOWEST -> matching.sortedBy { it.amount }
        ExpenseSort.NEWEST -> matching.sortedByDescending { it.date + it.time }
    }
}
