package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MealRecord
import com.example.model.UserMacroGoals
import com.example.ui.DailyProgressSummary
import com.example.ui.theme.DietGreenPrimary
import com.example.ui.theme.MacroCalorieColor
import com.example.ui.theme.MacroGreenGood
import com.example.ui.theme.MacroRedBad
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class DayCalorieStat(
    val dayName: String,
    val dateStr: String,
    val dayTimestamp: Long,
    val targetCalories: Int,
    val consumedCalories: Int,
    val delta: Int, // consumed - target
    val isHeldBelow: Boolean,
    val isToday: Boolean
)

@Composable
fun CalorieBudgetWeeklyDashboard(
    meals: List<MealRecord>,
    todaySummary: DailyProgressSummary,
    userGoals: UserMacroGoals,
    modifier: Modifier = Modifier
) {
    // 1. Real-Time Daily Countdown Computation
    val targetCalories = todaySummary.targetCalories.coerceAtLeast(1)
    val consumedCalories = todaySummary.totalCalories
    val remainingCalories = targetCalories - consumedCalories
    val isUnderBudget = remainingCalories >= 0
    val progressRatio = (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1.5f)

    val animatedProgress by animateFloatAsState(
        targetValue = (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "countdown_progress"
    )

    // 2. Weekly 7-Day Calorie Goal Adherence Computation
    val weeklyStats = remember(meals, userGoals, targetCalories) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val list = mutableListOf<DayCalorieStat>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Compute past 6 days + today = 7 days
        for (i in 6 downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + 24L * 60L * 60L * 1000L - 1L

            val dayMeals = meals.filter { it.timestamp in startOfDay..endOfDay }
            val dayConsumed = dayMeals.sumOf { it.calories }
            val dayTarget = if (i == 0) targetCalories else userGoals.baseCalorieTarget
            val delta = dayConsumed - dayTarget

            list.add(
                DayCalorieStat(
                    dayName = if (i == 0) "Today" else dayFormat.format(Date(startOfDay)),
                    dateStr = dateFormat.format(Date(startOfDay)),
                    dayTimestamp = startOfDay,
                    targetCalories = dayTarget,
                    consumedCalories = dayConsumed,
                    delta = delta,
                    isHeldBelow = delta <= 0,
                    isToday = (i == 0)
                )
            )
        }
        list
    }

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    val daysHeldBelow = weeklyStats.count { it.isHeldBelow }
    val adherencePercent = ((daysHeldBelow.toFloat() / weeklyStats.size) * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calorie_countdown_weekly_dashboard"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Real-Time Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = DietGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Calorie Budget & Countdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time daily countdown & 7-day goal adherence",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Pulse Badge
                Surface(
                    color = DietGreenPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(DietGreenPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DietGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // PART 1: Real-Time Countdown Circular Ring
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Countdown Gauge
                Box(
                    modifier = Modifier.size(105.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(105.dp)) {
                        val strokeWidth = 9.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Track background
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.35f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress fill
                        val sweep = animatedProgress * 360f
                        val progressColor = if (isUnderBudget) MacroGreenGood else MacroRedBad
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${abs(remainingCalories)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isUnderBudget) MacroGreenGood else MacroRedBad
                        )
                        Text(
                            text = if (isUnderBudget) "kcal left" else "kcal over",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Countdown Status & Quick Breakdown
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isUnderBudget) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isUnderBudget) MacroGreenGood else MacroRedBad,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isUnderBudget) "On Target Budget" else "Exceeded Daily Goal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isUnderBudget) MacroGreenGood else MacroRedBad
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isUnderBudget) {
                            "You have $remainingCalories kcal remaining to stay under your target today."
                        } else {
                            "Consumed ${abs(remainingCalories)} kcal above your calibrated target."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target vs Consumed badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Goal Target", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$targetCalories kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Consumed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$consumedCalories kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MacroCalorieColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==============================================================
            // PART 2: 7-Day Weekly Calorie Goal Adherence Bar Graph
            // ==============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Goal Variance (7 Days)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Days held below vs above goal setting",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 7-day adherence chip
                Surface(
                    color = if (adherencePercent >= 60) MacroGreenGood.copy(alpha = 0.12f) else MacroRedBad.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$daysHeldBelow/7 Days In Goal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (adherencePercent >= 60) MacroGreenGood else MacroRedBad,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MacroGreenGood, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Held Below Goal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MacroRedBad, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Above Goal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp, 2.dp).background(Color.Gray))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Goal Baseline", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Bar Graph with Goal Setting Baseline
            val maxDeltaAbs = remember(weeklyStats) {
                weeklyStats.maxOfOrNull { abs(it.delta) }?.coerceAtLeast(400) ?: 500
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val barWidth = 22.dp.toPx()
                    val baselineY = h * 0.52f // Center baseline representing Goal Setting
                    val maxBarHeight = h * 0.40f

                    // Draw Goal Setting Baseline (Dashed)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.55f),
                        start = Offset(0f, baselineY),
                        end = Offset(w, baselineY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    val count = weeklyStats.size
                    val spacing = (w - (count * barWidth)) / (count + 1)

                    weeklyStats.forEachIndexed { idx, stat ->
                        val barX = spacing + idx * (barWidth + spacing)
                        val deltaRatio = (abs(stat.delta).toFloat() / maxDeltaAbs.toFloat()).coerceIn(0.12f, 1f)
                        val barHeight = deltaRatio * maxBarHeight

                        val barColor = if (stat.isHeldBelow) MacroGreenGood else MacroRedBad

                        if (stat.isHeldBelow) {
                            // Below goal: drawn downward from baseline (budget preserved)
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(barX, baselineY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        } else {
                            // Above goal: drawn upward from baseline (budget exceeded)
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(barX, baselineY - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }

                // Interactive Day Labels & Click Targets
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyStats.forEachIndexed { index, stat ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedDayIndex = if (selectedDayIndex == index) null else index
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stat.dayName,
                                fontSize = 10.sp,
                                fontWeight = if (stat.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (stat.isToday) DietGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Interactive Day Detail Card (Shows when a day bar is selected or today by default)
            val activeStat = selectedDayIndex?.let { weeklyStats.getOrNull(it) } ?: weeklyStats.lastOrNull()
            if (activeStat != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${activeStat.dayName} (${activeStat.dateStr})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                if (activeStat.isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = DietGreenPrimary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Consumed: ${activeStat.consumedCalories} kcal • Goal: ${activeStat.targetCalories} kcal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (activeStat.isHeldBelow) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (activeStat.isHeldBelow) MacroGreenGood else MacroRedBad,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeStat.isHeldBelow) {
                                    "${abs(activeStat.delta)} kcal below"
                                } else {
                                    "+${activeStat.delta} kcal above"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (activeStat.isHeldBelow) MacroGreenGood else MacroRedBad
                            )
                        }
                    }
                }
            }
        }
    }
}
