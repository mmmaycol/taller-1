package com.example.handgestureapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gesture_events")
data class GestureEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gestureNumber: Int,
    val actionType: String,      // "sms", "call", "notification"
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean
)
