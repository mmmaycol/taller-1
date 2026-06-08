package com.example.handgestureapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GestureEventDao {
    @Insert
    suspend fun insert(event: GestureEvent)

    @Query("SELECT * FROM gesture_events ORDER BY timestamp DESC")
    fun getAllEvents(): LiveData<List<GestureEvent>>

    @Query("SELECT COUNT(*) FROM gesture_events WHERE gestureNumber = :gestureNumber")
    suspend fun getCountForGesture(gestureNumber: Int): Int

    @Query("SELECT * FROM gesture_events WHERE timestamp >= :since")
    fun getEventsSince(since: Long): LiveData<List<GestureEvent>>
}
