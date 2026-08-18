package com.grvig.financetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.grvig.financetracker.ui.theme.FinanceTrackerTheme

/**
 * A single card that floats over the launcher for adding an expense without
 * opening the app. Deliberately lean: no ViewModels, no navigation, one
 * repository, and everything it needs to draw is already on disk.
 */
class QuickAddActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinanceTrackerTheme(themeMode = ThemePreference.load(this)) {
                QuickAddScreen(onDismiss = { finish() })
            }
        }
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
