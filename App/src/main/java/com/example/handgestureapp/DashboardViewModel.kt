package com.example.handgestureapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GestureEventDao = AppDatabase.getInstance(application).gestureEventDao()
    val allEvents: LiveData<List<GestureEvent>> = repository.getAllEvents()

    private val _systemStatus = MutableLiveData<String>("Inactivo")
    val systemStatus: LiveData<String> = _systemStatus

    private val _lastGesto = MutableLiveData<String>("Ninguno")
    val lastGesto: LiveData<String> = _lastGesto

    private val database = FirebaseDatabase.getInstance().reference.child("events")

    init {
        listenToFirebaseUpdates()
    }

    private fun listenToFirebaseUpdates() {
        database.limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This is just to demonstrate real-time sync with Firebase
                // In a real app, we might update the UI or local DB
                _systemStatus.value = "Activo - Sincronizado"
            }

            override fun onCancelled(error: DatabaseError) {
                _systemStatus.value = "Error de conexión"
            }
        })
    }
    
    fun updateStatus(status: String, lastGesto: String) {
        _systemStatus.postValue(status)
        _lastGesto.postValue(lastGesto)
    }
}
