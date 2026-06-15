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

@Composable
fun EditBudgetScreen(
    budget: Budget,
    budgetViewModel: BudgetViewModel,
    onSaveClick: () -> Unit
) {

    var category by remember {
        mutableStateOf(budget.category)
    }

    var monthlyLimit by remember {
        mutableStateOf(
            budget.monthlyLimit.toString()
        )
    }

    var warningPercent by remember {
        mutableStateOf(
            budget.warningPercent.toString()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Edit Budget",
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

                val updatedBudget =
                    budget.copy(
                        category = category,
                        monthlyLimit =
                            monthlyLimit.toDoubleOrNull()
                                ?: budget.monthlyLimit,
                        warningPercent =
                            warningPercent.toIntOrNull()
                                ?: budget.warningPercent
                    )

                budgetViewModel.updateBudget(
                    updatedBudget
                )

                onSaveClick()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}