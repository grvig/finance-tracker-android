package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
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
import com.google.firebase.auth.FirebaseAuth
import com.grvig.financetracker.data.RecurringExpense
import com.grvig.financetracker.viewmodel.RecurringExpenseViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    recurringExpenseViewModel: RecurringExpenseViewModel,
    householdViewModel: HouseholdViewModel,
    onDashboardClick: () -> Unit
) {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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

    var editingRecurringExpense by remember {
        mutableStateOf<RecurringExpense?>(null)
    }

    var recurringExpenseToDelete by remember {
        mutableStateOf<RecurringExpense?>(null)
    }

    var memberEmails by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val snackbarHostState = remember {
        SnackbarHostState()
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
        memberEmails = householdViewModel.getMemberEmails(
            SessionManager.currentHouseholdId
        )
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

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
            },
            label = {
                Text("Notes")
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
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
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
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
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

                if (amountValue != null && amountValue > 0 && title.isNotBlank()) {

                    val recurringExpense = if (
                        editingRecurringExpense != null
                    ) {
                        editingRecurringExpense!!.copy(
                            title = title,
                            amount = amountValue,
                            category = category,
                            paymentMethod = paymentMethod,
                            notes = notes,
                            frequency = frequency
                        )
                    } else {
                        RecurringExpense(
                            title = title,
                            amount = amountValue,
                            category = category,
                            paymentMethod = paymentMethod,
                            cardName = null,
                            notes = notes,
                            frequency = frequency,
                            nextDueDate = LocalDate.now().toString(),
                            isActive = true
                        )
                    }

                    if (editingRecurringExpense != null) {
                        recurringExpenseViewModel.updateRecurringExpense(
                            recurringExpense
                        )
                    } else {
                        recurringExpenseViewModel.insertRecurringExpense(
                            recurringExpense
                        )
                    }

                    scope.launch {
                        kotlinx.coroutines.delay(200)
                        refreshRecurringExpenses()
                    }

                    title = ""
                    amount = ""
                    notes = ""
                    editingRecurringExpense = null
                } else {

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Please enter a title and a valid amount"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (editingRecurringExpense != null)
                    "Update Recurring Expense"
                else
                    "Save Recurring Expense"
            )
        }

        Text(
            text = "Saved Recurring Expenses",
            style = MaterialTheme.typography.headlineSmall
        )

        if (recurringExpenses.isEmpty()) {

            Text(
                text = "No recurring expenses set up yet"
            )
        }

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

                        val daysUntilDue = ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            LocalDate.parse(recurringExpense.nextDueDate)
                        )

                        Text(
                            when {
                                daysUntilDue < 0 -> "Overdue by ${-daysUntilDue} days"
                                daysUntilDue == 0L -> "Due today"
                                else -> "Due in $daysUntilDue days"
                            }
                        )

                        Text(
                            if (recurringExpense.isActive) "Active" else "Paused"
                        )

                        val setUpByLabel = when {
                            recurringExpense.addedBy.isBlank() -> ""
                            recurringExpense.addedBy == currentUserId -> "Set up by You"
                            else -> "Set up by ${memberEmails[recurringExpense.addedBy] ?: "a member"}"
                        }

                        if (setUpByLabel.isNotBlank()) {
                            Text(setUpByLabel)
                        }

                        Button(
                            onClick = {

                                title = recurringExpense.title
                                amount = recurringExpense.amount.toString()
                                notes = recurringExpense.notes
                                category = recurringExpense.category
                                paymentMethod = recurringExpense.paymentMethod
                                frequency = recurringExpense.frequency
                                editingRecurringExpense = recurringExpense
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Load Into Form")
                        }

                        Button(
                            onClick = {

                                recurringExpenseViewModel.updateRecurringExpense(
                                    recurringExpense.copy(
                                        isActive = !recurringExpense.isActive
                                    )
                                )

                                scope.launch {
                                    kotlinx.coroutines.delay(200)
                                    refreshRecurringExpenses()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                if (recurringExpense.isActive) "Pause" else "Resume"
                            )
                        }

                        Button(
                            onClick = {
                                recurringExpenseToDelete = recurringExpense
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

        recurringExpenseToDelete?.let { recurringExpense ->

            AlertDialog(
                onDismissRequest = {
                    recurringExpenseToDelete = null
                },
                title = {
                    Text("Delete Recurring Expense")
                },
                text = {
                    Text("Are you sure you want to delete this recurring expense?")
                },
                confirmButton = {
                    Button(
                        onClick = {

                            recurringExpenseViewModel.deleteRecurringExpense(
                                recurringExpense
                            )

                            scope.launch {
                                refreshRecurringExpenses()
                            }

                            recurringExpenseToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            recurringExpenseToDelete = null
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
