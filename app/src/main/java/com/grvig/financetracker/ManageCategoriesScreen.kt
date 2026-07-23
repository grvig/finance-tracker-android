package com.grvig.financetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch

@Composable
fun ManageCategoriesScreen(
    householdViewModel: HouseholdViewModel,
    onBack: () -> Unit
) {

    var categories by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var newCategory by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var categoryToDelete by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    fun refreshCategories() {
        scope.launch {
            categories = householdViewModel.getCategories(
                SessionManager.currentHouseholdId
            )
        }
    }

    LaunchedEffect(Unit) {
        refreshCategories()
    }

    AppScaffold(
        title = "Categories",
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
                value = newCategory,
                onValueChange = {
                    newCategory = it
                },
                label = {
                    Text("New Category")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {

                    errorMessage = null

                    val trimmed = newCategory.trim()

                    if (trimmed.isBlank()) {
                        errorMessage = "Please enter a category name"
                        return@Button
                    }

                    scope.launch {

                        val result = householdViewModel.addCategory(
                            SessionManager.currentHouseholdId,
                            trimmed
                        )

                        result.onSuccess {
                            newCategory = ""
                            refreshCategories()
                        }

                        result.onFailure {
                            errorMessage = it.message ?: "Could not add category"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Category")
            }

            errorMessage?.let {
                Text(text = it)
            }

            Text(
                text = "Your Categories",
                style = MaterialTheme.typography.titleMedium
            )

            categories.forEach { category ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(text = category)

                        Button(
                            onClick = {
                                categoryToDelete = category
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }

            categoryToDelete?.let { category ->

                AlertDialog(
                    onDismissRequest = {
                        categoryToDelete = null
                    },
                    title = {
                        Text("Delete Category")
                    },
                    text = {
                        Text("Delete \"$category\"? Existing expenses keep their category label.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {

                                scope.launch {

                                    val result = householdViewModel.removeCategory(
                                        SessionManager.currentHouseholdId,
                                        category
                                    )

                                    categoryToDelete = null

                                    result.onSuccess {
                                        refreshCategories()
                                    }

                                    result.onFailure {
                                        errorMessage = it.message ?: "Could not delete category"
                                    }
                                }
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                categoryToDelete = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
