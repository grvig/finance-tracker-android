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
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch

@Composable
fun HouseholdSetupScreen(
    householdViewModel: HouseholdViewModel,
    userId: String,
    onHouseholdReady: () -> Unit
) {

    var joinCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var createdCode by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Set Up Household",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Create a new household or join an existing one with a code."
        )

        Button(
            onClick = {

                errorMessage = null
                isLoading = true

                scope.launch {

                    val result = householdViewModel.createHousehold(
                        userId
                    )

                    isLoading = false

                    result.onSuccess { household ->
                        SessionManager.currentHouseholdId = household.id
                        createdCode = household.code
                    }

                    result.onFailure {
                        errorMessage = it.message ?: "Could not create household"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isLoading) "Working..." else "Create New Household"
            )
        }

        createdCode?.let { code ->

            Text(
                text = "Household created! Share this code: $code"
            )

            Button(
                onClick = onHouseholdReady,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }

        Text(
            text = "— or join an existing household —"
        )

        OutlinedTextField(
            value = joinCode,
            onValueChange = {
                joinCode = it
            },
            label = {
                Text("Household Code")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                errorMessage = null
                isLoading = true

                scope.launch {

                    val result = householdViewModel.joinHousehold(
                        joinCode.trim().uppercase(),
                        userId
                    )

                    isLoading = false

                    result.onSuccess { household ->
                        SessionManager.currentHouseholdId = household.id
                        onHouseholdReady()
                    }

                    result.onFailure {
                        errorMessage = it.message ?: "Could not join household"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isLoading) "Working..." else "Join Household"
            )
        }

        errorMessage?.let {
            Text(text = it)
        }
    }
}
