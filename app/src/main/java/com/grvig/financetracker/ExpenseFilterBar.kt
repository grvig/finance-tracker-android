package com.grvig.financetracker

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipMenu(
    label: String,
    active: Boolean,
    options: List<String>,
    onSelect: (Int) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Box {

        FilterChip(
            selected = active,
            onClick = { expanded = true },
            label = {
                Text(label)
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            options.forEachIndexed { index, option ->

                DropdownMenuItem(
                    text = {
                        Text(option)
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExpenseFilterBar(
    filters: ExpenseFilters,
    categories: List<String>,
    paymentMethods: List<String>,
    onFiltersChange: (ExpenseFilters) -> Unit,
    onCustomRangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val categoryOptions = listOf(FILTER_ALL) + categories
    val paymentOptions = listOf(FILTER_ALL) + paymentMethods

    Column(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = filters.search,
            onValueChange = {
                onFiltersChange(filters.copy(search = it))
            },
            placeholder = {
                Text("Search expenses")
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (filters.search.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onFiltersChange(filters.copy(search = ""))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChipMenu(
                label = filters.dateLabel(),
                active = filters.dateRange != DateRange.ALL_TIME,
                options = DateRange.entries.map { it.label },
                onSelect = { index ->

                    val picked = DateRange.entries[index]

                    if (picked == DateRange.CUSTOM) {
                        onCustomRangeClick()
                    } else {
                        onFiltersChange(
                            filters.copy(
                                dateRange = picked,
                                customStart = null,
                                customEnd = null
                            )
                        )
                    }
                }
            )

            FilterChipMenu(
                label = if (filters.category == FILTER_ALL) {
                    "Category"
                } else {
                    filters.category
                },
                active = filters.category != FILTER_ALL,
                options = categoryOptions,
                onSelect = { index ->
                    onFiltersChange(
                        filters.copy(category = categoryOptions[index])
                    )
                }
            )

            FilterChipMenu(
                label = if (filters.paymentMethod == FILTER_ALL) {
                    "Payment"
                } else {
                    filters.paymentMethod
                },
                active = filters.paymentMethod != FILTER_ALL,
                options = paymentOptions,
                onSelect = { index ->
                    onFiltersChange(
                        filters.copy(paymentMethod = paymentOptions[index])
                    )
                }
            )

            FilterChipMenu(
                label = filters.sort.label,
                active = filters.sort != ExpenseSort.NEWEST,
                options = ExpenseSort.entries.map { it.label },
                onSelect = { index ->
                    onFiltersChange(
                        filters.copy(sort = ExpenseSort.entries[index])
                    )
                }
            )

            if (!filters.isDefault()) {

                FilterChipMenu(
                    label = "Clear",
                    active = false,
                    options = listOf("Clear all filters"),
                    onSelect = {
                        onFiltersChange(ExpenseFilters())
                    }
                )
            }
        }
    }
}

@Composable
fun FilterSummary(
    count: Int,
    total: Double,
    modifier: Modifier = Modifier
) {

    Text(
        text = "$count expense${if (count == 1) "" else "s"} · ${formatMoneyFull(total)}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
