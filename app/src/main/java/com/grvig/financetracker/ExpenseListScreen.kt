package com.grvig.financetracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    var searchQuery by remember {
        mutableStateOf("")
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

    var selectedSort by remember {
        mutableStateOf("Newest")
    }

    var sortExpanded by remember {
        mutableStateOf(false)
    }

    val sortOptions = listOf(
        "Newest",
        "Oldest",
        "Highest Amount",
        "Lowest Amount"
    )

    var expenseToDelete by remember {
        mutableStateOf<Expense?>(null)
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

    val searchedExpenses = if (searchQuery.isBlank()) {
        expenses
    } else {
        expenses.filter {
            it.description.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredExpenses = if (selectedCategory == "All") {
        searchedExpenses
    } else {
        searchedExpenses.filter {
            it.category == selectedCategory
        }
    }

    val sortedExpenses = when (selectedSort) {
        "Oldest" -> filteredExpenses.sortedBy {
            it.date + it.time
        }
        "Highest Amount" -> filteredExpenses.sortedByDescending {
            it.amount
        }
        "Lowest Amount" -> filteredExpenses.sortedBy {
            it.amount
        }
        else -> filteredExpenses.sortedByDescending {
            it.date + it.time
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

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            label = {
                Text("Search")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = {
                categoryExpanded = it
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {

            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Category")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = categoryExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

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
        }

        ExposedDropdownMenuBox(
            expanded = sortExpanded,
            onExpandedChange = {
                sortExpanded = it
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {

            OutlinedTextField(
                value = selectedSort,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Sort")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = sortExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = sortExpanded,
                onDismissRequest = {
                    sortExpanded = false
                }
            ) {

                sortOptions.forEach { item ->

                    DropdownMenuItem(
                        text = {
                            Text(item)
                        },
                        onClick = {
                            selectedSort = item
                            sortExpanded = false
                        }
                    )
                }
            }
        }

        LazyColumn {

            items(sortedExpenses) { expense ->

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
                                expenseToDelete = expense
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

        expenseToDelete?.let { expense ->

            AlertDialog(
                onDismissRequest = {
                    expenseToDelete = null
                },
                title = {
                    Text("Delete Expense")
                },
                text = {
                    Text("Are you sure you want to delete this expense?")
                },
                confirmButton = {
                    Button(
                        onClick = {

                            expenseViewModel.deleteExpense(
                                expense
                            )

                            scope.launch {
                                delay(200)
                                refreshExpenses()
                            }

                            expenseToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            expenseToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}