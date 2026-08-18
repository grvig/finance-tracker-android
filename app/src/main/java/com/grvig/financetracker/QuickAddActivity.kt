package com.grvig.financetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.auth.FirebaseAuth
import com.grvig.financetracker.data.Expense
import com.grvig.financetracker.repository.ExpenseRepository
import com.grvig.financetracker.ui.theme.FinanceTrackerTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * A single card that floats over the launcher for adding an expense without
 * opening the app. Deliberately lean: no ViewModels, no navigation, one
 * repository, and everything it needs to draw is already on disk.
 */
class QuickAddActivity : ComponentActivity() {

    private val repository = ExpenseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val categories = AppPreferences.loadCategories(this)

        // Quick add only works once the app has been opened at least once:
        // it needs a signed in user, a household, and cached categories. Any
        // of those missing and we hand off to the app, which fills them in.
        val ready = FirebaseAuth.getInstance().currentUser != null &&
            SessionManager.currentHouseholdId.isNotBlank() &&
            categories.isNotEmpty()

        if (!ready) {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            finish()
            return
        }

        setContent {
            FinanceTrackerTheme(themeMode = ThemePreference.load(this)) {
                QuickAddScreen(
                    categories = categories,
                    onSave = { amount, category, description ->
                        save(amount, category, description)
                    },
                    onMoreOptions = { amount, category, description ->
                        openFullForm(amount, category, description)
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }

    /**
     * Dismisses straight away rather than waiting on Firestore. The write runs
     * on an application scope so closing this activity cannot cancel it, and
     * Firestore queues it locally if the device is offline.
     */
    private fun save(amount: Double, category: String, description: String) {

        val expense = Expense(
            amount = amount,
            category = category,
            paymentMethod = AppPreferences.loadLastPaymentMethod(this),
            description = description,
            date = LocalDate.now().toString(),
            time = LocalTime.now().toString()
        )

        AppScope.io.launch {
            repository.insertExpense(expense)
        }

        Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** Hands the part-typed expense to the full Add Expense screen. */
    private fun openFullForm(
        amount: String,
        category: String,
        description: String
    ) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(AddExpenseWidget.EXTRA_OPEN_ADD_EXPENSE, true)
                putExtra(EXTRA_AMOUNT, amount)
                putExtra(EXTRA_CATEGORY, category)
                putExtra(EXTRA_DESCRIPTION, description)
            }
        )
        finish()
    }

    override fun finish() {
        super.finish()
        // No slide animation; the card should feel like part of the launcher.
        overridePendingTransition(0, 0)
    }

    companion object {

        const val EXTRA_AMOUNT = "quick_add_amount"
        const val EXTRA_CATEGORY = "quick_add_category"
        const val EXTRA_DESCRIPTION = "quick_add_description"

        fun intent(context: Context): Intent {
            return Intent(context, QuickAddActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
        }
    }
}
