package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    onViewExpensesClick: () -> Unit,
    onDashboardClick: () -> Unit
) {

    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Add Expense",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            onClick = onDashboardClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Dashboard")
        }

        Button(
            onClick = onViewExpensesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Expenses")
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = paymentMethod,
            onValueChange = { paymentMethod = it },
            label = { Text("Payment Method") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                val amountValue = amount.toDoubleOrNull()

                if (
                    amountValue == null ||
                    category.isBlank() ||
                    paymentMethod.isBlank()
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Please fill required fields"
                        )
                    }
                    return@Button
                }

                val expense = Expense(
                    amount = amountValue,
                    category = category,
                    paymentMethod = paymentMethod,
                    cardName = null,
                    description = description,
                    notes = notes,
                    date = LocalDate.now().toString(),
                    time = LocalTime.now().toString(),
                    isRecurring = false
                )

                expenseViewModel.insertExpense(expense)

                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Expense saved to database"
                    )
                }

                amount = ""
                category = ""
                paymentMethod = ""
                description = ""
                notes = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Expense")
        }

        SnackbarHost(
            hostState = snackbarHostState
        )
    }
}