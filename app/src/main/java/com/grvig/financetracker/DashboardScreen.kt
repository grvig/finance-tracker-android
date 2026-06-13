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

@Composable
fun DashboardScreen(
    expenseViewModel: ExpenseViewModel,
    onAddExpenseClick: () -> Unit,
    onViewExpensesClick: () -> Unit,
    onBudgetClick: () -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
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
    }
}