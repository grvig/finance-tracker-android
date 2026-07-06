package com.grvig.financetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import com.grvig.financetracker.data.Budget
import com.grvig.financetracker.viewmodel.BudgetViewModel
import com.grvig.financetracker.data.RecurringExpense
import com.grvig.financetracker.viewmodel.RecurringExpenseViewModel

private fun nextDueDateAfter(
    currentDueDate: String,
    frequency: String
): String {

    val dueDate = LocalDate.parse(currentDueDate)

    val advancedDate = if (frequency == "Weekly") {
        dueDate.plusWeeks(1)
    } else {
        dueDate.plusMonths(1)
    }

    return advancedDate.toString()
}

private val chartColors = listOf(
    Color(0xFF7aa2f7),
    Color(0xFFbb9af7),
    Color(0xFF9ece6a),
    Color(0xFFe0af68),
    Color(0xFFf7768e),
    Color(0xFF7dcfff)
)

@Composable
private fun CategoryBreakdownChart(
    categoryTotals: List<Pair<String, Double>>
) {

    val maxAmount = categoryTotals.maxOfOrNull {
        it.second
    } ?: 0.0

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        categoryTotals.forEachIndexed { index, (category, amount) ->

            val barWidthFraction = if (maxAmount > 0) {
                (amount / maxAmount).toFloat()
            } else {
                0f
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {

                Text(
                    text = category,
                    modifier = Modifier.width(90.dp)
                )

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                ) {
                    drawRect(
                        color = chartColors[index % chartColors.size],
                        size = Size(
                            size.width * barWidthFraction,
                            size.height
                        )
                    )
                }

                Text(
                    text = "₹$amount"
                )
            }
        }
    }
}

