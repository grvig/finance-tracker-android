package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Budget
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.BudgetViewModel
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    expenseViewModel: ExpenseViewModel,
    budgetViewModel: BudgetViewModel,
    onDashboardClick: () -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    var budgets by remember {
        mutableStateOf<List<Budget>>(emptyList())
    }

    val months = (0..11).map { monthsAgo ->
        LocalDate.now()
            .minusMonths(monthsAgo.toLong())
            .toString()
            .substring(0, 7)
    }

    var selectedMonth by remember {
        mutableStateOf(months.first())
    }

    var monthExpanded by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
            budgets = budgetViewModel.getAllBudgets()
        }
    }

    val monthExpenses = expenses.filter {
        it.date.startsWith(selectedMonth)
    }

    val totalSpent = monthExpenses.sumOf {
        it.amount
    }

    val totalCount = monthExpenses.size

    val averageExpense = if (totalCount > 0) {
        totalSpent / totalCount
    } else {
        0.0
    }

    val totalBudget = budgets.sumOf {
        it.monthlyLimit
    }

    val budgetUsagePercent = if (totalBudget > 0) {
        ((totalSpent / totalBudget) * 100).toInt()
    } else {
        0
    }

    val categoryBreakdown = monthExpenses
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onDashboardClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Dashboard")
        }

        ExposedDropdownMenuBox(
            expanded = monthExpanded,
            onExpandedChange = {
                monthExpanded = it
            }
        ) {

            OutlinedTextField(
                value = selectedMonth,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Month")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = monthExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = {
                    monthExpanded = false
                }
            ) {

                months.forEach { month ->

                    DropdownMenuItem(
                        text = {
                            Text(month)
                        },
                        onClick = {
                            selectedMonth = month
                            monthExpanded = false
                        }
                    )
                }
            }
        }

        if (monthExpenses.isEmpty()) {

            Text(
                text = "No expenses recorded for this month"
            )
        } else {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text("Total Spent: ₹$totalSpent")
                    Text("Total Expenses: $totalCount")
                    Text("Average Expense: ₹$averageExpense")

                    if (totalBudget > 0) {
                        Text("Budget Usage: $budgetUsagePercent%")
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
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium
                    )

                    categoryBreakdown.forEach { (category, amount) ->

                        val percent = if (totalSpent > 0) {
                            ((amount / totalSpent) * 100).toInt()
                        } else {
                            0
                        }

                        Text("$category: ₹$amount ($percent%)")
                    }
                }
            }
        }
    }
}
