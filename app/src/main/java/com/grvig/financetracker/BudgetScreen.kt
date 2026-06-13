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

@Composable
fun BudgetScreen() {

    var category by remember {
        mutableStateOf("")
    }

    var monthlyLimit by remember {
        mutableStateOf("")
    }

    var warningPercent by remember {
        mutableStateOf("")
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
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Budget")
        }
    }
}