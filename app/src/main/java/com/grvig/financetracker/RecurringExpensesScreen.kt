package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.RecurringExpense
import com.grvig.financetracker.viewmodel.RecurringExpenseViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    recurringExpenseViewModel: RecurringExpenseViewModel,
    onDashboardClick: () -> Unit
) {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var category by remember {
        mutableStateOf("Food")
    }

    var paymentMethod by remember {
        mutableStateOf("UPI")
    }

    var frequency by remember {
        mutableStateOf("Monthly")
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var paymentExpanded by remember {
        mutableStateOf(false)
    }

    var frequencyExpanded by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Food",
        "Transport",
        "Shopping",
        "Bills",
        "Health",
        "Entertainment"
    )

    val paymentMethods = listOf(
        "Cash",
        "UPI",
        "Debit Card",
        "Credit Card"
    )

    val frequencies = listOf(
        "Weekly",
        "Monthly"
    )

    var recurringExpenses by remember {
        mutableStateOf<List<RecurringExpense>>(emptyList())
    }

    val scope = rememberCoroutineScope()

    fun refreshRecurringExpenses() {
        scope.launch {
            recurringExpenses =
                recurringExpenseViewModel.getAllRecurringExpenses()
        }
    }

    LaunchedEffect(Unit) {
        refreshRecurringExpenses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Recurring Expenses",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onDashboardClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Dashboard")
        }

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            label = {
                Text("Title")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
            },
            label = {
                Text("Amount")
            },
            modifier = Modifier.fillMaxWidth()
        )

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
                            category = item
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = paymentExpanded,
            onExpandedChange = {
                paymentExpanded = it
            }
        ) {

            OutlinedTextField(
                value = paymentMethod,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Payment Method")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = paymentExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = paymentExpanded,
                onDismissRequest = {
                    paymentExpanded = false
                }
            ) {

                paymentMethods.forEach { item ->

                    DropdownMenuItem(
                        text = {
                            Text(item)
                        },
                        onClick = {
                            paymentMethod = item
                            paymentExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = frequencyExpanded,
            onExpandedChange = {
                frequencyExpanded = it
            }
        ) {

            OutlinedTextField(
                value = frequency,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Frequency")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = frequencyExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = frequencyExpanded,
                onDismissRequest = {
                    frequencyExpanded = false
                }
            ) {

                frequencies.forEach { item ->

                    DropdownMenuItem(
                        text = {
                            Text(item)
                        },
                        onClick = {
                            frequency = item
                            frequencyExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {

                val amountValue = amount.toDoubleOrNull()

                if (amountValue != null && title.isNotBlank()) {

                    val recurringExpense = RecurringExpense(
                        title = title,
                        amount = amountValue,
                        category = category,
                        paymentMethod = paymentMethod,
                        cardName = null,
                        notes = "",
                        frequency = frequency,
                        nextDueDate = LocalDate.now().toString(),
                        isActive = true
                    )

                    recurringExpenseViewModel.insertRecurringExpense(
                        recurringExpense
                    )

                    scope.launch {
                        kotlinx.coroutines.delay(200)
                        refreshRecurringExpenses()
                    }

                    title = ""
                    amount = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Recurring Expense")
        }

        Text(
            text = "Saved Recurring Expenses",
            style = MaterialTheme.typography.headlineSmall
        )

        LazyColumn {

            items(recurringExpenses) { recurringExpense ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(recurringExpense.title)
                        Text("₹${recurringExpense.amount}")
                        Text(recurringExpense.category)
                        Text(
                            "${recurringExpense.frequency}, next due ${recurringExpense.nextDueDate}"
                        )

                        Button(
                            onClick = {

                                recurringExpenseViewModel.deleteRecurringExpense(
                                    recurringExpense
                                )

                                scope.launch {
                                    refreshRecurringExpenses()
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
