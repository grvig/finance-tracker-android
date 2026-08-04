package com.grvig.financetracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.google.firebase.auth.FirebaseAuth
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    expenseViewModel: ExpenseViewModel,
    householdViewModel: HouseholdViewModel,
    onAddExpenseClick: () -> Unit,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
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

    var householdCategories by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    val categories = listOf("All") + householdCategories

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

    var memberEmails by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val scope = rememberCoroutineScope()

    fun refreshExpenses() {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
        }
    }

    LaunchedEffect(Unit) {
        refreshExpenses()
        memberEmails = householdViewModel.getMemberEmails(
            SessionManager.currentHouseholdId
        )
        householdCategories = householdViewModel.getCategories(
            SessionManager.currentHouseholdId
        )
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

    AppScaffold(
        title = "Expense List",
        onBack = onBack,
        onOpenDrawer = onOpenDrawer,
        actions = {
            IconButton(onClick = { refreshExpenses() }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpenseClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add expense"
                )
            }
        }
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

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
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
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
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
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

        if (sortedExpenses.isEmpty()) {

            Text(
                text = "No expenses found",
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(sortedExpenses) { expense ->

                val addedByLabel = when {
                    expense.addedBy.isBlank() -> ""
                    expense.addedBy == currentUserId -> "Added by You"
                    else -> "Added by ${memberEmails[expense.addedBy] ?: "a member"}"
                }

                ExpenseRow(
                    expense = expense,
                    addedByLabel = addedByLabel,
                    onEditClick = { onEditExpenseClick(expense) },
                    onDeleteClick = { expenseToDelete = expense }
                )
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
}