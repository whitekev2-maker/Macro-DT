package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeightRecord
import com.example.ui.theme.DietGreenDark
import com.example.ui.theme.DietGreenPrimary
import com.example.ui.theme.MacroProteinColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightTrendChart(
    weights: List<WeightRecord>,
    targetWeightKg: Float,
    useLbs: Boolean,
    modifier: Modifier = Modifier
) {
    val unit = if (useLbs) "lbs" else "kg"
    val multiplier = if (useLbs) 2.20462f else 1f

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weight Progress Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Interactive timeline & goal trajectory",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Target: ${String.format("%.1f", targetWeightKg * multiplier)} $unit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (weights.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log your weight to view trend chart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val displayWeights = weights.map { it.weightKg * multiplier }
                val minWeight = (displayWeights.minOrNull() ?: 60f).coerceAtMost(targetWeightKg * multiplier) - 2f
                val maxWeight = (displayWeights.maxOrNull() ?: 80f) + 2f
                val range = (maxWeight - minWeight).coerceAtLeast(1f)

                val primaryLineColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                val targetLineColor = MacroProteinColor

                // Canvas Chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(weights) {
                            detectTapGestures { tapOffset ->
                                val spacing = size.width / (weights.size - 1).coerceAtLeast(1)
                                val index = ((tapOffset.x + (spacing / 2)) / spacing).toInt()
                                    .coerceIn(0, weights.size - 1)
                                selectedIndex = index
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 24.dp.toPx()
                    val chartHeight = h - paddingBottom
                    val spacing = w / (weights.size - 1).coerceAtLeast(1)

                    // Draw target weight dashed line
                    val targetY = chartHeight - ((targetWeightKg * multiplier - minWeight) / range * chartHeight)
                    if (targetY in 0f..chartHeight) {
                        drawLine(
                            color = targetLineColor.copy(alpha = 0.8f),
                            start = Offset(0f, targetY),
                            end = Offset(w, targetY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    }

                    // Draw baseline grid lines
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, chartHeight),
                        end = Offset(w, chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Build path
                    val path = Path()
                    val points = mutableListOf<Offset>()

                    weights.forEachIndexed { i, record ->
                        val x = if (weights.size == 1) w / 2 else i * spacing
                        val y = chartHeight - ((record.weightKg * multiplier - minWeight) / range * chartHeight)
                        points.add(Offset(x, y))
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            val prev = points[i - 1]
                            val cx = (prev.x + x) / 2
                            path.cubicTo(cx, prev.y, cx, y, x, y)
                        }
                    }

                    // Stroke curve
                    drawPath(
                        path = path,
                        color = primaryLineColor,
                        style = Stroke(width = 3.5.dp.toPx())
                    )

                    // Draw points
                    points.forEachIndexed { i, pt ->
                        val isSelected = (selectedIndex == i)
                        drawCircle(
                            color = if (isSelected) primaryLineColor else Color.White,
                            radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = primaryLineColor,
                            radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                            center = pt,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Scrubber / Selected Point Info
                val activeIndex = selectedIndex ?: (weights.size - 1)
                val activeRecord = weights.getOrNull(activeIndex)
                if (activeRecord != null) {
                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(activeRecord.timestamp))
                    val diffFromTarget = (activeRecord.weightKg - targetWeightKg) * multiplier

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(primaryLineColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${String.format("%.1f", activeRecord.weightKg * multiplier)} $unit (${if (diffFromTarget > 0) "+${String.format("%.1f", diffFromTarget)}" else String.format("%.1f", diffFromTarget)} from goal)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
