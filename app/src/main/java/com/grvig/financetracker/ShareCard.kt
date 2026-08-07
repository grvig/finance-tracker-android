package com.grvig.financetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grvig.financetracker.data.Expense
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Fixed width keeps the exported PNG a predictable size on every device. */
val ShareCardWidth = 340.dp

/** Turns a "2026-07" key into "July 2026" for the report header. */
fun formatMonthLabel(raw: String): String {
    return try {
        LocalDate.parse("$raw-01")
            .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    } catch (e: Exception) {
        raw
    }
}

private fun fullDate(raw: String): String {
    return try {
        formatFullDate(LocalDate.parse(raw))
    } catch (e: Exception) {
        raw
    }
}

@Composable
private fun ShareCardHeader(title: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        Text(
            text = "Finance Tracker",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ShareCardFooter() {

    Text(
        text = "Shared from Finance Tracker · ${formatFullDate(LocalDate.now())}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun ExpenseShareCard(
    expense: Expense,
    addedByLabel: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .width(ShareCardWidth)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {

        ShareCardHeader("Expense")

        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 8.dp
            )
        ) {

            Text(
                text = expense.description.ifBlank { expense.category },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = formatMoneyFull(expense.amount),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            HorizontalDivider()

            Column(modifier = Modifier.padding(top = 10.dp)) {

                DetailRow("Category", expense.category)
                DetailRow("Payment", expense.paymentMethod)

                expense.cardName
                    ?.takeIf { it.isNotBlank() }
                    ?.let { DetailRow("Card", it) }

                DetailRow("Date", fullDate(expense.date))
                DetailRow("Time", formatExpenseTime(expense.time))

                if (addedByLabel.isNotBlank()) {
                    DetailRow("Added by", addedByLabel)
                }

                if (expense.isRecurring) {
                    DetailRow("Recurring", "Yes")
                }

                if (expense.notes.isNotBlank()) {
                    DetailRow("Notes", expense.notes)
                }
            }
        }

        ShareCardFooter()
    }
}

@Composable
fun MonthlyReportShareCard(
    monthLabel: String,
    total: Double,
    expenseCount: Int,
    categoryTotals: List<Pair<String, Double>>,
    memberTotals: List<Pair<String, Double>> = emptyList(),
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .width(ShareCardWidth)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {

        ShareCardHeader(monthLabel)

        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 14.dp,
                bottom = 8.dp
            )
        ) {

            Text(
                text = "Total spent",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {

                Text(
                    text = formatMoneyFull(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "  across $expenseCount expense${if (expenseCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            HorizontalDivider()

            Text(
                text = "By category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )

            CategoryBreakdownChart(
                categoryTotals = categoryTotals,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (memberTotals.isNotEmpty()) {

                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

                Text(
                    text = "By member",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )

                memberTotals.forEach { (member, amount) ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {

                        Text(
                            text = member,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = formatMoneyFull(amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        ShareCardFooter()
    }
}

/** Beyond this the card gets too tall to read comfortably in a chat app. */
private const val MAX_SHARED_ROWS = 12

@Composable
fun ExpenseSummaryShareCard(
    title: String,
    subtitle: String,
    expenses: List<Expense>,
    total: Double,
    modifier: Modifier = Modifier,
    addedByLabel: (Expense) -> String = { "" }
) {

    val shown = expenses.take(MAX_SHARED_ROWS)
    val hidden = expenses.size - shown.size

    Column(
        modifier = modifier
            .width(ShareCardWidth)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {

        ShareCardHeader(title)

        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 14.dp,
                bottom = 8.dp
            )
        ) {

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {

                Text(
                    text = formatMoneyFull(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "  ${expenses.size} expense${if (expenses.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            HorizontalDivider()

            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                shown.forEach { expense ->

                    Row(modifier = Modifier.fillMaxWidth()) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(
                                text = expense.description.ifBlank { expense.category },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val payment = expense.cardName
                                ?.takeIf { it.isNotBlank() }
                                ?.let { "${expense.paymentMethod} · $it" }
                                ?: expense.paymentMethod

                            Text(
                                text = "${expense.category} · $payment",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "${formatExpenseDate(expense.date)} · ${formatExpenseTime(expense.time)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val who = addedByLabel(expense)

                            if (who.isNotBlank()) {
                                Text(
                                    text = "by $who",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = formatMoneyFull(expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (hidden > 0) {
                    Text(
                        text = "+ $hidden more expense${if (hidden == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ShareCardFooter()
    }
}
