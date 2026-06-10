package com.grvig.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

                ExpenseListScreen(
                    expenseViewModel = expenseViewModel
                )

            }
        }
    }
}