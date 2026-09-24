package com.example.service.detection

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.AlertStatus
import com.example.data.model.AppSettings
import com.example.data.model.EyeState
import com.example.data.model.FatigueLevel
import com.example.data.model.FatigueTelemetry
import com.example.data.model.MouthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class SimulationScenario(val label: String, val description: String) {
    NORMAL("Normal Driving", "Eyes wide open, regular blink rate, relaxed jaw"),
    MICRO_SLEEP("Micro-Sleep (Eyes Closed)", "Prolonged eye closure (>1.5s), triggers critical alarm"),
    YAWNING("Yawning Episode", "Mouth opens wide (MAR > 0.65) for 3+ seconds"),
    GRADUAL_FATIGUE("Gradual Drowsiness", "Heavy eyelids, erratic blinking, drooping posture")
}

class FatigueDetectionEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _telemetry = MutableStateFlow(FatigueTelemetry())
    val telemetry: StateFlow<FatigueTelemetry> = _telemetry.asStateFlow()

    private val _isMonitoringActive = MutableStateFlow(false)
    val isMonitoringActive: StateFlow<Boolean> = _isMonitoringActive.asStateFlow()

    private val _isCameraStreaming = MutableStateFlow(false)
    val isCameraStreaming: StateFlow<Boolean> = _isCameraStreaming.asStateFlow()

    private val _currentScenario = MutableStateFlow(SimulationScenario.NORMAL)
    val currentScenario: StateFlow<SimulationScenario> = _currentScenario.asStateFlow()

    private val _totalMonitoringSeconds = MutableStateFlow(0L)
    val totalMonitoringSeconds: StateFlow<Long> = _totalMonitoringSeconds.asStateFlow()

    private var monitoringJob: Job? = null
    private var timerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    // Tracking variables for temporal thresholds
    private var eyeClosedAccumulatorSec = 0f
    private var yawnAccumulatorSec = 0f
    private var simulatedTick = 0

    var onFatigueAlarmTriggered: ((FatigueTelemetry) -> Unit)? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 85)
        } catch (_: Exception) {
            // Audio device fallback
        }
    }

    fun startCamera() {
        _isCameraStreaming.value = true
    }

    fun stopCamera() {
        _isCameraStreaming.value = false
        if (_isMonitoringActive.value) {
            stopDetection()
        }
    }

    fun startDetection(settings: AppSettings) {
        if (_isMonitoringActive.value) return
        _isMonitoringActive.value = true

        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Default) {
            while (_isMonitoringActive.value) {
                delay(1000)
                _totalMonitoringSeconds.value += 1
            }
        }

        monitoringJob?.cancel()
        monitoringJob = scope.launch(Dispatchers.Default) {
            while (_isMonitoringActive.value) {
                delay(200) // 5 updates per second
                simulatedTick++
                processDetectionCycle(settings)
            }
        }
    }

    fun stopDetection() {
        _isMonitoringActive.value = false
        monitoringJob?.cancel()
        timerJob?.cancel()
        eyeClosedAccumulatorSec = 0f
        yawnAccumulatorSec = 0f
        stopAlarmSounds()
    }

    fun setScenario(scenario: SimulationScenario) {
        _currentScenario.value = scenario
        eyeClosedAccumulatorSec = 0f
        yawnAccumulatorSec = 0f
    }

    private fun processDetectionCycle(settings: AppSettings) {
        val scenario = _currentScenario.value
        val isDemo = settings.isDemoMode

        var currentEar = 0.32f
        var currentMar = 0.38f
        var blinkRate = 18
        var eyeState = EyeState.OPEN
        var mouthState = MouthState.NORMAL
        var fatigueLevel = FatigueLevel.NORMAL
        var confidence = 96.5f

        val deltaSec = 0.2f

        when (scenario) {
            SimulationScenario.NORMAL -> {
                // Natural subtle variations
                val jitter = (sin(simulatedTick * 0.1) * 0.02).toFloat()
                currentEar = 0.32f + jitter
                currentMar = 0.38f + (sin(simulatedTick * 0.15) * 0.03).toFloat()
                blinkRate = 16 + (simulatedTick % 5)
                eyeClosedAccumulatorSec = 0f
                yawnAccumulatorSec = 0f
                eyeState = EyeState.OPEN
                mouthState = MouthState.NORMAL
                fatigueLevel = FatigueLevel.NORMAL
                confidence = 97.4f + (jitter * 20)
            }
            SimulationScenario.MICRO_SLEEP -> {
                // Driver eyes shut completely!
                eyeClosedAccumulatorSec += deltaSec
                currentEar = 0.15f + ((sin(simulatedTick * 0.3) * 0.02).toFloat())
                currentMar = 0.36f
                blinkRate = 6
                eyeState = EyeState.CLOSED
                mouthState = MouthState.NORMAL
                confidence = 98.2f

                if (eyeClosedAccumulatorSec >= settings.eyeClosureDurationTriggerSec) {
                    fatigueLevel = FatigueLevel.CRITICAL_FATIGUE
                } else if (eyeClosedAccumulatorSec >= 0.6f) {
                    fatigueLevel = FatigueLevel.MILD_DROWSINESS
                }
            }
            SimulationScenario.YAWNING -> {
                // Mouth opens wide for a few seconds
                yawnAccumulatorSec += deltaSec
                currentEar = 0.26f // eyes squint slightly during deep yawn
                currentMar = 0.72f + ((sin(simulatedTick * 0.2) * 0.04).toFloat())
                blinkRate = 22
                eyeState = EyeState.PARTIALLY_CLOSED
                mouthState = MouthState.YAWNING
                confidence = 95.8f

                if (yawnAccumulatorSec >= 2.0f) {
                    fatigueLevel = FatigueLevel.CRITICAL_FATIGUE
                } else {
                    fatigueLevel = FatigueLevel.MILD_DROWSINESS
                }
            }
            SimulationScenario.GRADUAL_FATIGUE -> {
                // Fluctuating heavy eyelids and erratic blinks
                val wave = ((sin(simulatedTick * 0.08) + 1.0) / 2.0).toFloat() // 0.0f to 1.0f
                currentEar = 0.21f + (wave * 0.09f)
                currentMar = 0.45f + ((1f - wave) * 0.22f)
                blinkRate = 28 + (wave * 8).toInt()

                if (currentEar < settings.earThreshold) {
                    eyeClosedAccumulatorSec += deltaSec
                    eyeState = EyeState.CLOSED
                } else if (currentEar < 0.26f) {
                    eyeClosedAccumulatorSec = 0f
                    eyeState = EyeState.PARTIALLY_CLOSED
                } else {
                    eyeClosedAccumulatorSec = 0f
                    eyeState = EyeState.OPEN
                }

                if (currentMar > settings.marThreshold) {
                    yawnAccumulatorSec += deltaSec
                    mouthState = MouthState.YAWNING
                } else {
                    yawnAccumulatorSec = 0f
                    mouthState = MouthState.NORMAL
                }

                fatigueLevel = if (eyeClosedAccumulatorSec > 1.2f || yawnAccumulatorSec > 2.0f) {
                    FatigueLevel.CRITICAL_FATIGUE
                } else {
                    FatigueLevel.MILD_DROWSINESS
                }
                confidence = 94.0f + (wave * 4f)
            }
        }

        val isAlert = fatigueLevel == FatigueLevel.CRITICAL_FATIGUE
        val warningMessage = when {
            isAlert && eyeClosedAccumulatorSec >= settings.eyeClosureDurationTriggerSec ->
                "FATIGUE DETECTED – PLEASE TAKE A BREAK. Prolonged eye closure of ${String.format("%.1f", eyeClosedAccumulatorSec)}s"
            isAlert && mouthState == MouthState.YAWNING ->
                "FATIGUE DETECTED – PLEASE TAKE A BREAK. Extended yawn duration of ${String.format("%.1f", yawnAccumulatorSec)}s"
            fatigueLevel == FatigueLevel.MILD_DROWSINESS ->
                "Caution: Early fatigue signs detected. Reduced driver alertness."
            else ->
                "Normal Driving: Optimal vigilance and active eye tracking."
        }

        val newTelemetry = FatigueTelemetry(
            timestamp = System.currentTimeMillis(),
            ear = currentEar.coerceIn(0.1f, 0.45f),
            mar = currentMar.coerceIn(0.2f, 0.95f),
            blinkRatePerMin = blinkRate,
            closedDurationSec = eyeClosedAccumulatorSec,
            yawnDurationSec = yawnAccumulatorSec,
            confidence = confidence.coerceIn(85.0f, 99.8f),
            eyeStatus = eyeState,
            mouthStatus = mouthState,
            fatigueLevel = fatigueLevel,
            isFatigueAlert = isAlert,
            warningMessage = warningMessage,
            isDemoMode = isDemo
        )

        _telemetry.value = newTelemetry

        if (isAlert) {
            triggerAlarms(settings)
            onFatigueAlarmTriggered?.invoke(newTelemetry)
        }
    }

    private fun triggerAlarms(settings: AppSettings) {
        if (settings.soundAlertEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            } catch (_: Exception) {}
        }
        if (settings.vibrationEnabled) {
            triggerVibration()
        }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(250)
            }
        } catch (_: Exception) {}
    }

    private fun stopAlarmSounds() {
        try {
            toneGenerator?.stopTone()
        } catch (_: Exception) {}
    }

    fun release() {
        stopDetection()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
