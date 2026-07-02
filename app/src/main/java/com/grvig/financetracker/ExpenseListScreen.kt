package com.grvig.financetracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExpenseListScreen(
    expenseViewModel: ExpenseViewModel,
    onAddExpenseClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onEditExpenseClick: (Expense) -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "All",
        "Food",
        "Transport",
        "Shopping",
        "Bills",
        "Health",
        "Entertainment"
    )

    val scope = rememberCoroutineScope()

    fun refreshExpenses() {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
        }
    }

    LaunchedEffect(Unit) {
        refreshExpenses()
    }

    val filteredExpenses = if (selectedCategory == "All") {
        expenses
    } else {
        expenses.filter {
            it.category == selectedCategory
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Expense List",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = onDashboardClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Back To Dashboard")
        }

        Button(
            onClick = onAddExpenseClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Add New Expense")
        }

        Button(
            onClick = {
                categoryExpanded = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Category: $selectedCategory")
        }

        DropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = {
                categoryExpanded = false
            }
        ) {

            categories.forEach { item ->

                DropdownMenuItem(
                    text = {
                        Text(item)
                    },
                    onClick = {
                        selectedCategory = item
                        categoryExpanded = false
                    }
                )
            }
        }

        LazyColumn {

            items(filteredExpenses) { expense ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 6.dp
                        )
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text("₹${expense.amount}")
                        Text(expense.category)
                        Text(expense.paymentMethod)
                        Text(expense.description)

                        Button(
                            onClick = {
                                onEditExpenseClick(expense)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Edit")
                        }

                        Button(
                            onClick = {

                                expenseViewModel.deleteExpense(
                                    expense
                                )

                                scope.launch {
                                    delay(200)
                                    refreshExpenses()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}