package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FatigueEventEntity
import com.example.data.model.AppSettings
import com.example.data.model.DriverProfile
import com.example.data.model.FatigueTelemetry
import com.example.data.repository.AiSafetyAssistantRepository
import com.example.data.repository.ChatMessage
import com.example.data.repository.FatigueRepository
import com.example.data.repository.MessageSender
import com.example.service.detection.FatigueDetectionEngine
import com.example.service.detection.SimulationScenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val label: String) {
    LOGIN("Login"),
    DASHBOARD("Dashboard"),
    MONITORING("Driver Monitoring"),
    HISTORY("Detection History"),
    REPORTS("Reports & Analytics"),
    AI_ASSISTANT("Safety AI Assistant"),
    MODEL_INFO("Model Information"),
    SETTINGS("Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FatigueRepository(database.fatigueDao())
    private val aiRepository = AiSafetyAssistantRepository()
    val engine = FatigueDetectionEngine(application, viewModelScope)

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Auth & Driver State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _driverProfile = MutableStateFlow(DriverProfile())
    val driverProfile: StateFlow<DriverProfile> = _driverProfile.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Detection Telemetry from Engine
    val telemetry: StateFlow<FatigueTelemetry> = engine.telemetry
    val isMonitoringActive: StateFlow<Boolean> = engine.isMonitoringActive
    val isCameraStreaming: StateFlow<Boolean> = engine.isCameraStreaming
    val totalMonitoringSeconds: StateFlow<Long> = engine.totalMonitoringSeconds
    val currentScenario: StateFlow<SimulationScenario> = engine.currentScenario

    // Database History
    val historyEvents: StateFlow<List<FatigueEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = "Hello! I am your Driver Safety AI Assistant for the 'Real-Time Driver Fatigue Detection' project. Ask me any question regarding driver drowsiness symptoms, OpenCV computer vision processing, Keras neural network models, or highway safety protocols."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Active alert dismissal / acknowledgement
    private val _activeAlertAcknowledged = MutableStateFlow(false)
    val activeAlertAcknowledged: StateFlow<Boolean> = _activeAlertAcknowledged.asStateFlow()

    private var lastRecordedAlertTime = 0L

    init {
        // Seed initial history records for demo/presentation
        viewModelScope.launch {
            repository.seedInitialDemoDataIfNeeded()
        }

        // Auto record critical fatigue events into Room database
        engine.onFatigueAlarmTriggered = { tele ->
            val now = System.currentTimeMillis()
            // Throttle database inserts to once every 10 seconds per continuous alarm
            if (now - lastRecordedAlertTime > 10_000) {
                lastRecordedAlertTime = now
                _activeAlertAcknowledged.value = false
                viewModelScope.launch {
                    repository.recordEvent(
                        fatigueStatus = tele.fatigueLevel.label,
                        eyeStatus = "${tele.eyeStatus.label} (${String.format("%.1f", tele.closedDurationSec)}s)",
                        mouthStatus = tele.mouthStatus.label,
                        earValue = tele.ear,
                        marValue = tele.mar,
                        confidence = tele.confidence,
                        isAlert = true,
                        alertDurationSec = tele.closedDurationSec,
                        sessionDurationSec = totalMonitoringSeconds.value,
                        notes = tele.warningMessage
                    )
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun login(email: String, name: String) {
        _driverProfile.value = _driverProfile.value.copy(
            email = email,
            name = if (name.isNotBlank()) name else "Driver Safety Scholar"
        )
        _isLoggedIn.value = true
        _currentScreen.value = AppScreen.DASHBOARD
    }

    fun logout() {
        engine.stopDetection()
        engine.stopCamera()
        _isLoggedIn.value = false
        _currentScreen.value = AppScreen.LOGIN
    }

    fun startCamera() {
        engine.startCamera()
    }

    fun stopCamera() {
        engine.stopCamera()
    }

    fun startDetection() {
        engine.startDetection(_settings.value)
    }

    fun stopDetection() {
        engine.stopDetection()
    }

    fun setSimulationScenario(scenario: SimulationScenario) {
        engine.setScenario(scenario)
    }

    fun acknowledgeAlert() {
        _activeAlertAcknowledged.value = true
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
    }

    fun updateProfile(profile: DriverProfile) {
        _driverProfile.value = profile
    }

    fun sendAiQuestion(question: String) {
        if (question.isBlank()) return
        val userMsg = ChatMessage(sender = MessageSender.USER, text = question.trim())
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val reply = aiRepository.getResponse(question)
                val assistantMsg = ChatMessage(sender = MessageSender.ASSISTANT, text = reply)
                _chatMessages.value = _chatMessages.value + assistantMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    text = "I'm ready to answer any questions on driver fatigue, OpenCV image processing, and Keras deep learning. Please ask another project-related question."
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.release()
    }
}
