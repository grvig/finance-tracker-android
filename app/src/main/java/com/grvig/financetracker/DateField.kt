package com.grvig.financetracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val fullDayFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

fun formatFullDate(date: LocalDate): String {
    return date.format(fullDayFormatter)
}

@Composable
fun DateField(
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = formatFullDate(date),
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Date")
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Pick a date"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}
