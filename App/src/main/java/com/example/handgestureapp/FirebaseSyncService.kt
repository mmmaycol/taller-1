package com.example.handgestureapp

import com.google.firebase.database.FirebaseDatabase

object FirebaseSyncService {
    private val database = FirebaseDatabase.getInstance().reference

    fun syncEvent(event: GestureEvent) {
        val eventId = database.child("events").push().key ?: return
        database.child("events").child(eventId).setValue(event)
        
        // If it's a critical gesture (e.g., gesture 3), also update a "latest_critical" node
        if (event.gestureNumber == 3) {
            database.child("latest_critical").setValue(event)
        }
    }
}
