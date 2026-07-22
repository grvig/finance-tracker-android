package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

@Composable
fun MyExpensesScreen(
    expenseViewModel: ExpenseViewModel,
    onBack: () -> Unit
) {

    var expenses by remember {
        mutableStateOf<List<Expense>>(emptyList())
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            expenses = expenseViewModel.getAllExpenses()
        }
    }

    val myExpenses = expenses
        .filter {
            it.addedBy == currentUserId
        }
        .sortedByDescending {
            it.date + it.time
        }

    val myTotal = myExpenses.sumOf {
        it.amount
    }

    AppScaffold(
        title = "My Expenses",
        onBack = onBack
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Your Spending",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Total Spent: ₹$myTotal")
                    Text("Expenses: ${myExpenses.size}")
                }
            }

            if (myExpenses.isEmpty()) {
                Text(
                    text = "You haven't added any expenses yet",
                    modifier = Modifier.padding(16.dp)
                )
            }

            LazyColumn {

                items(myExpenses) { expense ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 6.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("₹${expense.amount}")
                            Text(expense.category)
                            Text(expense.date)
                            if (expense.description.isNotBlank()) {
                                Text(expense.description)
                            }
                        }
                    }
                }
            }
        }
    }
}
