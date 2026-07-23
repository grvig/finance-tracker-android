package com.grvig.financetracker.data

val defaultCategories = listOf(
    "Food",
    "Transport",
    "Shopping",
    "Bills",
    "Health",
    "Entertainment"
)

data class Household(
    val id: String = "",
    val code: String = "",
    val memberIds: List<String> = emptyList(),
    val createdBy: String = "",
    val categories: List<String> = emptyList()
)
