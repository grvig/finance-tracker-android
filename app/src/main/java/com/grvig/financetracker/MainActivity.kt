package com.grvig.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
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

                val backStack = remember {
                    mutableStateListOf(
                        if (authViewModel.currentUser != null)
                            Screen.LOADING
                        else
                            Screen.LOGIN
                    )
                }

                val currentScreen = backStack.last()

                var selectedExpense by remember {
                    mutableStateOf<Expense?>(null)
                }

                val scope = rememberCoroutineScope()

                fun navigateTo(screen: Screen) {
                    backStack.add(screen)
                }

                fun goBack() {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.size - 1)
                    }
                }

                fun resetTo(screen: Screen) {
                    backStack.clear()
                    backStack.add(screen)
                }

                BackHandler(enabled = backStack.size > 1) {
                    goBack()
                }

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

                                resetTo(
                                    if (SessionManager.currentHouseholdId.isBlank())
                                        Screen.HOUSEHOLD_SETUP
                                    else
                                        Screen.DASHBOARD
                                )
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
                                resetTo(
                                    if (hasHousehold)
                                        Screen.DASHBOARD
                                    else
                                        Screen.HOUSEHOLD_SETUP
                                )
                            },
                            onSignUpClick = {
                                navigateTo(Screen.SIGNUP)
                            }
                        )
                    }

                    Screen.SIGNUP -> {

                        SignUpScreen(
                            authViewModel = authViewModel,
                            householdViewModel = householdViewModel,
                            onSignUpSuccess = {
                                resetTo(Screen.HOUSEHOLD_SETUP)
                            },
                            onLoginClick = {
                                goBack()
                            }
                        )
                    }

                    Screen.HOUSEHOLD_SETUP -> {

                        HouseholdSetupScreen(
                            householdViewModel = householdViewModel,
                            userId = authViewModel.currentUser?.uid ?: "",
                            onHouseholdReady = {
                                resetTo(Screen.DASHBOARD)
                            }
                        )
                    }

                    Screen.DASHBOARD -> {
                        DashboardScreen(
                            expenseViewModel = expenseViewModel,
                            budgetViewModel = budgetViewModel,
                            recurringExpenseViewModel = recurringExpenseViewModel,
                            householdViewModel = householdViewModel,
                            onAddExpenseClick = {
                                navigateTo(Screen.ADD_EXPENSE)
                            },
                            onViewExpensesClick = {
                                navigateTo(Screen.EXPENSE_LIST)
                            },
                            onMyExpensesClick = {
                                navigateTo(Screen.MY_EXPENSES)
                            },
                            onBudgetClick = {
                                navigateTo(Screen.BUDGET)
                            },
                            onRecurringExpensesClick = {
                                navigateTo(Screen.RECURRING_EXPENSES)
                            },
                            onReportsClick = {
                                navigateTo(Screen.REPORTS)
                            },
                            onHouseholdClick = {
                                navigateTo(Screen.HOUSEHOLD_INFO)
                            },
                            onSignOutClick = {
                                authViewModel.signOut()
                                SessionManager.currentHouseholdId = ""
                                resetTo(Screen.LOGIN)
                            }
                        )
                    }

                    Screen.ADD_EXPENSE -> {
                        AddExpenseScreen(
                            expenseViewModel = expenseViewModel,
                            onViewExpensesClick = {
                                navigateTo(Screen.EXPENSE_LIST)
                            },
                            onBack = {
                                goBack()
                            }
                        )
                    }

                    Screen.EXPENSE_LIST -> {
                        ExpenseListScreen(
                            expenseViewModel = expenseViewModel,
                            householdViewModel = householdViewModel,
                            onAddExpenseClick = {
                                navigateTo(Screen.ADD_EXPENSE)
                            },
                            onBack = {
                                goBack()
                            },
                            onEditExpenseClick = { expense ->

                                selectedExpense = expense

                                navigateTo(Screen.EDIT_EXPENSE)
                            }
                        )
                    }

                    Screen.EDIT_EXPENSE -> {

                        selectedExpense?.let { expense ->

                            EditExpenseScreen(
                                expense = expense,
                                expenseViewModel = expenseViewModel,
                                onSaveClick = {
                                    goBack()
                                },
                                onBack = {
                                    goBack()
                                }
                            )
                        }
                    }
                    Screen.BUDGET -> {

                        BudgetScreen(
                            budgetViewModel = budgetViewModel,
                            expenseViewModel = expenseViewModel,
                            onBack = {
                                goBack()
                            }
                        )
                    }

                    Screen.RECURRING_EXPENSES -> {

                        RecurringExpensesScreen(
                            recurringExpenseViewModel = recurringExpenseViewModel,
                            householdViewModel = householdViewModel,
                            onBack = {
                                goBack()
                            }
                        )
                    }

                    Screen.REPORTS -> {

                        ReportsScreen(
                            expenseViewModel = expenseViewModel,
                            budgetViewModel = budgetViewModel,
                            householdViewModel = householdViewModel,
                            onBack = {
                                goBack()
                            }
                        )
                    }

                    Screen.MY_EXPENSES -> {

                        MyExpensesScreen(
                            expenseViewModel = expenseViewModel,
                            onBack = {
                                goBack()
                            }
                        )
                    }

                    Screen.HOUSEHOLD_INFO -> {

                        HouseholdInfoScreen(
                            householdViewModel = householdViewModel,
                            userId = authViewModel.currentUser?.uid ?: "",
                            onBack = {
                                goBack()
                            },
                            onManageCategoriesClick = {
                                navigateTo(Screen.MANAGE_CATEGORIES)
                            },
                            onLeaveHousehold = {
                                resetTo(Screen.HOUSEHOLD_SETUP)
                            }
                        )
                    }

                    Screen.MANAGE_CATEGORIES -> {

                        ManageCategoriesScreen(
                            householdViewModel = householdViewModel,
                            onBack = {
                                goBack()
                            }
                        )
                    }
                }
            }
        }
    }
}