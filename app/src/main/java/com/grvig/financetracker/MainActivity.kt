package com.grvig.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.repository.ExpenseRepository
import com.grvig.financetracker.ui.theme.FinanceTrackerTheme
import com.grvig.financetracker.viewmodel.ExpenseViewModel
import com.grvig.financetracker.viewmodel.ExpenseViewModelFactory
import com.grvig.financetracker.repository.BudgetRepository
import com.grvig.financetracker.viewmodel.BudgetViewModel
import com.grvig.financetracker.viewmodel.BudgetViewModelFactory
import com.grvig.financetracker.repository.RecurringExpenseRepository
import com.grvig.financetracker.viewmodel.RecurringExpenseViewModel
import com.grvig.financetracker.viewmodel.RecurringExpenseViewModelFactory
import com.grvig.financetracker.repository.AuthRepository
import com.grvig.financetracker.viewmodel.AuthViewModel
import com.grvig.financetracker.viewmodel.AuthViewModelFactory
import com.grvig.financetracker.repository.HouseholdRepository
import com.grvig.financetracker.viewmodel.HouseholdViewModel
import com.grvig.financetracker.viewmodel.HouseholdViewModelFactory
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recurringExpenseViewModel: RecurringExpenseViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var householdViewModel: HouseholdViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = ExpenseRepository()

        val factory = ExpenseViewModelFactory(
            repository
        )

        expenseViewModel = ViewModelProvider(
            this,
            factory
        )[ExpenseViewModel::class.java]
        val budgetRepository = BudgetRepository()

        val budgetFactory = BudgetViewModelFactory(
            budgetRepository
        )

        budgetViewModel = ViewModelProvider(
            this,
            budgetFactory
        )[BudgetViewModel::class.java]

        val recurringExpenseRepository = RecurringExpenseRepository()

        val recurringExpenseFactory = RecurringExpenseViewModelFactory(
            recurringExpenseRepository
        )

        recurringExpenseViewModel = ViewModelProvider(
            this,
            recurringExpenseFactory
        )[RecurringExpenseViewModel::class.java]

        val authRepository = AuthRepository()

        val authFactory = AuthViewModelFactory(
            authRepository
        )

        authViewModel = ViewModelProvider(
            this,
            authFactory
        )[AuthViewModel::class.java]

        val householdRepository = HouseholdRepository()

        val householdFactory = HouseholdViewModelFactory(
            householdRepository
        )

        householdViewModel = ViewModelProvider(
            this,
            householdFactory
        )[HouseholdViewModel::class.java]

        setContent {
            FinanceTrackerTheme {

                var currentScreen by remember {
                    mutableStateOf(
                        if (authViewModel.currentUser != null)
                            Screen.LOADING
                        else
                            Screen.LOGIN
                    )
                }

                var selectedExpense by remember {
                    mutableStateOf<Expense?>(null)
                }

                val scope = rememberCoroutineScope()

                when (currentScreen) {

                    Screen.LOADING -> {

                        LaunchedEffect(Unit) {
                            scope.launch {

                                val userId = authViewModel.currentUser?.uid ?: ""

                                val profile = householdViewModel.getUserProfile(
                                    userId
                                )

                                SessionManager.currentHouseholdId =
                                    profile?.householdId ?: ""

                                currentScreen = if (SessionManager.currentHouseholdId.isBlank())
                                    Screen.HOUSEHOLD_SETUP
                                else
                                    Screen.DASHBOARD
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading...")
                        }
                    }

                    Screen.LOGIN -> {

                        LoginScreen(
                            authViewModel = authViewModel,
                            householdViewModel = householdViewModel,
                            onLoginSuccess = { hasHousehold ->
                                currentScreen = if (hasHousehold)
                                    Screen.DASHBOARD
                                else
                                    Screen.HOUSEHOLD_SETUP
                            },
                            onSignUpClick = {
                                currentScreen =
                                    Screen.SIGNUP
                            }
                        )
                    }

                    Screen.SIGNUP -> {

                        SignUpScreen(
                            authViewModel = authViewModel,
                            householdViewModel = householdViewModel,
                            onSignUpSuccess = {
                                currentScreen =
                                    Screen.HOUSEHOLD_SETUP
                            },
                            onLoginClick = {
                                currentScreen =
                                    Screen.LOGIN
                            }
                        )
                    }

                    Screen.HOUSEHOLD_SETUP -> {

                        HouseholdSetupScreen(
                            householdViewModel = householdViewModel,
                            userId = authViewModel.currentUser?.uid ?: "",
                            onHouseholdReady = {
                                currentScreen =
                                    Screen.DASHBOARD
                            }
                        )
                    }

                    Screen.DASHBOARD -> {
                        DashboardScreen(
                            expenseViewModel = expenseViewModel,
                            budgetViewModel = budgetViewModel,
                            recurringExpenseViewModel = recurringExpenseViewModel,
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
                            },
                            onRecurringExpensesClick = {
                                currentScreen =
                                    Screen.RECURRING_EXPENSES
                            },
                            onReportsClick = {
                                currentScreen =
                                    Screen.REPORTS
                            },
                            onHouseholdClick = {
                                currentScreen =
                                    Screen.HOUSEHOLD_INFO
                            },
                            onSignOutClick = {
                                authViewModel.signOut()
                                SessionManager.currentHouseholdId = ""
                                currentScreen =
                                    Screen.LOGIN
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
                            budgetViewModel = budgetViewModel,
                            expenseViewModel = expenseViewModel
                        )
                    }

                    Screen.RECURRING_EXPENSES -> {

                        RecurringExpensesScreen(
                            recurringExpenseViewModel = recurringExpenseViewModel,
                            onDashboardClick = {
                                currentScreen =
                                    Screen.DASHBOARD
                            }
                        )
                    }

                    Screen.REPORTS -> {

                        ReportsScreen(
                            expenseViewModel = expenseViewModel,
                            budgetViewModel = budgetViewModel,
                            onDashboardClick = {
                                currentScreen =
                                    Screen.DASHBOARD
                            }
                        )
                    }

                    Screen.HOUSEHOLD_INFO -> {

                        HouseholdInfoScreen(
                            householdViewModel = householdViewModel,
                            userId = authViewModel.currentUser?.uid ?: "",
                            onDashboardClick = {
                                currentScreen =
                                    Screen.DASHBOARD
                            },
                            onLeaveHousehold = {
                                currentScreen =
                                    Screen.HOUSEHOLD_SETUP
                            }
                        )
                    }
                }
            }
        }
    }
}