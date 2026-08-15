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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    expenseViewModel: ExpenseViewModel,
    householdViewModel: HouseholdViewModel,
    onAddExpenseClick: () -> Unit,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onEditExpenseClick: (Expense) -> Unit,
    initialCategory: String = FILTER_ALL
) {

    val expenses by expenseViewModel.expenses.collectAsState()

    var filters by remember {
        mutableStateOf(ExpenseFilters(category = initialCategory))
    }

    var showRangePicker by remember {
        mutableStateOf(false)
    }

    var householdCategories by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var expenseToShare by remember {
        mutableStateOf<Expense?>(null)
    }

    var showSummaryShare by remember {
        mutableStateOf(false)
    }

    var expenseToDelete by remember {
        mutableStateOf<Expense?>(null)
    }

    var memberEmails by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        memberEmails = householdViewModel.getMemberEmails(
            SessionManager.currentHouseholdId
        )
        householdCategories = householdViewModel.getCategories(
            SessionManager.currentHouseholdId
        )
    }

    val visibleExpenses = expenses.applyFilters(filters)

    val visibleTotal = visibleExpenses.sumOf { it.amount }

    AppScaffold(
        title = "Expense List",
        onBack = onBack,
        onOpenDrawer = onOpenDrawer,
        actions = {

            IconButton(
                enabled = visibleExpenses.isNotEmpty(),
                onClick = { showSummaryShare = true }
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share these expenses"
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

        ExpenseFilterBar(
            filters = filters,
            categories = householdCategories,
            paymentMethods = PAYMENT_METHODS,
            onFiltersChange = { filters = it },
            onCustomRangeClick = { showRangePicker = true }
        )

        FilterSummary(
            count = visibleExpenses.size,
            total = visibleTotal
        )

        if (visibleExpenses.isEmpty()) {

            Text(
                text = "No expenses match these filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (showRangePicker) {

            DateRangePickerDialog(
                initialStart = filters.customStart,
                initialEnd = filters.customEnd,
                onDismiss = { showRangePicker = false },
                onConfirm = { start, end ->
                    filters = filters.copy(
                        dateRange = DateRange.CUSTOM,
                        customStart = start,
                        customEnd = end
                    )
                    showRangePicker = false
                }
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

            items(visibleExpenses) { expense ->

                val addedByLabel = when {
                    expense.addedBy.isBlank() -> ""
                    expense.addedBy == currentUserId -> "Added by You"
                    else -> "Added by ${memberEmails[expense.addedBy] ?: "a member"}"
                }

                ExpenseRow(
                    expense = expense,
                    addedByLabel = addedByLabel,
                    onEditClick = { onEditExpenseClick(expense) },
                    onDeleteClick = { expenseToDelete = expense },
                    onShareClick = { expenseToShare = expense }
                )
            }
        }

        expenseToShare?.let { expense ->

            val sharedBy = when {
                expense.addedBy.isBlank() -> ""
                expense.addedBy == currentUserId -> "You"
                else -> memberEmails[expense.addedBy] ?: "A household member"
            }

            SharePreviewDialog(
                fileName = "expense.png",
                subject = "Expense: ${formatMoneyFull(expense.amount)}",
                onDismiss = { expenseToShare = null },
                onError = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            ) {
                ExpenseShareCard(
                    expense = expense,
                    addedByLabel = sharedBy
                )
            }
        }

        if (showSummaryShare) {

            SharePreviewDialog(
                fileName = "expenses.png",
                subject = "Expenses: ${formatMoneyFull(visibleTotal)}",
                onDismiss = { showSummaryShare = false },
                onError = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            ) {
                ExpenseSummaryShareCard(
                    title = "Household expenses",
                    subtitle = filters.describe(),
                    expenses = visibleExpenses,
                    total = visibleTotal,
                    addedByLabel = { expense ->
                        when {
                            expense.addedBy.isBlank() -> ""
                            expense.addedBy == currentUserId -> "You"
                            else -> memberEmails[expense.addedBy] ?: "a member"
                        }
                    }
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

        SnackbarHost(hostState = snackbarHostState)
    }
    }
}