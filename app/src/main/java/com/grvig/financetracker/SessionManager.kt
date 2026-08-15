package com.grvig.financetracker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private val _householdId = MutableStateFlow("")

    /**
     * Observable so live queries can tear down and rebuild when the user joins
     * or leaves a household.
     */
    val householdId: StateFlow<String> = _householdId.asStateFlow()

    var currentHouseholdId: String
        get() = _householdId.value
        set(value) {
            _householdId.value = value
        }
}
