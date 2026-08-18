package com.grvig.financetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

        setContent {
            FinanceTrackerTheme(themeMode = ThemePreference.load(this)) {
                QuickAddScreen(
                    categories = AppPreferences.loadCategories(this),
                    onSave = { amount, category, description ->
                        save(amount, category, description)
                    },
                    onMoreOptions = { _, _, _ -> finish() },
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

    override fun finish() {
        super.finish()
        // No slide animation; the card should feel like part of the launcher.
        overridePendingTransition(0, 0)
    }

    companion object {
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
