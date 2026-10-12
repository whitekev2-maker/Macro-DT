package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeightRecord
import com.example.ui.DietViewModel
import com.example.ui.components.CalorieBudgetWeeklyDashboard
import com.example.ui.components.OverlayDashboardChart
import com.example.ui.components.WeightTrendChart
import com.example.ui.theme.DietGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrendsScreen(
    viewModel: DietViewModel,
    modifier: Modifier = Modifier
) {
    val meals by viewModel.allMeals.collectAsState()
    val weights by viewModel.allWeightsAsc.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val overlayConfig by viewModel.overlayConfig.collectAsState()
    val todaySummary by viewModel.todaySummary.collectAsState()

    var showAddWeightDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Trends & Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Interactive weight & multi-metric overlays",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons row directly below the title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAddWeightDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("log_weight_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Log Weight",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Unit selector [ kg | lb ]
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("unit_selector_toggle_bar")
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setWeightUnit(false) }
                                    .background(if (!userGoals.useLbs) DietGreenPrimary else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("unit_selector_kg")
                            ) {
                                Text(
                                    text = "kg",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = if (!userGoals.useLbs) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setWeightUnit(true) }
                                    .background(if (userGoals.useLbs) DietGreenPrimary else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("unit_selector_lb")
                            ) {
                                Text(
                                    text = "lb",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = if (userGoals.useLbs) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Real-Time Calorie Countdown & 7-Day Goal Adherence Dashboard
        item {
            CalorieBudgetWeeklyDashboard(
                meals = meals,
                todaySummary = todaySummary,
                userGoals = userGoals
            )
        }

        // Section 2: Weight Trend Chart
        item {
            WeightTrendChart(
                weights = weights,
                targetWeightKg = userGoals.targetWeightKg,
                useLbs = userGoals.useLbs
            )
        }

        // Section 3: Customizable Multi-Metric Overlay Dashboard
        item {
            OverlayDashboardChart(
                meals = meals,
                weights = weights,
                config = overlayConfig,
                useLbs = userGoals.useLbs,
                onConfigChange = { days, weight, cals, prot, carb, fat ->
                    viewModel.updateOverlayConfig(days, weight, cals, prot, carb, fat)
                }
            )
        }

        // Section 4: Weight History Log
        item {
            Text(
                text = "Recent Weight Entries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (weights.isEmpty()) {
            item {
                Text(
                    text = "No weight records yet. Tap 'Log Weight' to track your first entry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(weights.reversed()) { record ->
                WeightRecordItem(
                    record = record,
                    useLbs = userGoals.useLbs,
                    onDelete = { viewModel.deleteWeight(record) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddWeightDialog) {
        AddWeightDialog(
            initialUseLbs = userGoals.useLbs,
            currentWeightKg = userGoals.currentWeightKg,
            onDismiss = { showAddWeightDialog = false },
            onSave = { weightVal, isEnteredInLbs, note ->
                if (userGoals.useLbs != isEnteredInLbs) {
                    viewModel.setWeightUnit(isEnteredInLbs)
                }
                viewModel.logWeight(weightVal, isEnteredInLbs, note)
                showAddWeightDialog = false
            }
        )
    }
}

@Composable
fun WeightRecordItem(
    record: WeightRecord,
    useLbs: Boolean,
    onDelete: () -> Unit
) {
    val unit = if (useLbs) "lb" else "kg"
    val mult = if (useLbs) 2.20462f else 1f
    val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${String.format("%.1f", record.weightKg * mult)} $unit",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Weight",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddWeightDialog(
    initialUseLbs: Boolean,
    currentWeightKg: Float,
    onDismiss: () -> Unit,
    onSave: (weightVal: Float, isEnteredInLbs: Boolean, note: String) -> Unit
) {
    var dialogUseLbs by remember { mutableStateOf(initialUseLbs) }
    val mult = if (dialogUseLbs) 2.20462f else 1f
    var weightInput by remember(dialogUseLbs) { mutableStateOf(String.format("%.1f", currentWeightKg * mult)) }
    var noteInput by remember { mutableStateOf("") }
    val unitLabel = if (dialogUseLbs) "lb (pounds)" else "kg (kilograms)"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Log Weight", fontWeight = FontWeight.Bold)

                // Inline unit selector in dialog
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { dialogUseLbs = false }
                                .background(if (!dialogUseLbs) DietGreenPrimary else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("dialog_unit_kg")
                        ) {
                            Text(
                                text = "kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!dialogUseLbs) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { dialogUseLbs = true }
                                .background(if (dialogUseLbs) DietGreenPrimary else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("dialog_unit_lb")
                        ) {
                            Text(
                                text = "lb",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dialogUseLbs) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Record your weight in $unitLabel to update charts and correlations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Weight (${if (dialogUseLbs) "lb" else "kg"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_input_field"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Note (Optional, Encrypted)") },
                    placeholder = { Text("e.g. Morning weigh-in") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawVal = weightInput.toFloatOrNull() ?: (currentWeightKg * mult)
                    onSave(rawVal, dialogUseLbs, noteInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                modifier = Modifier.testTag("save_weight_confirm_button")
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
