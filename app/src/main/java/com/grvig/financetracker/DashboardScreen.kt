package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
fun DashboardScreen(
expenseViewModel: ExpenseViewModel,
budgetViewModel: BudgetViewModel,
recurringExpenseViewModel: RecurringExpenseViewModel,
onAddExpenseClick: () -> Unit,
onViewExpensesClick: () -> Unit,
onBudgetClick: () -> Unit,
onRecurringExpensesClick: () -> Unit
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
    }
}