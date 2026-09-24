package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.DriverProfile
import com.example.ui.components.AssistiveSafetyNotice
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.DeepCockpit
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    driverProfile: DriverProfile,
    settings: AppSettings,
    onUpdateProfile: (DriverProfile) -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(driverProfile.name) }
    var vehicleId by remember { mutableStateOf(driverProfile.vehicleId) }
    var teamMembers by remember { mutableStateOf(driverProfile.teamMembers) }

    var soundEnabled by remember { mutableStateOf(settings.soundAlertEnabled) }
    var vibrationEnabled by remember { mutableStateOf(settings.vibrationEnabled) }
    var isFrontCamera by remember { mutableStateOf(settings.isFrontCamera) }
    var isDemoMode by remember { mutableStateOf(settings.isDemoMode) }
    var isDarkTheme by remember { mutableStateOf(settings.isDarkTheme) }

    var earThreshold by remember { mutableFloatStateOf(settings.earThreshold) }
    var marThreshold by remember { mutableFloatStateOf(settings.marThreshold) }
    var closureDelaySec by remember { mutableFloatStateOf(settings.eyeClosureDurationTriggerSec) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var saveSuccessNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpit)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TechCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = TechCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SYSTEM CONFIGURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Settings & Driver Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Driver & Project Profile Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DRIVER & PROJECT PROFILE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Driver / Student Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = vehicleId,
                    onValueChange = { vehicleId = it },
                    label = { Text("Vehicle Registration / Cabin ID") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_id_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = teamMembers,
                    onValueChange = { teamMembers = it },
                    label = { Text("Project Team Members / Batch") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onUpdateProfile(
                            driverProfile.copy(
                                name = name.trim(),
                                vehicleId = vehicleId.trim(),
                                teamMembers = teamMembers.trim()
                            )
                        )
                        saveSuccessNotice = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_profile_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = DeepCockpit, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Profile Changes", color = DeepCockpit, fontWeight = FontWeight.Bold)
                }

                if (saveSuccessNotice) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Profile updated successfully!",
                        color = SafetyGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Alert & Feedback Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALERT & FEEDBACK CHANNELS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Sound Alert Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Audible Emergency Siren", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Play loud tone alarm when prolonged eye closure is detected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            onUpdateSettings(settings.copy(soundAlertEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = TechCyan, checkedTrackColor = TechCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("sound_alert_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Vibration Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Device Haptic Vibration", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Trigger steering / seat tactile pulse when drowsy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            onUpdateSettings(settings.copy(vibrationEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = TechCyan, checkedTrackColor = TechCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("vibration_alert_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Computer Vision & Sensitivity Thresholds
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DETECTION SENSITIVITY CALIBRATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Eye closure duration slider
                Text(
                    text = "Eye Closure Alarm Delay: ${String.format("%.1f", closureDelaySec)} seconds",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Consecutive duration with closed eyes before alarm fires (Standard: 1.5s)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = closureDelaySec,
                    onValueChange = {
                        closureDelaySec = it
                        onUpdateSettings(settings.copy(eyeClosureDurationTriggerSec = it))
                    },
                    valueRange = 0.8f..3.0f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = TechCyan, activeTrackColor = TechCyan),
                    modifier = Modifier.testTag("closure_delay_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // EAR Threshold
                Text(
                    text = "Eye Aspect Ratio (EAR) Threshold: ${String.format("%.2f", earThreshold)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Values below this count as closed eyes (Soukupová & Čech: 0.22)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = earThreshold,
                    onValueChange = {
                        earThreshold = it
                        onUpdateSettings(settings.copy(earThreshold = it))
                    },
                    valueRange = 0.15f..0.30f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = SafetyGreen, activeTrackColor = SafetyGreen),
                    modifier = Modifier.testTag("ear_threshold_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // MAR Threshold
                Text(
                    text = "Mouth Aspect Ratio (MAR) Threshold: ${String.format("%.2f", marThreshold)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Values above this count as active yawning (Standard: 0.65)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = marThreshold,
                    onValueChange = {
                        marThreshold = it
                        onUpdateSettings(settings.copy(marThreshold = it))
                    },
                    valueRange = 0.50f..0.85f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber),
                    modifier = Modifier.testTag("mar_threshold_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Camera & Simulation Mode
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Science, contentDescription = null, tint = TechCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENVIRONMENT & SIMULATION MODE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Simulation / Demo Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Safe Demo / Simulation Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Simulates facial dynamics for safe college project demonstration", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = {
                            isDemoMode = it
                            onUpdateSettings(settings.copy(isDemoMode = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = WarningAmber, checkedTrackColor = WarningAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("demo_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Camera Selector Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Camera Lens", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = if (isFrontCamera) "Front-Facing Driver Cabin Lens" else "Rear Environment Lens", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilterChip(
                        selected = isFrontCamera,
                        onClick = {
                            isFrontCamera = !isFrontCamera
                            onUpdateSettings(settings.copy(isFrontCamera = isFrontCamera))
                        },
                        label = { Text(if (isFrontCamera) "Front Cam" else "Rear Cam") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TechCyan.copy(alpha = 0.2f), selectedLabelColor = TechCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out of Session", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))
        AssistiveSafetyNotice()

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out?") },
                text = { Text("Are you sure you want to end your driver monitoring session? Active camera and fatigue tracking will be stopped.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Text("Log Out")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
