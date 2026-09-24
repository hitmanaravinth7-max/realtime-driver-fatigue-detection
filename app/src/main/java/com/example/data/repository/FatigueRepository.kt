package com.example.data.repository

import com.example.data.local.FatigueDao
import com.example.data.local.FatigueEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FatigueRepository(private val dao: FatigueDao) {

    val allEvents: Flow<List<FatigueEventEntity>> = dao.getAllEvents()
    val alertEvents: Flow<List<FatigueEventEntity>> = dao.getAlertEventsOnly()

    suspend fun recordEvent(
        fatigueStatus: String,
        eyeStatus: String,
        mouthStatus: String,
        earValue: Float,
        marValue: Float,
        confidence: Float,
        isAlert: Boolean,
        alertDurationSec: Float = 0f,
        sessionDurationSec: Long = 0L,
        notes: String = ""
    ): Long {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val entity = FatigueEventEntity(
            timestamp = now,
            dateString = dateFormat.format(Date(now)),
            timeString = timeFormat.format(Date(now)),
            fatigueStatus = fatigueStatus,
            eyeStatus = eyeStatus,
            mouthStatus = mouthStatus,
            earValue = earValue,
            marValue = marValue,
            confidence = confidence,
            isAlertTriggered = isAlert,
            alertDurationSec = alertDurationSec,
            sessionDurationSec = sessionDurationSec,
            notes = notes
        )
        return dao.insertEvent(entity)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }

    suspend fun seedInitialDemoDataIfNeeded() {
        val count = dao.getTotalEventCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val oneHour = 3600 * 1000L
            val oneDay = 24 * oneHour

            val sampleEvents = listOf(
                FatigueEventEntity(
                    timestamp = now - 20 * 60 * 1000,
                    dateString = dateFormat.format(Date(now - 20 * 60 * 1000)),
                    timeString = timeFormat.format(Date(now - 20 * 60 * 1000)),
                    fatigueStatus = "Critical Fatigue",
                    eyeStatus = "Eyes Closed (2.3s)",
                    mouthStatus = "Normal",
                    earValue = 0.18f,
                    marValue = 0.39f,
                    confidence = 97.2f,
                    isAlertTriggered = true,
                    alertDurationSec = 2.3f,
                    sessionDurationSec = 1420L,
                    notes = "Prolonged eye closure detected during night test drive"
                ),
                FatigueEventEntity(
                    timestamp = now - 45 * 60 * 1000,
                    dateString = dateFormat.format(Date(now - 45 * 60 * 1000)),
                    timeString = timeFormat.format(Date(now - 45 * 60 * 1000)),
                    fatigueStatus = "Mild Fatigue",
                    eyeStatus = "Eyes Open",
                    mouthStatus = "Yawning Detected (MAR 0.74)",
                    earValue = 0.29f,
                    marValue = 0.74f,
                    confidence = 94.8f,
                    isAlertTriggered = true,
                    alertDurationSec = 3.1f,
                    sessionDurationSec = 890L,
                    notes = "Repetitive yawning sequence detected"
                ),
                FatigueEventEntity(
                    timestamp = now - 90 * 60 * 1000,
                    dateString = dateFormat.format(Date(now - 90 * 60 * 1000)),
                    timeString = timeFormat.format(Date(now - 90 * 60 * 1000)),
                    fatigueStatus = "Normal / Alert",
                    eyeStatus = "Eyes Open",
                    mouthStatus = "Normal",
                    earValue = 0.33f,
                    marValue = 0.41f,
                    confidence = 98.6f,
                    isAlertTriggered = false,
                    alertDurationSec = 0f,
                    sessionDurationSec = 3600L,
                    notes = "Stable highway driving, optimal alertness"
                ),
                FatigueEventEntity(
                    timestamp = now - oneDay,
                    dateString = dateFormat.format(Date(now - oneDay)),
                    timeString = "22:15:30",
                    fatigueStatus = "Critical Fatigue",
                    eyeStatus = "Eyes Closed (1.9s)",
                    mouthStatus = "Yawning Detected",
                    earValue = 0.19f,
                    marValue = 0.69f,
                    confidence = 96.1f,
                    isAlertTriggered = true,
                    alertDurationSec = 1.9f,
                    sessionDurationSec = 2700L,
                    notes = "Micro-sleep event detected after 45 min drive"
                ),
                FatigueEventEntity(
                    timestamp = now - oneDay - 4 * oneHour,
                    dateString = dateFormat.format(Date(now - oneDay)),
                    timeString = "18:30:10",
                    fatigueStatus = "Normal / Alert",
                    eyeStatus = "Eyes Open",
                    mouthStatus = "Normal",
                    earValue = 0.34f,
                    marValue = 0.38f,
                    confidence = 99.1f,
                    isAlertTriggered = false,
                    alertDurationSec = 0f,
                    sessionDurationSec = 1800L,
                    notes = "Evening commuter route, no signs of drowsiness"
                ),
                FatigueEventEntity(
                    timestamp = now - 2 * oneDay,
                    dateString = dateFormat.format(Date(now - 2 * oneDay)),
                    timeString = "23:45:00",
                    fatigueStatus = "Critical Fatigue",
                    eyeStatus = "Eyes Closed (2.8s)",
                    mouthStatus = "Normal",
                    earValue = 0.16f,
                    marValue = 0.42f,
                    confidence = 98.4f,
                    isAlertTriggered = true,
                    alertDurationSec = 2.8f,
                    sessionDurationSec = 3100L,
                    notes = "Audible alarm triggered. Driver pulled over safely."
                )
            )
            dao.insertAll(sampleEvents)
        }
    }
}
