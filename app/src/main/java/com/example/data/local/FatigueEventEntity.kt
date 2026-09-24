package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fatigue_events")
data class FatigueEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dateString: String,
    val timeString: String,
    val fatigueStatus: String,
    val eyeStatus: String,
    val mouthStatus: String,
    val earValue: Float,
    val marValue: Float,
    val confidence: Float,
    val isAlertTriggered: Boolean,
    val alertDurationSec: Float,
    val sessionDurationSec: Long,
    val notes: String = ""
)
