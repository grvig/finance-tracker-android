package com.grvig.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.database.DatabaseProvider
import com.grvig.financetracker.repository.ExpenseRepository
import com.grvig.financetracker.ui.theme.FinanceTrackerTheme
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.ExpenseViewModelFactory
import com.grvig.financetracker.repository.BudgetRepository
import com.grvig.financetracker.viewmodel.BudgetViewModel
import com.grvig.financetracker.viewmodel.BudgetViewModelFactory
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var budgetViewModel: BudgetViewModel

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
        val budgetRepository = BudgetRepository(
            database.budgetDao()
        )

        val budgetFactory = BudgetViewModelFactory(
            budgetRepository
        )

        budgetViewModel = ViewModelProvider(
            this,
            budgetFactory
        )[BudgetViewModel::class.java]

        setContent {
            FinanceTrackerTheme {

                var currentScreen by remember {
                    mutableStateOf(Screen.DASHBOARD)
                }

                var selectedExpense by remember {
                    mutableStateOf<Expense?>(null)
                }

                when (currentScreen) {

                    Screen.DASHBOARD -> {
                        DashboardScreen(
                            expenseViewModel = expenseViewModel,
                            budgetViewModel = budgetViewModel,
                            onAddExpenseClick = {
                                currentScreen =
                                    Screen.ADD_EXPENSE
                            },
                            onViewExpensesClick = {
                                currentScreen =
                                    Screen.EXPENSE_LIST
                            },
                            onBudgetClick = {
                                currentScreen =
                                    Screen.BUDGET
                            }
                        )
                    }

                    Screen.ADD_EXPENSE -> {
                        AddExpenseScreen(
                            expenseViewModel = expenseViewModel,
                            onViewExpensesClick = {
                                currentScreen =
                                    Screen.EXPENSE_LIST
                            },
                            onDashboardClick = {
                                currentScreen =
                                    Screen.DASHBOARD
                            }
                        )
                    }

                    Screen.EXPENSE_LIST -> {
                        ExpenseListScreen(
                            expenseViewModel = expenseViewModel,
                            onAddExpenseClick = {
                                currentScreen =
                                    Screen.ADD_EXPENSE
                            },
                            onDashboardClick = {
                                currentScreen =
                                    Screen.DASHBOARD
                            },
                            onEditExpenseClick = { expense ->

                                selectedExpense = expense

                                currentScreen =
                                    Screen.EDIT_EXPENSE
                            }
                        )
                    }

                    Screen.EDIT_EXPENSE -> {

                        selectedExpense?.let { expense ->

                            EditExpenseScreen(
                                expense = expense,
                                expenseViewModel = expenseViewModel,
                                onSaveClick = {
                                    currentScreen =
                                        Screen.EXPENSE_LIST
                                }
                            )
                        }
                    }
                    Screen.BUDGET -> {

                        BudgetScreen(
                            budgetViewModel = budgetViewModel
                        )
                    }
                }
            }
        }
    }
}