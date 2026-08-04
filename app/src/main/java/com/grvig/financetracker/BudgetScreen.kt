package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Budget
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.BudgetViewModel
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    budgetViewModel: BudgetViewModel,
    expenseViewModel: ExpenseViewModel,
    householdViewModel: HouseholdViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {

    var category by remember {
        mutableStateOf("")
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var categories by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var monthlyLimit by remember {
        mutableStateOf("")
    }

    var warningPercent by remember {
        mutableStateOf("")
    }

    var budgets by remember {
        mutableStateOf<List<Budget>>(emptyList())
    }
    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }
    var editingBudget by remember {
        mutableStateOf<Budget?>(null)
    }

    var budgetToDelete by remember {
        mutableStateOf<Budget?>(null)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    fun refreshBudgets() {
        scope.launch {
            budgets = budgetViewModel.getAllBudgets()
        }
    }

    fun refreshExpenses() {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
        }
    }

    LaunchedEffect(Unit) {
        refreshBudgets()
        refreshExpenses()
        categories = householdViewModel.getCategories(
            SessionManager.currentHouseholdId
        )
        if (category.isBlank()) {
            category = categories.firstOrNull() ?: ""
        }
    }

    val currentMonth =
        LocalDate.now().toString().substring(0, 7)

    val monthExpenses = expenses.filter {
        it.date.startsWith(currentMonth)
    }

    fun spentForCategory(category: String): Double {
        return monthExpenses
            .filter {
                it.category == category
            }
            .sumOf {
                it.amount
            }
    }

    AppScaffold(
        title = "Budget Tracking",
        onBack = onBack,
        onOpenDrawer = onOpenDrawer
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = {
                categoryExpanded = it
            }
        ) {

            OutlinedTextField(
                value = category,
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
                            category = item
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = monthlyLimit,
            onValueChange = {
                monthlyLimit = it
            },
            label = {
                Text("Monthly Limit")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = warningPercent,
            onValueChange = {
                warningPercent = it
            },
            label = {
                Text("Warning Percent")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                val limitValue =
                    monthlyLimit.toDoubleOrNull()

                val warningValue =
                    warningPercent.toIntOrNull()

                if (
                    limitValue != null &&
                    limitValue > 0 &&
                    warningValue != null &&
                    warningValue in 0..100 &&
                    category.isNotBlank()
                ) {

                    val budget = if (
                        editingBudget != null
                    ) {
                        editingBudget!!.copy(
                            category = category,
                            monthlyLimit = limitValue,
                            warningPercent = warningValue
                        )
                    } else {
                        Budget(
                            category = category,
                            monthlyLimit = limitValue,
                            warningPercent = warningValue
                        )
                    }

                    if (editingBudget != null) {

                        budgetViewModel.updateBudget(
                            budget
                        )
                        scope.launch {
                            kotlinx.coroutines.delay(200)
                            refreshBudgets()
                        }

                    } else {

                        budgetViewModel.insertBudget(
                            budget
                        )
                        scope.launch {
                            kotlinx.coroutines.delay(200)
                            refreshBudgets()
                        }
                    }
                    Log.d(
                        "FinanceTracker",
                        budget.toString()
                    )

                    category = categories.firstOrNull() ?: ""
                    monthlyLimit = ""
                    warningPercent = ""
                    editingBudget = null
                } else {

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Please enter a category, monthly limit and warning percent"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (editingBudget != null)
                    "Update Budget"
                else
                    "Save Budget"
            )
        }

        Button(
            onClick = {
                refreshBudgets()
                refreshExpenses()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh")
        }

        Text(
            text = "Saved Budgets",
            style = MaterialTheme.typography.headlineSmall
        )

        if (budgets.isEmpty()) {

            Text(
                text = "No budgets created yet"
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            items(budgets) { budget ->

                val spent = spentForCategory(budget.category)

                val spentPercent = if (budget.monthlyLimit > 0) {
                    ((spent / budget.monthlyLimit) * 100).toInt()
                } else {
                    0
                }

                val overWarning = spentPercent >= budget.warningPercent

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(
                            start = 14.dp,
                            end = 6.dp,
                            top = 12.dp,
                            bottom = 12.dp
                        )
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(modifier = Modifier.weight(1f)) {

                                Text(
                                    text = budget.category,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "${formatMoneyFull(spent)} of ${formatMoneyFull(budget.monthlyLimit)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Text(
                                text = "$spentPercent%",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (overWarning) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )

                            IconButton(
                                onClick = {

                                    category = budget.category

                                    monthlyLimit =
                                        budget.monthlyLimit.toString()

                                    warningPercent =
                                        budget.warningPercent.toString()
                                    editingBudget = budget
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit budget",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    budgetToDelete = budget
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete budget",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        BudgetProgressBar(
                            percent = spentPercent,
                            modifier = Modifier.padding(top = 10.dp, end = 8.dp)
                        )

                        if (overWarning) {
                            Text(
                                text = "Over your ${budget.warningPercent}% warning level",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        budgetToDelete?.let { budget ->

            AlertDialog(
                onDismissRequest = {
                    budgetToDelete = null
                },
                title = {
                    Text("Delete Budget")
                },
                text = {
                    Text("Are you sure you want to delete this budget?")
                },
                confirmButton = {
                    Button(
                        onClick = {

                            budgetViewModel.deleteBudget(
                                budget
                            )

                            scope.launch {
                                refreshBudgets()
                            }

                            budgetToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            budgetToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState
        )
    }
    }
}