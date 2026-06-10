package com.grvig.financetracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun ExpenseListScreen(
    expenseViewModel: ExpenseViewModel,
    onAddExpenseClick: () -> Unit,
    onDashboardClick: () -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    val scope = rememberCoroutineScope()

    fun refreshExpenses() {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
        }
    }

    LaunchedEffect(Unit) {
        refreshExpenses()
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

        LazyColumn {

            items(expenses) { expense ->

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

                                expenseViewModel.deleteExpense(
                                    expense
                                )

                                scope.launch {
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