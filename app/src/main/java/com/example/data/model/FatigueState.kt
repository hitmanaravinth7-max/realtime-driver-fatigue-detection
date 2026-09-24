package com.example.data.model

enum class EyeState(val label: String) {
    OPEN("Eyes Open"),
    PARTIALLY_CLOSED("Partially Closed"),
    CLOSED("Eyes Closed")
}

enum class MouthState(val label: String) {
    NORMAL("Normal"),
    SPEAKING("Speaking / Moving"),
    YAWNING("Yawning Detected")
}

enum class FatigueLevel(val label: String, val severityRank: Int) {
    NORMAL("Normal / Alert", 0),
    MILD_DROWSINESS("Mild Fatigue", 1),
    CRITICAL_FATIGUE("High Risk Fatigue", 2)
}

enum class AlertStatus(val label: String) {
    NORMAL("Normal"),
    WARNING("Caution"),
    CRITICAL_ALERT("FATIGUE DETECTED – PLEASE TAKE A BREAK")
}

data class FatigueTelemetry(
    val timestamp: Long = System.currentTimeMillis(),
    val ear: Float = 0.32f, // Eye Aspect Ratio (Normal: ~0.28-0.35, Closed: < 0.22)
    val mar: Float = 0.38f, // Mouth Aspect Ratio (Normal: ~0.35-0.45, Yawn: > 0.65)
    val blinkRatePerMin: Int = 18,
    val closedDurationSec: Float = 0f,
    val yawnDurationSec: Float = 0f,
    val confidence: Float = 96.4f,
    val eyeStatus: EyeState = EyeState.OPEN,
    val mouthStatus: MouthState = MouthState.NORMAL,
    val fatigueLevel: FatigueLevel = FatigueLevel.NORMAL,
    val isFatigueAlert: Boolean = false,
    val warningMessage: String = "Driver is alert and responsive",
    val isDemoMode: Boolean = true
)

data class DriverProfile(
    val email: String = "driver@college.edu",
    val name: String = "Driver Safety Scholar",
    val vehicleId: String = "TN-09-EV-2026",
    val projectTitle: String = "Real-Time Driver Fatigue Detection",
    val university: String = "College Final Year Capstone",
    val teamMembers: String = "Engineering Final Year Project Team",
    val isDemoAccount: Boolean = true
)

data class AppSettings(
    val earThreshold: Float = 0.22f,
    val marThreshold: Float = 0.65f,
    val eyeClosureDurationTriggerSec: Float = 1.5f,
    val soundAlertEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isDemoMode: Boolean = true,
    val sensitivity: String = "Balanced (1.5s delay)",
    val isDarkTheme: Boolean = true
)
