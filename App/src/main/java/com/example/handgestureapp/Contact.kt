package com.example.handgestureapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gestureId: Int,
    val name: String,
    val phoneNumber: String,
    val message: String
)
