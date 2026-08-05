package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch

@Composable
fun MyExpensesScreen(
    expenseViewModel: ExpenseViewModel,
    householdViewModel: HouseholdViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    var filters by remember {
        mutableStateOf(ExpenseFilters())
    }

    var showRangePicker by remember {
        mutableStateOf(false)
    }

    var householdCategories by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
            householdCategories = householdViewModel.getCategories(
                SessionManager.currentHouseholdId
            )
        }
    }

    val mine = expenses.filter {
        it.addedBy == currentUserId
    }

    val myExpenses = mine.applyFilters(filters)

    val myTotal = myExpenses.sumOf {
        it.amount
    }

    AppScaffold(
        title = "My Expenses",
        onBack = onBack,
        onOpenDrawer = onOpenDrawer
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Your Spending",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatMoneyFull(myTotal),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${myExpenses.size} expense${if (myExpenses.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            ExpenseFilterBar(
                filters = filters,
                categories = householdCategories,
                paymentMethods = PAYMENT_METHODS,
                onFiltersChange = { filters = it },
                onCustomRangeClick = { showRangePicker = true }
            )

            if (myExpenses.isEmpty()) {
                Text(
                    text = if (mine.isEmpty()) {
                        "You haven't added any expenses yet"
                    } else {
                        "No expenses match these filters"
                    },
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
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(myExpenses) { expense ->

                    ExpenseRow(
                        expense = expense,
                        addedByLabel = ""
                    )
                }
            }
        }
    }
}
