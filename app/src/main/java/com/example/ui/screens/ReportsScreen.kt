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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FatigueEventEntity
import com.example.data.model.DriverProfile
import com.example.ui.components.AssistiveSafetyNotice
import com.example.ui.components.MetricGaugeCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.DeepCockpit
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber

@Composable
fun ReportsScreen(
    events: List<FatigueEventEntity>,
    driverProfile: DriverProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showExportDialog by remember { mutableStateOf(false) }
    var copiedToClipboardNotice by remember { mutableStateOf(false) }

    val totalEvents = events.size
    val fatigueEventsCount = events.count { it.isAlertTriggered }
    val normalEventsCount = totalEvents - fatigueEventsCount
    val avgDurationMinutes = if (events.isNotEmpty()) {
        (events.map { it.sessionDurationSec }.average() / 60).toInt().coerceAtLeast(1)
    } else 0

    val generatedReportText = remember(events, driverProfile) {
        """
=====================================================
  REAL-TIME DRIVER FATIGUE DETECTION SYSTEM
  COLLEGE CAPSTONE PROJECT AUDIT REPORT
=====================================================
Driver Name      : ${driverProfile.name}
Email            : ${driverProfile.email}
Vehicle ID       : ${driverProfile.vehicleId}
Project Division : ${driverProfile.university}
Team Members     : ${driverProfile.teamMembers}

SUMMARY STATISTICS:
-----------------------------------------------------
Total Logged Sessions/Events : $totalEvents
Fatigue Alerts Triggered     : $fatigueEventsCount
Normal Vigilance Events      : $normalEventsCount
Alert Ratio                  : ${if (totalEvents > 0) String.format("%.1f", (fatigueEventsCount.toFloat() / totalEvents) * 100) else "0.0"}%
Average Session Duration     : $avgDurationMinutes mins

RECENT DETECTION INCIDENTS:
-----------------------------------------------------
${
            events.take(5).joinToString("\n") { ev ->
                "- [${ev.dateString} ${ev.timeString}] ${ev.fatigueStatus} | ${ev.eyeStatus} | EAR: ${String.format("%.2f", ev.earValue)} | MAR: ${String.format("%.2f", ev.marValue)} | Alert: ${if (ev.isAlertTriggered) "YES" else "NO"}"
            }
        }

System Architecture: OpenCV (Haar Cascades / Landmarks) + Keras MobileNetV2 CNN.
Notice: This document is an assistive academic safety report.
=====================================================
        """.trimIndent()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpit)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Reports Header Card
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
                            text = "SAFETY ANALYTICS & AUDIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Driver Vigilance Reports",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Aggregated statistics from OpenCV & CNN detection runs",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export / Download CTA button
                Button(
                    onClick = {
                        copiedToClipboardNotice = false
                        showExportDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("download_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = DeepCockpit,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate & Export College Project Report",
                        color = DeepCockpit,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2x2 Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricGaugeCard(
                title = "Total Sessions",
                valueString = "$totalEvents",
                subtitle = "Logged Monitoring Runs",
                progress = (totalEvents / 20f).coerceIn(0f, 1f),
                progressColor = TechCyan,
                icon = Icons.Default.Assessment,
                modifier = Modifier.weight(1f),
                testTag = "total_sessions_card"
            )

            MetricGaugeCard(
                title = "Fatigue Events",
                valueString = "$fatigueEventsCount",
                subtitle = "Critical Alarms Triggered",
                progress = if (totalEvents > 0) (fatigueEventsCount.toFloat() / totalEvents) else 0f,
                progressColor = AlertRed,
                icon = Icons.Default.NotificationsActive,
                modifier = Modifier.weight(1f),
                testTag = "fatigue_events_card"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricGaugeCard(
                title = "Normal Events",
                valueString = "$normalEventsCount",
                subtitle = "Optimal Vigilance",
                progress = if (totalEvents > 0) (normalEventsCount.toFloat() / totalEvents) else 0f,
                progressColor = SafetyGreen,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f),
                testTag = "normal_events_card"
            )

            MetricGaugeCard(
                title = "Avg Duration",
                valueString = "${avgDurationMinutes}m",
                subtitle = "Minutes per Session",
                progress = (avgDurationMinutes / 60f).coerceIn(0f, 1f),
                progressColor = WarningAmber,
                icon = Icons.Default.HourglassTop,
                modifier = Modifier.weight(1f),
                testTag = "avg_duration_card"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily / Weekly Fatigue Trend Bar Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FATIGUE INCIDENCE TREND (LAST 7 DAYS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val mockAlertCounts = listOf(1, 3, 0, 2, 4, 1, fatigueEventsCount.coerceAtMost(5))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    daysOfWeek.forEachIndexed { idx, day ->
                        val count = mockAlertCounts[idx]
                        val barHeightFactor = (count / 5f).coerceIn(0.12f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$count",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (count > 2) AlertRed else TechCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height((90 * barHeightFactor).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (count > 2) AlertRed else TechCyan)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Time-of-day Risk Distribution Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DIURNAL FATIGUE RISK PATTERNS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val intervals = listOf(
                    Triple("Morning (06:00 - 12:00)", "Low Risk", SafetyGreen),
                    Triple("Afternoon (12:00 - 18:00)", "Moderate (Post-lunch dip)", WarningAmber),
                    Triple("Evening (18:00 - 22:00)", "Elevated", WarningAmber),
                    Triple("Night / Micro-sleep (22:00 - 06:00)", "High Risk (Circadian trough)", AlertRed)
                )

                intervals.forEach { (time, risk, color) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Surface(
                            color = color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = risk,
                                color = color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        AssistiveSafetyNotice()

        // Report Preview & Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("College Project Report Preview") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Surface(
                            color = DeepCockpit,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generatedReportText,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = TechCyan,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        if (copiedToClipboardNotice) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Report copied to system clipboard!",
                                color = SafetyGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedReportText))
                            copiedToClipboardNotice = true

                            // Also launch system share sheet
                            try {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, generatedReportText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Export Driver Fatigue Report")
                                context.startActivity(shareIntent)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepCockpit)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy & Share", color = DeepCockpit, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
