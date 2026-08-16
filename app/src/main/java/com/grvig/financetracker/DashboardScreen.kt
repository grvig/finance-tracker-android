package com.grvig.financetracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.grvig.financetracker.viewmodel.HouseholdViewModel

@Composable
fun DashboardScreen(
expenseViewModel: ExpenseViewModel,
budgetViewModel: BudgetViewModel,
recurringExpenseViewModel: RecurringExpenseViewModel,
householdViewModel: HouseholdViewModel,
onAddExpenseClick: () -> Unit,
onViewExpensesClick: () -> Unit,
onCategoryClick: (String) -> Unit,
onMyExpensesClick: () -> Unit,
onBudgetClick: () -> Unit,
onRecurringExpensesClick: () -> Unit,
onReportsClick: () -> Unit,
onHouseholdClick: () -> Unit,
onSignOutClick: () -> Unit,
onOpenDrawer: () -> Unit
) {

    val expenses by expenseViewModel.expenses.collectAsState()
    val budgets by budgetViewModel.budgets.collectAsState()
    val recurringExpenses by recurringExpenseViewModel
        .recurringExpenses
        .collectAsState()

    var memberCount by remember {
        mutableStateOf(0)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {

            recurringExpenseViewModel.generateDueExpenses(
                LocalDate.now().toString()
            )

            memberCount = householdViewModel.getHousehold(
                SessionManager.currentHouseholdId
            )?.memberIds?.size ?: 0
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

    AppScaffold(
        title = "Dashboard",
        onOpenDrawer = onOpenDrawer
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        if (memberCount > 0) {
            Text(
                text = "Shared with $memberCount member${if (memberCount == 1) "" else "s"}"
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Text(
                    text = formatMoneyFull(monthSpent),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (lastMonthSpent > 0) {
                    Text(
                        text = if (monthlyChangePercent >= 0)
                            "▲ $monthlyChangePercent% vs last month"
                        else
                            "▼ ${-monthlyChangePercent}% vs last month",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    StatTile(
                        label = "Today",
                        value = formatMoneyFull(todaySpent),
                        onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Expenses",
                        value = totalExpenses.toString(),
                        onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Avg",
                        value = formatMoneyFull(averageExpense),
                        onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Budget Remaining",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatMoneyFull(remainingBudget),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = "of ${formatMoneyFull(totalBudget)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }

                if (totalBudget > 0) {

                    BudgetProgressBar(
                        percent = budgetUsagePercent,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = "$budgetUsagePercent% used",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        text = "No budgets set yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (largestCategory != "None") {
                    Text(
                        text = "Top category: $largestCategory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }

        if (categoryTotals.isNotEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium
                    )

                    CategoryBreakdownChart(
                        categoryTotals = categoryTotals,
                        modifier = Modifier.padding(top = 8.dp),
                        onCategoryClick = onCategoryClick
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Spending Trend (6 Months)",
                    style = MaterialTheme.typography.titleMedium
                )

                if (monthlyTotals.all { it.second == 0.0 }) {

                    Text(
                        text = "No spending data yet"
                    )
                } else {

                    MonthlyTrendChart(
                        monthlyTotals = monthlyTotals
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = onAddExpenseClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Expense")
            }

            OutlinedButton(
                onClick = onViewExpensesClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("All Expenses")
            }
        }
    }
    }
}