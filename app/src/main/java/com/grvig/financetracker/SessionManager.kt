package com.grvig.financetracker

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private var appContext: Context? = null

    private val _householdId = MutableStateFlow("")

    /**
     * Observable so live queries can tear down and rebuild when the user joins
     * or leaves a household.
     */
    val householdId: StateFlow<String> = _householdId.asStateFlow()

    /**
     * Seeds the household from disk. Called on process start so the widget and
     * the quick add card work even when the app itself was never opened.
     *
     * The stored value can be stale if the user left the household from
     * another device; MainActivity re-resolves it against Firestore on launch.
     */
    fun restore(context: Context) {
        appContext = context.applicationContext
        _householdId.value = AppPreferences.loadHouseholdId(context)
    }

    var currentHouseholdId: String
        get() = _householdId.value
        set(value) {
            _householdId.value = value
            appContext?.let { AppPreferences.saveHouseholdId(it, value) }
        }
}
