package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.screens.DailyLogScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SocialAndSettingsScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.screens.WearableScreen
import com.example.ui.theme.DietGreenPrimary
import com.example.ui.theme.DietTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTrackerApp(
    viewModel: DietViewModel = viewModel()
) {
    val userGoals by viewModel.userGoals.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (userGoals.darkModeSetting) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    DietTrackerTheme(darkTheme = isDark) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.diet_tracker_icon_1790991462456),
                                    contentDescription = "Diet Tracker Icon",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Diet Tracker",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Privacy indicator badge
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = DietGreenPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AES-256",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Quick Weight Unit Toggle [ kg | lb ]
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleWeightUnit() }
                                .testTag("topbar_unit_toggle")
                        ) {
                            Text(
                                text = if (userGoals.useLbs) "Unit: lb" else "Unit: kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        // Quick Dark Mode toggle icon button
                        IconButton(
                            onClick = {
                                val nextMode = if (isDark) "LIGHT" else "DARK"
                                viewModel.updateDarkMode(nextMode)
                            },
                            modifier = Modifier.testTag("quick_dark_mode_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.SCANNER,
                        onClick = { viewModel.selectTab(AppTab.SCANNER) },
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Scanner") },
                        label = { Text("Scan", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_scanner")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.DAILY_LOG,
                        onClick = { viewModel.selectTab(AppTab.DAILY_LOG) },
                        icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Daily Log") },
                        label = { Text("Log", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_log")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.TRENDS,
                        onClick = { viewModel.selectTab(AppTab.TRENDS) },
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trends") },
                        label = { Text("Trends", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_trends")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.WEARABLE,
                        onClick = { viewModel.selectTab(AppTab.WEARABLE) },
                        icon = { Icon(Icons.Default.Watch, contentDescription = "Wearables") },
                        label = { Text("Wearable", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_wearable")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.SOCIAL,
                        onClick = { viewModel.selectTab(AppTab.SOCIAL) },
                        icon = { Icon(Icons.Default.Share, contentDescription = "Social") },
                        label = { Text("Social", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_social")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentTab) {
                    AppTab.SCANNER -> ScannerScreen(viewModel = viewModel)
                    AppTab.DAILY_LOG -> DailyLogScreen(viewModel = viewModel)
                    AppTab.TRENDS -> TrendsScreen(viewModel = viewModel)
                    AppTab.WEARABLE -> WearableScreen(viewModel = viewModel)
                    AppTab.SOCIAL -> SocialAndSettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
