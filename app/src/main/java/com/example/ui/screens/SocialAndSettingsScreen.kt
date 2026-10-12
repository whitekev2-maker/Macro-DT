package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DietViewModel
import com.example.ui.theme.DietGreenPrimary
import com.example.ui.theme.MacroCalorieColor
import com.example.ui.theme.MacroProteinColor

data class FriendAchievement(
    val id: String,
    val name: String,
    val avatarInitials: String,
    val achievementText: String,
    val timeAgo: String,
    var cheerCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialAndSettingsScreen(
    viewModel: DietViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userGoals by viewModel.userGoals.collectAsState()
    val todaySummary by viewModel.todaySummary.collectAsState()
    val latestWearable by viewModel.latestWearable.collectAsState()

    var showGoalsEditor by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    // Friend achievements state
    val cheersMap = remember {
        mutableStateMapOf(
            "1" to 14,
            "2" to 22,
            "3" to 8
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Social & Preferences",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Share daily achievements & configure privacy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Share Daily Achievements Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily Achievement Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Achievement Preview Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🥗 Diet Tracker • Today's Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Calories: ${todaySummary.totalCalories} / ${todaySummary.targetCalories} kcal\n" +
                                    "• Protein: ${todaySummary.totalProtein.toInt()}g / ${todaySummary.targetProtein.toInt()}g (${if (todaySummary.totalProtein >= todaySummary.targetProtein) "Goal Hit! 🎯" else "On track 🚀"})\n" +
                                    "• Steps Synced: ${String.format("%,d", latestWearable?.steps ?: 8450)} steps 👟\n" +
                                    "• 7-Day Healthy Macro Streak 🔥",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val shareText = "🥗 Diet Tracker Achievement Today!\n" +
                                "Intake: ${todaySummary.totalCalories} kcal (${todaySummary.balancedMealsCount} balanced meals)\n" +
                                "Protein: ${todaySummary.totalProtein.toInt()}g / ${todaySummary.targetProtein.toInt()}g\n" +
                                "Activity: ${String.format("%,d", latestWearable?.steps ?: 8450)} steps synced\n" +
                                "7-Day Healthy Eating Streak 🔥 • Tracked privately with Diet Tracker!"

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Achievement with Friends")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_achievement_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Achievements with Friends", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 2: Friends Nutrition Circle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Friends Nutrition Circle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Encourage friends and celebrate healthy habits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Friend 1
                FriendMilestoneItem(
                    name = "Alex Morgan",
                    initials = "AM",
                    achievement = "Hit 145g Protein target with Grilled Salmon 🥗",
                    timeAgo = "1h ago",
                    cheers = cheersMap["1"] ?: 14,
                    onCheer = {
                        val curr = cheersMap["1"] ?: 14
                        cheersMap["1"] = curr + 1
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Friend 2
                FriendMilestoneItem(
                    name = "Sarah Jenkins",
                    initials = "SJ",
                    achievement = "Logged 10,500 steps + 200 kcal deficit 🏃‍♀️",
                    timeAgo = "3h ago",
                    cheers = cheersMap["2"] ?: 22,
                    onCheer = {
                        val curr = cheersMap["2"] ?: 22
                        cheersMap["2"] = curr + 1
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Friend 3
                FriendMilestoneItem(
                    name = "Marcus Vance",
                    initials = "MV",
                    achievement = "Reached 14-day balanced macro streak! 🔥",
                    timeAgo = "5h ago",
                    cheers = cheersMap["3"] ?: 8,
                    onCheer = {
                        val curr = cheersMap["3"] ?: 8
                        cheersMap["3"] = curr + 1
                    }
                )
            }
        }

        // Section 3: Dark Mode Option for Late-Night Logging
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dark Mode (Late-Night Food Logging)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calming forest green theme designed for low-light evening logging",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SYSTEM" to "System",
                        "LIGHT" to "Calming Light",
                        "DARK" to "Forest Dark"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = userGoals.darkModeSetting == mode,
                            onClick = { viewModel.updateDarkMode(mode) },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_chip_$mode")
                        )
                    }
                }
            }
        }

        // Section 4: Privacy & Local Data Encryption Vault
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DietGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy & Local Encryption Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DietGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AES-256-GCM Hardware Encrypted",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All user records, meal photos, personal notes, and biometric telemetry are encrypted and stored exclusively on your device. Zero external data profiling.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showPrivacyPolicyDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_privacy_policy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View Full Privacy Policy",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section 5: Target Macro Goals Editor
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Macro & Weight Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Edit button as a line of its own under "Daily Macro & Weight Targets"
                OutlinedButton(
                    onClick = { showGoalsEditor = !showGoalsEditor },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_macro_targets_button")
                ) {
                    Icon(
                        imageVector = if (showGoalsEditor) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showGoalsEditor) "Close" else "Edit",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showGoalsEditor) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroGoalsEditorForm(
                        currentGoals = userGoals,
                        onSave = { c, p, carb, f, w, lbs ->
                            viewModel.updateGoals(c, p, carb, f, w, lbs)
                            showGoalsEditor = false
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    val targetWeightStr = if (userGoals.useLbs) {
                        String.format("%.1f lb", userGoals.targetWeightKg * 2.20462f)
                    } else {
                        String.format("%.1f kg", userGoals.targetWeightKg)
                    }
                    Text(
                        text = "• Calories: ${userGoals.baseCalorieTarget} kcal/day\n" +
                                "• Protein: ${userGoals.targetProteinGrams.toInt()}g • Carbs: ${userGoals.targetCarbsGrams.toInt()}g • Fat: ${userGoals.targetFatGrams.toInt()}g\n" +
                                "• Target Weight: $targetWeightStr (${if (userGoals.useLbs) "Pounds / lb" else "Kilograms / kg"})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicyDialog = false })
    }
}

@Composable
fun FriendMilestoneItem(
    name: String,
    initials: String,
    achievement: String,
    timeAgo: String,
    cheers: Int,
    onCheer: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeAgo,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = achievement,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onCheer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Cheer",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$cheers",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MacroGoalsEditorForm(
    currentGoals: com.example.model.UserMacroGoals,
    onSave: (Int, Float, Float, Float, Float, Boolean) -> Unit
) {
    var cals by remember { mutableStateOf(currentGoals.baseCalorieTarget.toString()) }
    var protein by remember { mutableStateOf(currentGoals.targetProteinGrams.toInt().toString()) }
    var carbs by remember { mutableStateOf(currentGoals.targetCarbsGrams.toInt().toString()) }
    var fat by remember { mutableStateOf(currentGoals.targetFatGrams.toInt().toString()) }
    var useLbs by remember { mutableStateOf(currentGoals.useLbs) }
    var targetWeight by remember(useLbs) {
        mutableStateOf(
            if (useLbs) String.format("%.1f", currentGoals.targetWeightKg * 2.20462f)
            else String.format("%.1f", currentGoals.targetWeightKg)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = cals,
            onValueChange = { cals = it },
            label = { Text("Base Calories Target (kcal)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("Carbs (g)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fat (g)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        OutlinedTextField(
            value = targetWeight,
            onValueChange = { targetWeight = it },
            label = { Text("Target Goal Weight (${if (useLbs) "lb" else "kg"})") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Weight Measurement Unit: ${if (useLbs) "Pounds (lb)" else "Kilograms (kg)"}", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = useLbs, onCheckedChange = { useLbs = it })
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                val c = cals.toIntOrNull() ?: currentGoals.baseCalorieTarget
                val p = protein.toFloatOrNull() ?: currentGoals.targetProteinGrams
                val cb = carbs.toFloatOrNull() ?: currentGoals.targetCarbsGrams
                val f = fat.toFloatOrNull() ?: currentGoals.targetFatGrams
                val rawW = targetWeight.toFloatOrNull() ?: currentGoals.targetWeightKg
                val finalWeightKg = if (useLbs) rawW / 2.20462f else rawW
                onSave(c, p, cb, f, finalWeightKg, useLbs)
            },
            colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Updated Targets")
        }
    }
}

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DietGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = DietGreenPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Effective Date: October 12, 2026\nApp ID: com.aistudio.diettracker.nvkqwz",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DietGreenPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Text(
                    text = "1. Local-First Architecture",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "All meal history, calorie metrics, custom notes, and body weight logs are stored locally on your device in an encrypted database. Zero external data profiling.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "2. Hardware AES-256-GCM Encryption",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your private notes and sensitive log records are secured using Android KeyStore-backed AES-256-GCM authenticated encryption.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "3. Permissions & Media",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "• Camera (android.permission.CAMERA): Used only when photographing food in the scanner for immediate macro detection. Photos are never used for biometric facial profiling.\n" +
                            "• Internet (android.permission.INTERNET): Used to query Google Gemini AI and Google Search Grounding for real-world nutritional information.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "4. Third-Party Services",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "When analyzing food photos or querying nutrition facts, data is processed securely via Google Gemini Generative AI services. No ad networks or cross-app tracking SDKs are included.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "5. Data Control & Deletion",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "You can edit or delete any meal or weight entry at any time. Uninstalling the app permanently purges all local databases and encryption keys.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "6. Developer Contact",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "For inquiries regarding this privacy policy, contact whitekev2@gmail.com.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DietGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close")
            }
        }
    )
}
