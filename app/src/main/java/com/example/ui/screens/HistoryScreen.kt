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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FatigueEventEntity
import com.example.ui.components.AssistiveSafetyNotice
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CardSlateLight
import com.example.ui.theme.DeepCockpit
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TechCyan
import com.example.ui.theme.WarningAmber

enum class HistoryFilter {
    ALL, ALERT_ONLY, NORMAL_ONLY
}

@Composable
fun HistoryScreen(
    events: List<FatigueEventEntity>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedEventForDetails by remember { mutableStateOf<FatigueEventEntity?>(null) }

    val filteredEvents = remember(events, searchQuery, selectedFilter) {
        events.filter { event ->
            val matchesFilter = when (selectedFilter) {
                HistoryFilter.ALL -> true
                HistoryFilter.ALERT_ONLY -> event.isAlertTriggered
                HistoryFilter.NORMAL_ONLY -> !event.isAlertTriggered
            }
            val matchesSearch = searchQuery.isBlank() ||
                event.dateString.contains(searchQuery, ignoreCase = true) ||
                event.timeString.contains(searchQuery, ignoreCase = true) ||
                event.fatigueStatus.contains(searchQuery, ignoreCase = true) ||
                event.notes.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpit)
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DETECTION AUDIT LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Historical Events (${filteredEvents.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear History",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by date, time, or status...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TechCyan,
                unfocusedBorderColor = SurfaceBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == HistoryFilter.ALL,
                onClick = { selectedFilter = HistoryFilter.ALL },
                label = { Text("All Records") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TechCyan.copy(alpha = 0.2f),
                    selectedLabelColor = TechCyan
                )
            )
            FilterChip(
                selected = selectedFilter == HistoryFilter.ALERT_ONLY,
                onClick = { selectedFilter = HistoryFilter.ALERT_ONLY },
                label = { Text("Fatigue Alerts") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AlertRed.copy(alpha = 0.2f),
                    selectedLabelColor = AlertRed
                )
            )
            FilterChip(
                selected = selectedFilter == HistoryFilter.NORMAL_ONLY,
                onClick = { selectedFilter = HistoryFilter.NORMAL_ONLY },
                label = { Text("Normal Driving") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SafetyGreen.copy(alpha = 0.2f),
                    selectedLabelColor = SafetyGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No detection events match your filter",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    Card(
                        onClick = { selectedEventForDetails = event },
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (event.isAlertTriggered) AlertRed.copy(alpha = 0.6f) else SurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${event.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = TechCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${event.dateString} at ${event.timeString}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(
                                    label = if (event.isAlertTriggered) "ALERT" else "NORMAL",
                                    statusColor = if (event.isAlertTriggered) AlertRed else SafetyGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Fatigue Status: ${event.fatigueStatus}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Eye: ${event.eyeStatus}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Mouth: ${event.mouthStatus}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "EAR: ${String.format("%.2f", event.earValue)} | MAR: ${String.format("%.2f", event.marValue)}",
                                    fontSize = 11.sp,
                                    color = TechCyan
                                )
                                Text(
                                    text = "Confidence: ${String.format("%.1f", event.confidence)}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (event.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Note: ${event.notes}",
                                    fontSize = 11.sp,
                                    color = WarningAmber
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        AssistiveSafetyNotice()

        // Clear Confirmation Dialog
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear Detection History?") },
                text = { Text("This will permanently remove all recorded fatigue and normal monitoring log events from the local SQLite database.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearHistory()
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Event Detail Dialog
        selectedEventForDetails?.let { detail ->
            AlertDialog(
                onDismissRequest = { selectedEventForDetails = null },
                title = { Text("Fatigue Event Details") },
                text = {
                    Column {
                        Text("Date: ${detail.dateString} at ${detail.timeString}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Fatigue Classification: ${detail.fatigueStatus}")
                        Text("Eye State: ${detail.eyeStatus}")
                        Text("Mouth State: ${detail.mouthStatus}")
                        Text("Eye Aspect Ratio (EAR): ${String.format("%.3f", detail.earValue)}")
                        Text("Mouth Aspect Ratio (MAR): ${String.format("%.3f", detail.marValue)}")
                        Text("Neural Network Confidence: ${String.format("%.1f", detail.confidence)}%")
                        Text("Alarm Triggered: ${if (detail.isAlertTriggered) "YES (Audible & Visual)" else "NO"}")
                        if (detail.alertDurationSec > 0f) {
                            Text("Closure/Yawn Duration: ${String.format("%.1f", detail.alertDurationSec)} seconds")
                        }
                        if (detail.sessionDurationSec > 0L) {
                            Text("Total Drive Session: ${detail.sessionDurationSec / 60} minutes")
                        }
                        if (detail.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Observation: ${detail.notes}", color = WarningAmber)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedEventForDetails = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
