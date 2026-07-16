package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BudgetScreen(
    budgetViewModel: BudgetViewModel,
    expenseViewModel: ExpenseViewModel
) {

    var category by remember {
        mutableStateOf("")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Budget Tracking",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = category,
            onValueChange = {
                category = it
            },
            label = {
                Text("Category")
            },
            modifier = Modifier.fillMaxWidth()
        )

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

                    category = ""
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

        LazyColumn {

            items(budgets) { budget ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        val spent = spentForCategory(budget.category)

                        val spentPercent = if (budget.monthlyLimit > 0) {
                            ((spent / budget.monthlyLimit) * 100).toInt()
                        } else {
                            0
                        }

                        Text(
                            text = budget.category
                        )

                        Text(
                            text = "₹${budget.monthlyLimit}"
                        )

                        Text(
                            text = "Spent: ₹$spent"
                        )

                        if (spentPercent >= budget.warningPercent) {
                            Text(
                                text = "⚠ $spentPercent% of budget used"
                            )
                        }
                        Button(
                            onClick = {

                                category = budget.category

                                monthlyLimit =
                                    budget.monthlyLimit.toString()

                                warningPercent =
                                    budget.warningPercent.toString()
                                editingBudget = budget
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Load Into Form")
                        }
                        Button(
                            onClick = {
                                budgetToDelete = budget
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