@Composable
private fun MonthlyTrendChart(
    monthlyTotals: List<Pair<String, Double>>
) {

    val maxAmount = monthlyTotals.maxOfOrNull {
        it.second
    } ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        monthlyTotals.forEach { (month, amount) ->

            val barHeightFraction = if (maxAmount > 0) {
                (amount / maxAmount).toFloat()
            } else {
                0f
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Canvas(
                    modifier = Modifier
                        .width(24.dp)
                        .height(80.dp)
                ) {
                    drawRect(
                        color = chartColors[0],
                        topLeft = androidx.compose.ui.geometry.Offset(
                            0f,
                            size.height * (1 - barHeightFraction)
                        ),
                        size = Size(
                            size.width,
                            size.height * barHeightFraction
                        )
                    )
                }

                Text(
                    text = month,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
expenseViewModel: ExpenseViewModel,
budgetViewModel: BudgetViewModel,
recurringExpenseViewModel: RecurringExpenseViewModel,
onAddExpenseClick: () -> Unit,
onViewExpensesClick: () -> Unit,
onBudgetClick: () -> Unit,
onRecurringExpensesClick: () -> Unit,
onReportsClick: () -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }
    var budgets by remember {
        mutableStateOf<List<Budget>>(emptyList())
    }
    var recurringExpenses by remember {
        mutableStateOf<List<RecurringExpense>>(emptyList())
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {

            val dueRecurringExpenses = recurringExpenseViewModel
                .getAllRecurringExpenses()
                .filter {
                    it.isActive &&
                        it.nextDueDate <= LocalDate.now().toString()
                }

            dueRecurringExpenses.forEach { recurringExpense ->

                expenseViewModel.insertExpense(
                    Expense(
                        amount = recurringExpense.amount,
                        category = recurringExpense.category,
                        paymentMethod = recurringExpense.paymentMethod,
                        cardName = recurringExpense.cardName,
                        description = recurringExpense.title,
                        notes = recurringExpense.notes,
                        date = recurringExpense.nextDueDate,
                        time = LocalTime.now().toString(),
                        isRecurring = true
                    )
                )

                recurringExpenseViewModel.updateRecurringExpense(
                    recurringExpense.copy(
                        nextDueDate = nextDueDateAfter(
                            recurringExpense.nextDueDate,
                            recurringExpense.frequency
                        )
                    )
                )
            }

            kotlinx.coroutines.delay(200)

            expenses = expenseViewModel.getAllExpenses()
            budgets = budgetViewModel.getAllBudgets()
            recurringExpenses =
                recurringExpenseViewModel.getAllRecurringExpenses()
        }
    }

    val totalExpenses = expenses.size

    val totalSpent = expenses.sumOf {
        it.amount
    }

    val today = LocalDate.now().toString()

    val todaySpent = expenses
        .filter {
            it.date == today
        }
        .sumOf {
            it.amount
        }

    val currentMonth =
        LocalDate.now().toString().substring(0, 7)

    val monthSpent = expenses
        .filter {
            it.date.startsWith(currentMonth)
        }
        .sumOf {
            it.amount
        }

    val lastMonth = LocalDate.now()
        .minusMonths(1)
        .toString()
        .substring(0, 7)

    val lastMonthSpent = expenses
        .filter {
            it.date.startsWith(lastMonth)
        }
        .sumOf {
            it.amount
        }

    val monthlyChangePercent = if (lastMonthSpent > 0) {
        (((monthSpent - lastMonthSpent) / lastMonthSpent) * 100).toInt()
    } else {
        0
    }

    val totalBudget = budgets.sumOf {
        it.monthlyLimit
    }

    val remainingBudget =
        totalBudget - monthSpent

    val largestCategory = expenses
        .groupBy {
            it.category
        }
        .mapValues { entry ->
            entry.value.sumOf {
                it.amount
            }
        }
        .maxByOrNull {
            it.value
        }
        ?.key
        ?: "None"

    val averageExpense = if (totalExpenses > 0) {
        totalSpent / totalExpenses
    } else {
        0.0
    }

    val budgetUsagePercent = if (totalBudget > 0) {
        ((monthSpent / totalBudget) * 100).toInt()
    } else {
        0
    }

    val categoryTotals = expenses
        .groupBy {
            it.category
        }
        .mapValues { entry ->
            entry.value.sumOf {
                it.amount
            }
        }
        .toList()
        .sortedByDescending {
            it.second
        }

    val monthlyTotals = (5 downTo 0).map { monthsAgo ->

        val month = LocalDate.now()
            .minusMonths(monthsAgo.toLong())
            .toString()
            .substring(0, 7)

        val spent = expenses
            .filter {
                it.date.startsWith(month)
            }
            .sumOf {
                it.amount
            }

        month.substring(5, 7) to spent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Total Expenses: $totalExpenses")
                Text("Total Spent: ₹$totalSpent")
                Text("Average Expense: ₹$averageExpense")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Today's Spending: ₹$todaySpent")
                Text("This Month's Spending: ₹$monthSpent")
                Text("Change From Last Month: $monthlyChangePercent%")
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    "Total Budget: ₹$totalBudget"
                )

                Text(
                    "Remaining Budget: ₹$remainingBudget"
                )

                Text(
                    "Largest Category: $largestCategory"
                )

                Text(
                    "Budget Usage: $budgetUsagePercent%"
                )
            }
        }

        if (categoryTotals.isNotEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium
                    )

                    CategoryBreakdownChart(
                        categoryTotals = categoryTotals
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Spending Trend (6 Months)",
                    style = MaterialTheme.typography.titleMedium
                )

                MonthlyTrendChart(
                    monthlyTotals = monthlyTotals
                )
            }
        }

        Button(
            onClick = onAddExpenseClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense")
        }

        Button(
            onClick = onViewExpensesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Expenses")
        }
        Button(
            onClick = onBudgetClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Budget Tracking")
        }

        Button(
            onClick = onRecurringExpensesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Recurring Expenses")
        }

        Button(
            onClick = onReportsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reports")
        }
    }
}