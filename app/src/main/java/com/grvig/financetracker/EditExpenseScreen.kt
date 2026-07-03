package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    expense: Expense,
    expenseViewModel: ExpenseViewModel,
    onSaveClick: () -> Unit
) {

    var amount by remember {
        mutableStateOf(expense.amount.toString())
    }

    var category by remember {
        mutableStateOf(expense.category)
    }

    var paymentMethod by remember {
        mutableStateOf(expense.paymentMethod)
    }

    var description by remember {
        mutableStateOf(expense.description)
    }

    var notes by remember {
        mutableStateOf(expense.notes)
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var paymentExpanded by remember {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Edit Expense",
            style = MaterialTheme.typography.headlineMedium
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

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = {
                Text("Description")
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

        Button(
            onClick = {

                val updatedExpense =
                    expense.copy(
                        amount = amount.toDoubleOrNull() ?: expense.amount,
                        category = category,
                        paymentMethod = paymentMethod,
                        description = description,
                        notes = notes
                    )

                expenseViewModel.updateExpense(
                    updatedExpense
                )

                onSaveClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}