package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    householdViewModel: HouseholdViewModel,
    onViewExpensesClick: () -> Unit,
    onBack: () -> Unit
) {

    var amount by remember { mutableStateOf("") }

    var category by remember {
        mutableStateOf("")
    }

    var paymentMethod by remember {
        mutableStateOf("UPI")
    }

    var cardName by remember { mutableStateOf("") }

    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var paymentExpanded by remember {
        mutableStateOf(false)
    }

    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var categories by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    LaunchedEffect(Unit) {
        categories = householdViewModel.getCategories(
            SessionManager.currentHouseholdId
        )
        if (category.isBlank()) {
            category = categories.firstOrNull() ?: ""
        }
    }

    val paymentMethods = PAYMENT_METHODS

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    AppScaffold(
        title = "Add Expense",
        onBack = onBack
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            textStyle = MaterialTheme.typography.headlineSmall,
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

        if (paymentMethodUsesCard(paymentMethod)) {

            OutlinedTextField(
                value = cardName,
                onValueChange = { cardName = it },
                label = { Text("Card name") },
                placeholder = { Text("e.g. HDFC Regalia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        DateField(
            date = selectedDate,
            onClick = { showDatePicker = true }
        )

        if (showDatePicker) {

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

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

                val amountValue =
                    amount.toDoubleOrNull()

                if (amountValue == null || amountValue <= 0) {

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Please enter a valid amount"
                        )
                    }

                    return@Button
                }

                if (category.isBlank()) {

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Please pick a category"
                        )
                    }

                    return@Button
                }

                val expense = Expense(
                    amount = amountValue,
                    category = category,
                    paymentMethod = paymentMethod,
                    cardName = if (paymentMethodUsesCard(paymentMethod)) {
                        cardName.trim().ifBlank { null }
                    } else {
                        null
                    },
                    description = description,
                    notes = notes,
                    date = selectedDate.toString(),
                    time = LocalTime.now().toString(),
                    isRecurring = false
                )

                expenseViewModel.insertExpense(
                    expense
                )

                // Quick add has no payment field and reuses this.
                AppPreferences.saveLastPaymentMethod(context, paymentMethod)

                scope.launch {

                    val result = snackbarHostState.showSnackbar(
                        message = "Expense saved",
                        actionLabel = "View"
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        onViewExpensesClick()
                    }
                }

                amount = ""
                cardName = ""
                description = ""
                notes = ""
                selectedDate = LocalDate.now()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Save Expense",
                style = MaterialTheme.typography.labelLarge
            )
        }

        SnackbarHost(
            hostState = snackbarHostState
        )
    }
    }
}