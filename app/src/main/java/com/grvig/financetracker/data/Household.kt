package com.grvig.financetracker.data

data class Household(
    val id: String = "",
    val code: String = "",
    val memberIds: List<String> = emptyList(),
    val createdBy: String = ""
)
