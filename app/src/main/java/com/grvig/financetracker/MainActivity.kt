package com.grvig.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.grvig.financetracker.database.DatabaseProvider
import com.grvig.financetracker.repository.ExpenseRepository
import com.grvig.financetracker.ui.theme.FinanceTrackerTheme
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var expenseViewModel: ExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseProvider.getDatabase(this)

        val repository = ExpenseRepository(
            database.expenseDao()
        )

        val factory = ExpenseViewModelFactory(
            repository
        )

        expenseViewModel = ViewModelProvider(
            this,
            factory
        )[ExpenseViewModel::class.java]

        setContent {
            FinanceTrackerTheme {

                var currentScreen by remember {
                    mutableStateOf(Screen.DASHBOARD)
                }

                when (currentScreen) {

                    Screen.DASHBOARD -> {
                        DashboardScreen(
                            onAddExpenseClick = {
                                currentScreen =
                                    Screen.ADD_EXPENSE
                            },
                            onViewExpensesClick = {
                                currentScreen =
                                    Screen.EXPENSE_LIST
                            }
                        )
                    }

                    Screen.ADD_EXPENSE -> {
                        AddExpenseScreen(
                            expenseViewModel = expenseViewModel,
                            onViewExpensesClick = {
                                currentScreen =
                                    Screen.EXPENSE_LIST
                            }
                        )
                    }

                    Screen.EXPENSE_LIST -> {
                        ExpenseListScreen(
                            expenseViewModel = expenseViewModel,
                            onAddExpenseClick = {
                                currentScreen =
                                    Screen.ADD_EXPENSE
                            }
                        )
                    }
                }
            }
        }
    }
}