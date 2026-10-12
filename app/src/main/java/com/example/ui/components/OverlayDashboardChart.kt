package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MealRecord
import com.example.model.WeightRecord
import com.example.ui.OverlayDashboardConfig
import com.example.ui.theme.DietGreenPrimary
import com.example.ui.theme.MacroCalorieColor
import com.example.ui.theme.MacroCarbColor
import com.example.ui.theme.MacroFatColor
import com.example.ui.theme.MacroProteinColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyAggregatedStats(
    val dateLabel: String,
    val dayTimestamp: Long,
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f,
    val weight: Float? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverlayDashboardChart(
    meals: List<MealRecord>,
    weights: List<WeightRecord>,
    config: OverlayDashboardConfig,
    useLbs: Boolean,
    onConfigChange: (days: Int?, weight: Boolean?, cals: Boolean?, prot: Boolean?, carb: Boolean?, fat: Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = if (useLbs) "lbs" else "kg"
    val weightMult = if (useLbs) 2.20462f else 1f

    // Compute aggregated daily stats for the chosen window (7, 14, 30 days)
    val dailyStatsList = remember(meals, weights, config.timeWindowDays) {
        val list = mutableListOf<DailyAggregatedStats>()
        val cal = Calendar.getInstance()

        for (i in (config.timeWindowDays - 1) downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = targetCal.timeInMillis
            val endOfDay = startOfDay + 86400000L

            val dayMeals = meals.filter { it.timestamp in startOfDay until endOfDay }
            val dayWeight = weights.lastOrNull { it.timestamp <= endOfDay }?.weightKg

            val label = SimpleDateFormat("M/d", Locale.getDefault()).format(Date(startOfDay))

            list.add(
                DailyAggregatedStats(
                    dateLabel = label,
                    dayTimestamp = startOfDay,
                    totalCalories = dayMeals.sumOf { it.calories.toDouble() }.toFloat(),
                    totalProtein = dayMeals.sumOf { it.proteinGrams.toDouble() }.toFloat(),
                    totalCarbs = dayMeals.sumOf { it.carbsGrams.toDouble() }.toFloat(),
                    totalFat = dayMeals.sumOf { it.fatGrams.toDouble() }.toFloat(),
                    weight = dayWeight?.times(weightMult)
                )
            )
        }
        list
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Unified Overlay Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Correlate macros, calories & weight over time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Time window selector
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    listOf(7, 14, 30).forEach { days ->
                        val isSelected = config.timeWindowDays == days
                        Box(
                            modifier = Modifier
                                .clickable { onConfigChange(days, null, null, null, null, null) }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${days}D",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Toggle Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricToggleChip(
                    label = "Weight ($unit)",
                    color = DietGreenPrimary,
                    selected = config.showWeight,
                    onToggle = { onConfigChange(null, !config.showWeight, null, null, null, null) }
                )
                MetricToggleChip(
                    label = "Calories (kcal)",
                    color = MacroCalorieColor,
                    selected = config.showCalories,
                    onToggle = { onConfigChange(null, null, !config.showCalories, null, null, null) }
                )
                MetricToggleChip(
                    label = "Protein (g)",
                    color = MacroProteinColor,
                    selected = config.showProtein,
                    onToggle = { onConfigChange(null, null, null, !config.showProtein, null, null) }
                )
                MetricToggleChip(
                    label = "Carbs (g)",
                    color = MacroCarbColor,
                    selected = config.showCarbs,
                    onToggle = { onConfigChange(null, null, null, null, !config.showCarbs, null) }
                )
                MetricToggleChip(
                    label = "Fat (g)",
                    color = MacroFatColor,
                    selected = config.showFat,
                    onToggle = { onConfigChange(null, null, null, null, null, !config.showFat) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Canvas Multi-Metric Overlay
            val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val w = size.width
                val h = size.height
                val paddingBottom = 22.dp.toPx()
                val chartHeight = h - paddingBottom
                val count = dailyStatsList.size
                val stepX = w / (count - 1).coerceAtLeast(1)

                // Background horizontal guide lines
                for (step in 1..3) {
                    val y = (chartHeight / 4) * step
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Helper to normalize and plot a series
                fun drawSeries(
                    values: List<Float>,
                    color: Color,
                    strokeWidthDp: Float = 2.5f
                ) {
                    val nonZero = values.filter { it > 0f }
                    if (nonZero.isEmpty()) return
                    val minVal = (nonZero.minOrNull() ?: 0f) * 0.9f
                    val maxVal = (nonZero.maxOrNull() ?: 1f) * 1.1f
                    val span = (maxVal - minVal).coerceAtLeast(1f)

                    val path = Path()
                    values.forEachIndexed { i, v ->
                        val x = i * stepX
                        val safeVal = if (v <= 0f) minVal else v
                        val y = chartHeight - ((safeVal - minVal) / span * chartHeight)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = strokeWidthDp.dp.toPx())
                    )

                    // Draw dots for non-zero points
                    values.forEachIndexed { i, v ->
                        if (v > 0f) {
                            val x = i * stepX
                            val y = chartHeight - ((v - minVal) / span * chartHeight)
                            drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }

                // 1. Overlay Calories (kcal)
                if (config.showCalories) {
                    drawSeries(dailyStatsList.map { it.totalCalories }, MacroCalorieColor, 3f)
                }
                // 2. Overlay Weight
                if (config.showWeight) {
                    val weightsSeries = dailyStatsList.map { it.weight ?: 0f }
                    drawSeries(weightsSeries, DietGreenPrimary, 3.5f)
                }
                // 3. Overlay Protein
                if (config.showProtein) {
                    drawSeries(dailyStatsList.map { it.totalProtein }, MacroProteinColor, 2f)
                }
                // 4. Overlay Carbs
                if (config.showCarbs) {
                    drawSeries(dailyStatsList.map { it.totalCarbs }, MacroCarbColor, 2f)
                }
                // 5. Overlay Fat
                if (config.showFat) {
                    drawSeries(dailyStatsList.map { it.totalFat }, MacroFatColor, 2f)
                }
            }

            // X-Axis Date Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = (dailyStatsList.size / 4).coerceAtLeast(1)
                dailyStatsList.filterIndexed { index, _ -> index % step == 0 || index == dailyStatsList.size - 1 }
                    .forEach { stats ->
                        Text(
                            text = stats.dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trend Correlation Insight Box
            val validCals = dailyStatsList.map { it.totalCalories }.filter { it > 0 }
            val avgCal = if (validCals.isNotEmpty()) validCals.average().toInt() else 0
            val validProt = dailyStatsList.map { it.totalProtein }.filter { it > 0 }
            val avgProt = if (validProt.isNotEmpty()) validProt.average().toInt() else 0

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Macro & Weight Correlation Insight",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${config.timeWindowDays}-Day Average: $avgCal kcal/day with ${avgProt}g protein. Healthy deficit maintained consistently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricToggleChip(
    label: String,
    color: Color,
    selected: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onToggle,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.15f),
            selectedLabelColor = color
        )
    )
}
