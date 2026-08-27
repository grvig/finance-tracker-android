package com.grvig.financetracker

import android.content.Intent
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /**
     * Set when the widget launches us. Consumed once the household has
     * resolved, since routing before that would land on a screen with no
     * household id.
     */
    private var pendingAddExpense by mutableStateOf(false)

    /** Values typed into the quick add card before "More options" was tapped. */
    private var pendingAmount by mutableStateOf("")
    private var pendingCategory by mutableStateOf("")
    private var pendingDescription by mutableStateOf("")

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recurringExpenseViewModel: RecurringExpenseViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var householdViewModel: HouseholdViewModel

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readAddExpenseExtras(intent)
    }

    private fun readAddExpenseExtras(intent: Intent) {
        if (!intent.getBooleanExtra(AddExpenseWidget.EXTRA_OPEN_ADD_EXPENSE, false)) {
            return
        }
        pendingAddExpense = true
        pendingAmount = intent.getStringExtra(QuickAddActivity.EXTRA_AMOUNT) ?: ""
        pendingCategory = intent.getStringExtra(QuickAddActivity.EXTRA_CATEGORY) ?: ""
        pendingDescription =
            intent.getStringExtra(QuickAddActivity.EXTRA_DESCRIPTION) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readAddExpenseExtras(intent)

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

            var themeMode by remember {
                mutableStateOf(ThemePreference.load(this))
            }

            FinanceTrackerTheme(themeMode = themeMode) {

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

                var expenseListCategory by remember {
                    mutableStateOf(FILTER_ALL)
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

                // Handles both a cold launch (fires once LOADING resolves to a
                // real screen) and a warm one (fires when onNewIntent flips the
                // flag). Waits for a household so we never land on Add Expense
                // without one.
                LaunchedEffect(pendingAddExpense, currentScreen) {

                    val ready = currentScreen != Screen.LOADING &&
                        currentScreen != Screen.LOGIN &&
                        currentScreen != Screen.SIGNUP &&
                        currentScreen != Screen.HOUSEHOLD_SETUP &&
                        SessionManager.currentHouseholdId.isNotBlank()

                    if (pendingAddExpense && ready) {
                        pendingAddExpense = false
                        if (currentScreen != Screen.ADD_EXPENSE) {
                            navigateTo(Screen.ADD_EXPENSE)
                        }
                    }
                }

                val drawerState = rememberDrawerState(DrawerValue.Closed)

                val drawerScreens = setOf(
                    Screen.DASHBOARD,
                    Screen.ADD_EXPENSE,
                    Screen.EXPENSE_LIST,
                    Screen.MY_EXPENSES,
                    Screen.BUDGET,
                    Screen.RECURRING_EXPENSES,
                    Screen.REPORTS
                )

                val drawerEnabled = currentScreen in drawerScreens

                var drawerMemberCount by remember { mutableStateOf(0) }

                LaunchedEffect(drawerEnabled, SessionManager.currentHouseholdId) {
                    if (drawerEnabled && SessionManager.currentHouseholdId.isNotBlank()) {
                        drawerMemberCount = householdViewModel.getHousehold(
                            SessionManager.currentHouseholdId
                        )?.memberIds?.size ?: 0
                    }
                }

                LaunchedEffect(currentScreen) {
                    drawerState.close()
                }

                BackHandler(enabled = drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                }

                fun openDrawer() {
                    scope.launch { drawerState.open() }
                }

                fun navigateFromDrawer(screen: Screen) {
                    scope.launch { drawerState.close() }
                    expenseListCategory = FILTER_ALL
                    if (screen != currentScreen) {
                        if (screen == Screen.DASHBOARD) {
                            resetTo(Screen.DASHBOARD)
                        } else {
                            resetTo(Screen.DASHBOARD)
                            navigateTo(screen)
                        }
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = drawerEnabled,
                    drawerContent = {
                        if (drawerEnabled) {
                            AppDrawerContent(
                                currentScreen = currentScreen,
                                userEmail = authViewModel.currentUser?.email ?: "",
                                memberCount = drawerMemberCount,
                                onNavigate = { navigateFromDrawer(it) },
                                onHouseholdClick = {
                                    scope.launch { drawerState.close() }
                                    resetTo(Screen.DASHBOARD)
                                    navigateTo(Screen.HOUSEHOLD_INFO)
                                },
                                onSignOutClick = {
                                    scope.launch { drawerState.close() }
                                    authViewModel.signOut()
                                    SessionManager.currentHouseholdId = ""
                                    resetTo(Screen.LOGIN)
                                }
                            )
                        }
                    }
                ) {

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

                                // Cached so the quick add card can draw its
                                // category chips without a Firestore round trip.
                                if (SessionManager.currentHouseholdId.isNotBlank()) {
                                    AppPreferences.saveCategories(
                                        this@MainActivity,
                                        householdViewModel.getCategories(
                                            SessionManager.currentHouseholdId
                                        )
                                    )
                                }

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
                                expenseListCategory = FILTER_ALL
                                navigateTo(Screen.EXPENSE_LIST)
                            },
                            onCategoryClick = { category ->
                                expenseListCategory = category
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
                            },
                            onOpenDrawer = { openDrawer() }
                        )
                    }

                    Screen.ADD_EXPENSE -> {

                        // Read before clearing so the first composition seeds
                        // the form and a later visit starts empty.
                        val seedAmount = pendingAmount
                        val seedCategory = pendingCategory
                        val seedDescription = pendingDescription

                        LaunchedEffect(Unit) {
                            pendingAmount = ""
                            pendingCategory = ""
                            pendingDescription = ""
                        }

                        AddExpenseScreen(
                            expenseViewModel = expenseViewModel,
                            householdViewModel = householdViewModel,
                            onViewExpensesClick = {
                                navigateTo(Screen.EXPENSE_LIST)
                            },
                            onBack = {
                                goBack()
                            },
                            initialAmount = seedAmount,
                            initialCategory = seedCategory,
                            initialDescription = seedDescription
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
                            onOpenDrawer = { openDrawer() },
                            onEditExpenseClick = { expense ->

                                selectedExpense = expense

                                navigateTo(Screen.EDIT_EXPENSE)
                            },
                            initialCategory = expenseListCategory
                        )
                    }

                    Screen.EDIT_EXPENSE -> {

                        selectedExpense?.let { expense ->

                            EditExpenseScreen(
                                expense = expense,
                                expenseViewModel = expenseViewModel,
                                householdViewModel = householdViewModel,
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
                            householdViewModel = householdViewModel,
                            onBack = {
                                goBack()
                            },
                            onOpenDrawer = { openDrawer() }
                        )
                    }

                    Screen.RECURRING_EXPENSES -> {

                        RecurringExpensesScreen(
                            recurringExpenseViewModel = recurringExpenseViewModel,
                            householdViewModel = householdViewModel,
                            onBack = {
                                goBack()
                            },
                            onOpenDrawer = { openDrawer() }
                        )
                    }

                    Screen.REPORTS -> {

                        ReportsScreen(
                            expenseViewModel = expenseViewModel,
                            budgetViewModel = budgetViewModel,
                            householdViewModel = householdViewModel,
                            onBack = {
                                goBack()
                            },
                            onOpenDrawer = { openDrawer() },
                            onCategoryClick = { category ->
                                expenseListCategory = category
                                navigateTo(Screen.EXPENSE_LIST)
                            }
                        )
                    }

                    Screen.MY_EXPENSES -> {

                        MyExpensesScreen(
                            expenseViewModel = expenseViewModel,
                            householdViewModel = householdViewModel,
                            onEditExpenseClick = { expense ->
                                selectedExpense = expense
                                navigateTo(Screen.EDIT_EXPENSE)
                            },
                            onBack = {
                                goBack()
                            },
                            onOpenDrawer = { openDrawer() }
                        )
                    }

                    Screen.HOUSEHOLD_INFO -> {

                        HouseholdInfoScreen(
                            householdViewModel = householdViewModel,
                            userId = authViewModel.currentUser?.uid ?: "",
                            themeMode = themeMode,
                            onThemeModeChange = { mode ->
                                themeMode = mode
                                ThemePreference.save(this@MainActivity, mode)
                            },
                            onBack = {
                                goBack()
                            },
                            onManageCategoriesClick = {
                                navigateTo(Screen.MANAGE_CATEGORIES)
                            },
                            onNotificationsClick = {
                                navigateTo(Screen.NOTIFICATION_SETTINGS)
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

                    Screen.NOTIFICATION_SETTINGS -> {

                        NotificationSettingsScreen(
                            householdViewModel = householdViewModel,
                            currentUserId = authViewModel.currentUser?.uid ?: "",
                            onBack = {
                                goBack()
                            },
                            onMasterEnabled = { }
                        )
                    }
                }
                }
            }
        }
    }
}