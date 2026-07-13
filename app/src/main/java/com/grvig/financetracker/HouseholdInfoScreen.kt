package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Household
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch

@Composable
fun HouseholdInfoScreen(
    householdViewModel: HouseholdViewModel,
    userId: String,
    onDashboardClick: () -> Unit,
    onLeaveHousehold: () -> Unit
) {

    var household by remember {
        mutableStateOf<Household?>(null)
    }

    var showLeaveConfirmation by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            household = householdViewModel.getHousehold(
                SessionManager.currentHouseholdId
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Household",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onDashboardClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back To Dashboard")
        }

        household?.let { current ->

            Text(
                text = "Household Code: ${current.code}"
            )

            Text(
                text = "Members: ${current.memberIds.size}"
            )

            Button(
                onClick = {
                    showLeaveConfirmation = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Leave Household")
            }
        }

        if (showLeaveConfirmation) {

            AlertDialog(
                onDismissRequest = {
                    showLeaveConfirmation = false
                },
                title = {
                    Text("Leave Household")
                },
                text = {
                    Text("Are you sure you want to leave this household?")
                },
                confirmButton = {
                    Button(
                        onClick = {

                            scope.launch {

                                householdViewModel.leaveHousehold(
                                    SessionManager.currentHouseholdId,
                                    userId
                                )

                                SessionManager.currentHouseholdId = ""
                                showLeaveConfirmation = false
                                onLeaveHousehold()
                            }
                        }
                    ) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showLeaveConfirmation = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
