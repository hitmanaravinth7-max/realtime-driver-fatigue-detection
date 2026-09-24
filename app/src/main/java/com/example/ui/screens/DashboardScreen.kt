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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FatigueEventEntity
import com.example.data.model.DriverProfile
import com.example.data.model.EyeState
import com.example.data.model.FatigueLevel
import com.example.data.model.FatigueTelemetry
import com.example.data.model.MouthState
import com.example.ui.components.AssistiveSafetyNotice
import com.example.ui.components.FatigueEmergencyAlertBanner
import com.example.ui.components.MetricGaugeCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.DeepCockpit
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    driverProfile: DriverProfile,
    telemetry: FatigueTelemetry,
    isMonitoringActive: Boolean,
    totalSeconds: Long,
    recentEvents: List<FatigueEventEntity>,
    onNavigateToMonitoring: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onAcknowledgeAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    val fatigueColor = when (telemetry.fatigueLevel) {
        FatigueLevel.NORMAL -> SafetyGreen
        FatigueLevel.MILD_DROWSINESS -> WarningAmber
        FatigueLevel.CRITICAL_FATIGUE -> AlertRed
    }

    val eyeColor = when (telemetry.eyeStatus) {
        EyeState.OPEN -> SafetyGreen
        EyeState.PARTIALLY_CLOSED -> WarningAmber
        EyeState.CLOSED -> AlertRed
    }

    val mouthColor = when (telemetry.mouthStatus) {
        MouthState.NORMAL -> SafetyGreen
        MouthState.SPEAKING -> TechCyan
        MouthState.YAWNING -> AlertRed
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpit)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Driver Status Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DRIVER MONITORING DASHBOARD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = driverProfile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vehicle ID: ${driverProfile.vehicleId}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // System Active status badge
                    StatusBadge(
                        label = if (isMonitoringActive) "ACTIVE TELEMETRY" else "STANDBY",
                        statusColor = if (isMonitoringActive) SafetyGreen else WarningAmber,
                        icon = if (isMonitoringActive) Icons.Default.CheckCircle else Icons.Default.HourglassBottom
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Launch Live Camera CTA
                Button(
                    onClick = onNavigateToMonitoring,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_live_monitoring_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = DeepCockpit,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMonitoringActive) "View Live Detection Camera" else "Start Driver Monitoring Camera",
                        color = DeepCockpit,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Emergency Alert Banner if Fatigue Detected
        FatigueEmergencyAlertBanner(
            isAlertActive = telemetry.isFatigueAlert,
            alertDurationSec = telemetry.closedDurationSec,
            timestamp = telemetry.timestamp,
            warningMessage = telemetry.warningMessage,
            onAcknowledge = onAcknowledgeAlert,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // 2x2 Telemetry Metric Cards
        Text(
            text = "REAL-TIME PHYSIOLOGICAL METRICS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricGaugeCard(
                title = "Fatigue Status",
                valueString = telemetry.fatigueLevel.label,
                subtitle = if (telemetry.isFatigueAlert) "Threshold Exceeded" else "Safe Alertness Level",
                progress = when (telemetry.fatigueLevel) {
                    FatigueLevel.NORMAL -> 0.2f
                    FatigueLevel.MILD_DROWSINESS -> 0.6f
                    FatigueLevel.CRITICAL_FATIGUE -> 1.0f
                },
                progressColor = fatigueColor,
                icon = Icons.Default.Warning,
                modifier = Modifier.weight(1f),
                testTag = "fatigue_status_card"
            )

            MetricGaugeCard(
                title = "Detection Confidence",
                valueString = "${String.format("%.1f", telemetry.confidence)}%",
                subtitle = "Convolutional Classifier",
                progress = (telemetry.confidence / 100f).coerceIn(0f, 1f),
                progressColor = TechCyan,
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f),
                testTag = "confidence_metric_card"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricGaugeCard(
                title = "Eye Status",
                valueString = telemetry.eyeStatus.label,
                subtitle = "EAR: ${String.format("%.2f", telemetry.ear)} (Closed: ${String.format("%.1f", telemetry.closedDurationSec)}s)",
                progress = (telemetry.ear / 0.40f).coerceIn(0f, 1f),
                progressColor = eyeColor,
                icon = Icons.Default.RemoveRedEye,
                modifier = Modifier.weight(1f),
                testTag = "eye_status_card"
            )

            MetricGaugeCard(
                title = "Mouth / Yawning",
                valueString = telemetry.mouthStatus.label,
                subtitle = "MAR: ${String.format("%.2f", telemetry.mar)} (Blinks: ${telemetry.blinkRatePerMin}/min)",
                progress = (telemetry.mar / 0.85f).coerceIn(0f, 1f),
                progressColor = mouthColor,
                icon = Icons.Default.Visibility,
                modifier = Modifier.weight(1f),
                testTag = "mouth_status_card"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Total Monitoring Time & Alert Status Row
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CardSlateLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TOTAL MONITORING TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTime(totalSeconds),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                StatusBadge(
                    label = if (telemetry.isFatigueAlert) "ALARM ACTIVE" else "ALL CLEAR",
                    statusColor = if (telemetry.isFatigueAlert) AlertRed else SafetyGreen,
                    icon = if (telemetry.isFatigueAlert) Icons.Default.NotificationsActive else Icons.Default.CheckCircle
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Detection History Mini Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT DETECTION HISTORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("view_full_history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp), tint = TechCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Full Log", fontSize = 11.sp, color = TechCyan)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recentEvents.isEmpty()) {
                    Text(
                        text = "No detection events recorded yet. Start monitoring to log events.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    recentEvents.take(3).forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${event.dateString} at ${event.timeString}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${event.eyeStatus} | ${event.mouthStatus}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            StatusBadge(
                                label = if (event.isAlertTriggered) "ALERT" else "NORMAL",
                                statusColor = if (event.isAlertTriggered) AlertRed else SafetyGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Link to Safety AI Assistant
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Driver Safety AI Assistant",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ask questions on drowsiness symptoms & OpenCV/Keras",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = onNavigateToAiAssistant,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    modifier = Modifier.testTag("open_ai_assistant_button")
                ) {
                    Text("Chat", fontSize = 12.sp, color = DeepCockpit, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        AssistiveSafetyNotice()
    }
}
