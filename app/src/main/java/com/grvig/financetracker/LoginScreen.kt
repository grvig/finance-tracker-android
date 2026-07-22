package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.viewmodel.AuthViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    householdViewModel: HouseholdViewModel,
    onLoginSuccess: (Boolean) -> Unit,
    onSignUpClick: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    AppScaffold(
        title = "Login"
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Email")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Text(text = it)
        }

        Button(
            onClick = {

                errorMessage = null
                isLoading = true

                scope.launch {

                    val result = authViewModel.signIn(
                        email,
                        password
                    )

                    result.onSuccess { user ->

                        val profile = householdViewModel.getUserProfile(
                            user.uid
                        )

                        SessionManager.currentHouseholdId =
                            profile?.householdId ?: ""

                        isLoading = false

                        onLoginSuccess(
                            !SessionManager.currentHouseholdId.isBlank()
                        )
                    }

                    result.onFailure {
                        isLoading = false
                        errorMessage = it.message ?: "Login failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isLoading) "Logging In..." else "Login"
            )
        }

        Button(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account? Sign Up")
        }
    }
    }
}
