package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FatigueDao {
    @Query("SELECT * FROM fatigue_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<FatigueEventEntity>>

    @Query("SELECT * FROM fatigue_events WHERE isAlertTriggered = 1 ORDER BY timestamp DESC")
    fun getAlertEventsOnly(): Flow<List<FatigueEventEntity>>

    @Query("SELECT COUNT(*) FROM fatigue_events")
    suspend fun getTotalEventCount(): Int

    @Query("SELECT COUNT(*) FROM fatigue_events WHERE isAlertTriggered = 1")
    suspend fun getFatigueAlertCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: FatigueEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<FatigueEventEntity>)

    @Query("DELETE FROM fatigue_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM fatigue_events")
    suspend fun clearAll()
}
