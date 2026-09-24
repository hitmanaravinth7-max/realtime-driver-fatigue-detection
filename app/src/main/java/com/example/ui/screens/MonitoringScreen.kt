package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.AppSettings
import com.example.data.model.EyeState
import com.example.data.model.FatigueLevel
import com.example.data.model.FatigueTelemetry
import com.example.data.model.MouthState
import com.example.service.detection.SimulationScenario
import com.example.ui.components.AssistiveSafetyNotice
import com.example.ui.components.FatigueEmergencyAlertBanner
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
fun MonitoringScreen(
    telemetry: FatigueTelemetry,
    isMonitoringActive: Boolean,
    isCameraStreaming: Boolean,
    currentScenario: SimulationScenario,
    settings: AppSettings,
    onStartCamera: () -> Unit,
    onStopCamera: () -> Unit,
    onStartDetection: () -> Unit,
    onStopDetection: () -> Unit,
    onSelectScenario: (SimulationScenario) -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onAcknowledgeAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            onStartCamera()
        }
    }

    var cameraSelector by remember {
        mutableStateOf(
            if (settings.isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val fatigueBorderColor = when (telemetry.fatigueLevel) {
        FatigueLevel.NORMAL -> TechCyan
        FatigueLevel.MILD_DROWSINESS -> WarningAmber
        FatigueLevel.CRITICAL_FATIGUE -> AlertRed
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpit)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Mode & Status Indicator Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DRIVER MONITORING HUD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (settings.isDemoMode) "DEMO SIMULATION MODE" else "LIVE CAMERA OPTICAL STREAM",
                    fontSize = 10.sp,
                    color = if (settings.isDemoMode) WarningAmber else SafetyGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            StatusBadge(
                label = if (telemetry.isFatigueAlert) "ALERT TRIGGERED" else "NORMAL",
                statusColor = if (telemetry.isFatigueAlert) AlertRed else SafetyGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Emergency Warning Banner when Alert is active
        FatigueEmergencyAlertBanner(
            isAlertActive = telemetry.isFatigueAlert,
            alertDurationSec = telemetry.closedDurationSec,
            timestamp = telemetry.timestamp,
            warningMessage = telemetry.warningMessage,
            onAcknowledge = onAcknowledgeAlert,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Camera Feed / HUD Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, fatigueBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .testTag("camera_preview_container")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isCameraStreaming && hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                } catch (_: Exception) {
                                    // Camera bind fallback
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback / Simulated Cockpit Driver Frame
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isCameraStreaming) Icons.Default.CameraAlt else Icons.Default.VideocamOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (!hasCameraPermission) "Camera Permission Required for Live Stream" else if (!isCameraStreaming) "Camera is Stopped" else "Initializing Sensor Feed...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!hasCameraPermission) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("grant_camera_permission_button")
                                ) {
                                    Text("Grant Camera Access", color = DeepCockpit, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // HUD Canvas Overlay (Facial Bounding Box, Landmark Mesh, Eye ROI, Mouth ROI)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val faceLeft = w * 0.25f
                    val faceTop = h * 0.18f
                    val faceWidth = w * 0.50f
                    val faceHeight = h * 0.62f

                    // Face Bounding Box Corners
                    val cornerLen = 28f
                    val strokeW = 4f
                    val hudColor = if (telemetry.isFatigueAlert) AlertRed else TechCyan

                    // Top-Left corner
                    drawLine(hudColor, Offset(faceLeft, faceTop), Offset(faceLeft + cornerLen, faceTop), strokeW)
                    drawLine(hudColor, Offset(faceLeft, faceTop), Offset(faceLeft, faceTop + cornerLen), strokeW)

                    // Top-Right corner
                    drawLine(hudColor, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth - cornerLen, faceTop), strokeW)
                    drawLine(hudColor, Offset(faceLeft + faceWidth, faceTop), Offset(faceLeft + faceWidth, faceTop + cornerLen), strokeW)

                    // Bottom-Left corner
                    drawLine(hudColor, Offset(faceLeft, faceTop + faceHeight), Offset(faceLeft + cornerLen, faceTop + faceHeight), strokeW)
                    drawLine(hudColor, Offset(faceLeft, faceTop + faceHeight), Offset(faceLeft, faceTop + faceHeight - cornerLen), strokeW)

                    // Bottom-Right corner
                    drawLine(hudColor, Offset(faceLeft + faceWidth, faceTop + faceHeight), Offset(faceLeft + faceWidth - cornerLen, faceTop + faceHeight), strokeW)
                    drawLine(hudColor, Offset(faceLeft + faceWidth, faceTop + faceHeight), Offset(faceLeft + faceWidth, faceTop + faceHeight - cornerLen), strokeW)

                    // Left Eye Box
                    val leftEyeX = faceLeft + faceWidth * 0.18f
                    val eyeY = faceTop + faceHeight * 0.32f
                    val eyeBoxW = faceWidth * 0.28f
                    val eyeBoxH = faceHeight * 0.16f
                    val eyeColor = if (telemetry.eyeStatus == EyeState.CLOSED) AlertRed else SafetyGreen

                    drawRect(
                        color = eyeColor,
                        topLeft = Offset(leftEyeX, eyeY),
                        size = Size(eyeBoxW, eyeBoxH),
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )

                    // Right Eye Box
                    val rightEyeX = faceLeft + faceWidth * 0.54f
                    drawRect(
                        color = eyeColor,
                        topLeft = Offset(rightEyeX, eyeY),
                        size = Size(eyeBoxW, eyeBoxH),
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )

                    // Mouth Box
                    val mouthX = faceLeft + faceWidth * 0.30f
                    val mouthY = faceTop + faceHeight * 0.65f
                    val mouthW = faceWidth * 0.40f
                    val mouthH = faceHeight * 0.20f
                    val mouthBoxColor = if (telemetry.mouthStatus == MouthState.YAWNING) AlertRed else TechCyan

                    drawRect(
                        color = mouthBoxColor,
                        topLeft = Offset(mouthX, mouthY),
                        size = Size(mouthW, mouthH),
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                }

                // Top Left Overlay: Live EAR / MAR Telemetry HUD
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                        Text(
                            text = "EAR: ${String.format("%.2f", telemetry.ear)} (${telemetry.eyeStatus.label})",
                            color = if (telemetry.eyeStatus == EyeState.CLOSED) AlertRed else SafetyGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MAR: ${String.format("%.2f", telemetry.mar)} (${telemetry.mouthStatus.label})",
                            color = if (telemetry.mouthStatus == MouthState.YAWNING) AlertRed else TechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Blinks: ${telemetry.blinkRatePerMin}/min | Conf: ${String.format("%.1f", telemetry.confidence)}%",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Top Right: Switch Camera front/back button
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = {
                            val newIsFront = !settings.isFrontCamera
                            cameraSelector = if (newIsFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                            onUpdateSettings(settings.copy(isFrontCamera = newIsFront))
                        },
                        modifier = Modifier.testTag("switch_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera Lens",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Banner on preview: Status text
                Surface(
                    color = (if (telemetry.isFatigueAlert) AlertRed else CardSlate).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = telemetry.warningMessage,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (telemetry.isFatigueAlert) "CRITICAL" else "SAFE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Camera and Detection Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DETECTION SYSTEM CONTROLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start / Stop Camera Button
                    Button(
                        onClick = {
                            if (isCameraStreaming) {
                                onStopCamera()
                            } else {
                                if (hasCameraPermission) {
                                    onStartCamera()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCameraStreaming) AlertRed else CardSlateLight
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_camera_button")
                    ) {
                        Icon(
                            imageVector = if (isCameraStreaming) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCameraStreaming) "Stop Camera" else "Start Camera",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Start / Stop Detection Button
                    Button(
                        onClick = {
                            if (isMonitoringActive) {
                                onStopDetection()
                            } else {
                                onStartDetection()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMonitoringActive) WarningAmber else TechCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_detection_button")
                    ) {
                        Icon(
                            imageVector = if (isMonitoringActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = DeepCockpit,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMonitoringActive) "Stop Detection" else "Start Detection",
                            color = DeepCockpit,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulation / Evaluation Scenario Selector
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
                        text = "EVALUATION SIMULATION SCENARIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        color = WarningAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "TESTING TOOL",
                            color = WarningAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select a physiological scenario to test system behavior and alarm verification in college demo:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                SimulationScenario.values().forEach { scenario ->
                    val isSelected = currentScenario == scenario
                    Card(
                        onClick = { onSelectScenario(scenario) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TechCyan.copy(alpha = 0.12f) else CardSlateLight
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) TechCyan else SurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("scenario_${scenario.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) TechCyan else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scenario.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TechCyan else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = scenario.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        AssistiveSafetyNotice()
    }
}
