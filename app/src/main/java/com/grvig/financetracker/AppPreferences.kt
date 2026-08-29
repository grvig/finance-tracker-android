package com.grvig.financetracker

import android.content.Context

/**
 * Small values that have to survive process death so entry points outside
 * MainActivity (the widget, the quick add card) can work without the app
 * having been opened first.
 */
object AppPreferences {

    private const val PREFS_NAME = "finance_tracker_settings"
    private const val KEY_HOUSEHOLD_ID = "household_id"
    private const val KEY_CATEGORIES = "cached_categories"
    private const val KEY_LAST_PAYMENT_METHOD = "last_payment_method"
    private const val KEY_LAST_CATEGORY = "last_category"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadHouseholdId(context: Context): String {
        return prefs(context).getString(KEY_HOUSEHOLD_ID, "") ?: ""
    }

    fun saveHouseholdId(context: Context, householdId: String) {
        prefs(context).edit().putString(KEY_HOUSEHOLD_ID, householdId).apply()
    }

    /**
     * Categories are cached so the quick add card can draw its chips without
     * waiting on a Firestore round trip.
     */
    fun loadCategories(context: Context): List<String> {
        val stored = prefs(context).getString(KEY_CATEGORIES, "") ?: ""
        return if (stored.isBlank()) {
            emptyList()
        } else {
            stored.split("\n").filter { it.isNotBlank() }
        }
    }

    fun saveCategories(context: Context, categories: List<String>) {
        prefs(context)
            .edit()
            .putString(KEY_CATEGORIES, categories.joinToString("\n"))
            .apply()
    }

    fun loadLastPaymentMethod(context: Context): String {
        return prefs(context).getString(KEY_LAST_PAYMENT_METHOD, null)
            ?: PAYMENT_METHODS.first()
    }

    fun saveLastPaymentMethod(context: Context, method: String) {
        prefs(context).edit().putString(KEY_LAST_PAYMENT_METHOD, method).apply()
    }

    /**
     * Household spending clusters, so the category used last is a better first
     * guess than whatever happens to sort first.
     */
    fun loadLastCategory(context: Context): String {
        return prefs(context).getString(KEY_LAST_CATEGORY, null).orEmpty()
    }

    fun saveLastCategory(context: Context, category: String) {
        prefs(context).edit().putString(KEY_LAST_CATEGORY, category).apply()
    }
}
