package com.grvig.financetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

fun formatMoney(amount: Double): String {
    val rounded = amount.roundToLong()
    return when {
        abs(amount) >= 10_000_000 -> "₹${trimDecimal(amount / 10_000_000)}Cr"
        abs(amount) >= 100_000 -> "₹${trimDecimal(amount / 100_000)}L"
        abs(amount) >= 1_000 -> "₹${trimDecimal(amount / 1_000)}k"
        else -> "₹$rounded"
    }
}

fun formatMoneyFull(amount: Double): String {
    val rounded = amount.roundToLong()
    val sign = if (rounded < 0) "-" else ""
    val digits = abs(rounded).toString()

    if (digits.length <= 3) {
        return "₹$sign$digits"
    }

    val last3 = digits.takeLast(3)
    var rest = digits.dropLast(3)
    val groups = mutableListOf<String>()

    while (rest.length > 2) {
        groups.add(0, rest.takeLast(2))
        rest = rest.dropLast(2)
    }
    if (rest.isNotEmpty()) {
        groups.add(0, rest)
    }

    return "₹$sign${groups.joinToString(",")},$last3"
}

private fun trimDecimal(value: Double): String {
    val oneDp = (value * 10).roundToLong() / 10.0
    return if (oneDp % 1.0 == 0.0) {
        oneDp.toLong().toString()
    } else {
        oneDp.toString()
    }
}

private fun niceCeiling(value: Double): Double {
    if (value <= 0) return 1.0
    val exponent = kotlin.math.floor(kotlin.math.log10(value))
    val magnitude = 10.0.pow(exponent)
    val normalized = value / magnitude
    val niceNormalized = when {
        normalized <= 1.0 -> 1.0
        normalized <= 2.0 -> 2.0
        normalized <= 2.5 -> 2.5
        normalized <= 5.0 -> 5.0
        else -> 10.0
    }
    return niceNormalized * magnitude
}

@Composable
fun StatTile(
    label: String,
    value: String,
    onContainer: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = onContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = onContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BudgetProgressBar(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val fraction = (percent / 100f).coerceIn(0f, 1f)
    val trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
    val fillColor = when {
        percent >= 100 -> MaterialTheme.colorScheme.error
        percent >= 80 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
    ) {
        val radius = size.height / 2f

        drawRoundRect(
            color = trackColor,
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )

        if (fraction > 0f) {
            drawRoundRect(
                color = fillColor,
                size = Size(
                    (size.width * fraction).coerceAtLeast(size.height),
                    size.height
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
            )
        }
    }
}

@Composable
fun CategoryBreakdownChart(
    categoryTotals: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    onCategoryClick: ((String) -> Unit)? = null
) {

    val total = categoryTotals.sumOf { it.second }
    val maxAmount = categoryTotals.maxOfOrNull { it.second } ?: 0.0

    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {

        categoryTotals.forEachIndexed { index, (category, amount) ->

            val fraction = if (maxAmount > 0) {
                (amount / maxAmount).toFloat()
            } else {
                0f
            }

            val percent = if (total > 0) {
                ((amount / total) * 100).roundToLong()
            } else {
                0L
            }

            val barColor = palette[index % palette.size]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (onCategoryClick != null) {
                            Modifier.clickable {
                                onCategoryClick(category)
                            }
                        } else {
                            Modifier
                        }
                    )
                    .padding(vertical = 6.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                    )

                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                    )

                    Text(
                        text = formatMoneyFull(amount),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "  $percent%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(10.dp)
                ) {
                    val radius = size.height / 2f

                    drawRoundRect(
                        color = trackColor,
                        size = Size(size.width, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                    )

                    if (fraction > 0f) {
                        drawRoundRect(
                            color = barColor,
                            size = Size(
                                (size.width * fraction).coerceAtLeast(size.height),
                                size.height
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendChart(
    monthlyTotals: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {

    val rawMax = monthlyTotals.maxOfOrNull { it.second } ?: 0.0
    val axisMax = niceCeiling(rawMax)

    val barColor = MaterialTheme.colorScheme.primary
    val mutedBarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxIndex = monthlyTotals.indexOfFirst { it.second == rawMax && rawMax > 0 }

    Column(modifier = modifier.fillMaxWidth()) {

        Row(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier
                    .height(140.dp)
                    .width(52.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatMoney(axisMax),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    text = formatMoney(axisMax / 2),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    text = "₹0",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .padding(start = 8.dp)
            ) {

                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val lines = 2
                    for (i in 0..lines) {
                        val y = size.height * (i.toFloat() / lines)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {

                    monthlyTotals.forEachIndexed { index, (_, amount) ->

                        val fraction = if (axisMax > 0) {
                            (amount / axisMax).toFloat()
                        } else {
                            0f
                        }

                        Canvas(
                            modifier = Modifier
                                .width(26.dp)
                                .height(140.dp)
                        ) {
                            val barHeight = size.height * fraction
                            if (barHeight > 0f) {
                                val radius = 4.dp.toPx()
                                drawRoundRect(
                                    color = if (index == maxIndex) barColor else mutedBarColor,
                                    topLeft = Offset(0f, size.height - barHeight),
                                    size = Size(size.width, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            monthlyTotals.forEach { (label, _) ->
                Box(
                    modifier = Modifier.width(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
        }
    }
}
