package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StatusBadge
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ModelInfoScreen
import com.example.ui.screens.MonitoringScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.DeepCockpit
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = settings.isDarkTheme) {
                DriverFatigueApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverFatigueApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val isMonitoringActive by viewModel.isMonitoringActive.collectAsStateWithLifecycle()
    val isCameraStreaming by viewModel.isCameraStreaming.collectAsStateWithLifecycle()
    val totalSeconds by viewModel.totalMonitoringSeconds.collectAsStateWithLifecycle()
    val driverProfile by viewModel.driverProfile.collectAsStateWithLifecycle()
    val historyEvents by viewModel.historyEvents.collectAsStateWithLifecycle()
    val currentScenario by viewModel.currentScenario.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { email, name ->
                viewModel.login(email, name)
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepCockpit,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Drawer Header
                    Surface(
                        color = CardSlate,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "COLLEGE CAPSTONE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Driver Fatigue Guard",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = driverProfile.email,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StatusBadge(
                                label = if (isMonitoringActive) "MONITORING ACTIVE" else "STANDBY",
                                statusColor = if (isMonitoringActive) SafetyGreen else WarningAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "PROJECT NAVIGATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    // Navigation Drawer items
                    val allScreens = listOf(
                        Triple(AppScreen.DASHBOARD, Icons.Default.Dashboard, "System Overview"),
                        Triple(AppScreen.MONITORING, Icons.Default.CameraAlt, "Real-Time Camera"),
                        Triple(AppScreen.HISTORY, Icons.Default.History, "Audit Log"),
                        Triple(AppScreen.REPORTS, Icons.Default.Assessment, "Analytics & Trends"),
                        Triple(AppScreen.AI_ASSISTANT, Icons.Default.Psychology, "Safety AI Assistant"),
                        Triple(AppScreen.MODEL_INFO, Icons.Default.Science, "OpenCV & Keras Specs"),
                        Triple(AppScreen.SETTINGS, Icons.Default.Settings, "Config & Calibration")
                    )

                    allScreens.forEach { (screen, icon, subtitle) ->
                        val isSelected = currentScreen == screen
                        NavigationDrawerItem(
                            label = {
                                Column {
                                    Text(
                                        text = screen.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 10.sp,
                                        color = if (isSelected) TechCyan.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) TechCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                viewModel.navigateTo(screen)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = TechCyan.copy(alpha = 0.15f),
                                selectedTextColor = TechCyan,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag("nav_drawer_${screen.name.lowercase()}")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Logout in Drawer
                    NavigationDrawerItem(
                        label = { Text("Log Out", fontSize = 13.sp, color = AlertRed, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AlertRed) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.logout()
                        },
                        modifier = Modifier.testTag("nav_drawer_logout")
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentScreen.label,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Real-Time Driver Fatigue Detection",
                                fontSize = 11.sp,
                                color = TechCyan
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = TechCyan
                            )
                        }
                    },
                    actions = {
                        if (telemetry.isFatigueAlert) {
                            Surface(
                                color = AlertRed.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "ALARM",
                                    color = AlertRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("top_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DeepCockpit,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                // Bottom Navigation Bar for primary rapid workflow
                NavigationBar(
                    containerColor = CardSlate,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val bottomItems = listOf(
                        Pair(AppScreen.DASHBOARD, Icons.Default.Dashboard),
                        Pair(AppScreen.MONITORING, Icons.Default.CameraAlt),
                        Pair(AppScreen.HISTORY, Icons.Default.History),
                        Pair(AppScreen.REPORTS, Icons.Default.Assessment),
                        Pair(AppScreen.AI_ASSISTANT, Icons.Default.Psychology)
                    )

                    bottomItems.forEach { (screen, icon) ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            icon = {
                                if (screen == AppScreen.MONITORING && telemetry.isFatigueAlert) {
                                    BadgedBox(badge = { Badge { Text("!") } }) {
                                        Icon(imageVector = icon, contentDescription = screen.label)
                                    }
                                } else {
                                    Icon(imageVector = icon, contentDescription = screen.label)
                                }
                            },
                            label = { Text(screen.label.split(" ").first(), fontSize = 10.sp) },
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DeepCockpit,
                                selectedTextColor = TechCyan,
                                indicatorColor = TechCyan,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("bottom_nav_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = { email, name ->
                                viewModel.login(email, name)
                            }
                        )
                    }
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            driverProfile = driverProfile,
                            telemetry = telemetry,
                            isMonitoringActive = isMonitoringActive,
                            totalSeconds = totalSeconds,
                            recentEvents = historyEvents,
                            onNavigateToMonitoring = { viewModel.navigateTo(AppScreen.MONITORING) },
                            onNavigateToHistory = { viewModel.navigateTo(AppScreen.HISTORY) },
                            onNavigateToReports = { viewModel.navigateTo(AppScreen.REPORTS) },
                            onNavigateToAiAssistant = { viewModel.navigateTo(AppScreen.AI_ASSISTANT) },
                            onAcknowledgeAlert = { viewModel.acknowledgeAlert() }
                        )
                    }
                    AppScreen.MONITORING -> {
                        MonitoringScreen(
                            telemetry = telemetry,
                            isMonitoringActive = isMonitoringActive,
                            isCameraStreaming = isCameraStreaming,
                            currentScenario = currentScenario,
                            settings = settings,
                            onStartCamera = { viewModel.startCamera() },
                            onStopCamera = { viewModel.stopCamera() },
                            onStartDetection = { viewModel.startDetection() },
                            onStopDetection = { viewModel.stopDetection() },
                            onSelectScenario = { viewModel.setSimulationScenario(it) },
                            onUpdateSettings = { viewModel.updateSettings(it) },
                            onAcknowledgeAlert = { viewModel.acknowledgeAlert() }
                        )
                    }
                    AppScreen.HISTORY -> {
                        HistoryScreen(
                            events = historyEvents,
                            onClearHistory = { viewModel.clearHistory() }
                        )
                    }
                    AppScreen.REPORTS -> {
                        ReportsScreen(
                            events = historyEvents,
                            driverProfile = driverProfile
                        )
                    }
                    AppScreen.AI_ASSISTANT -> {
                        AiAssistantScreen(
                            messages = chatMessages,
                            isThinking = isAiThinking,
                            onSendMessage = { viewModel.sendAiQuestion(it) }
                        )
                    }
                    AppScreen.MODEL_INFO -> {
                        ModelInfoScreen()
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            driverProfile = driverProfile,
                            settings = settings,
                            onUpdateProfile = { viewModel.updateProfile(it) },
                            onUpdateSettings = { viewModel.updateSettings(it) },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}
