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
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    expenseViewModel: ExpenseViewModel,
    onDashboardClick: () -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    val months = (0..11).map { monthsAgo ->
        LocalDate.now()
            .minusMonths(monthsAgo.toLong())
            .toString()
            .substring(0, 7)
    }

    var selectedMonth by remember {
        mutableStateOf(months.first())
    }

    var monthExpanded by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
        }
    }

    val monthExpenses = expenses.filter {
        it.date.startsWith(selectedMonth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onDashboardClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Dashboard")
        }

        ExposedDropdownMenuBox(
            expanded = monthExpanded,
            onExpandedChange = {
                monthExpanded = it
            }
        ) {

            OutlinedTextField(
                value = selectedMonth,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Month")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = monthExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = {
                    monthExpanded = false
                }
            ) {

                months.forEach { month ->

                    DropdownMenuItem(
                        text = {
                            Text(month)
                        },
                        onClick = {
                            selectedMonth = month
                            monthExpanded = false
                        }
                    )
                }
            }
        }
    }
}
