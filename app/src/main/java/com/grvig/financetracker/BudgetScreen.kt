package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Budget
import com.grvig.financetracker.viewmodel.BudgetViewModel
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import kotlinx.coroutines.launch

@Composable
fun BudgetScreen(
    budgetViewModel: BudgetViewModel
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
    var editingBudget by remember {
        mutableStateOf<Budget?>(null)
    }

    val scope = rememberCoroutineScope()

    fun refreshBudgets() {
        scope.launch {
            budgets = budgetViewModel.getAllBudgets()
        }
    }

    LaunchedEffect(Unit) {
        refreshBudgets()
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
                    warningValue != null
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

                    } else {

                        budgetViewModel.insertBudget(
                            budget
                        )
                    }
                    scope.launch {
                        refreshBudgets()
                    }
                    Log.d(
                        "FinanceTracker",
                        budget.toString()
                    )

                    category = ""
                    monthlyLimit = ""
                    warningPercent = ""
                    editingBudget = null
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

        Text(
            text = "Saved Budgets",
            style = MaterialTheme.typography.headlineSmall
        )

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

                        Text(
                            text = budget.category
                        )

                        Text(
                            text = "₹${budget.monthlyLimit}"
                        )

                        Text(
                            text = "${budget.warningPercent}% Warning"
                        )
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

                                budgetViewModel.deleteBudget(
                                    budget
                                )

                                scope.launch {
                                    refreshBudgets()
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