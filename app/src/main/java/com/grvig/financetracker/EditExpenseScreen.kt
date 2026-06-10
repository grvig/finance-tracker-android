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
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel

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
            value = paymentMethod,
            onValueChange = {
                paymentMethod = it
            },
            label = {
                Text("Payment Method")
            },
            modifier = Modifier.fillMaxWidth()
        )

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