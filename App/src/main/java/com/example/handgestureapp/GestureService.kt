package com.example.handgestureapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.IBinder
import androidx.core.app.NotificationCompat

class GestureService : Service(), HandLandmarkerHelper.LandmarkerListener {

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var cameraManager: CameraManager
    private var cameraId: String = ""
    private var isTorchOn = false
    private var lastGesture = ""

    override fun onCreate() {
        super.onCreate()

        // Notificación obligatoria para foreground service
        val channelId = "gesture_channel"
        val channel = NotificationChannel(
            channelId, "Gesture Detection",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Detector de gestos activo")
            .setContentText("Mostrando gesto con la mano...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        startForeground(1, notification)

        // Linterna
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0] // Cámara trasera

        // MediaPipe
        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            handLandmarkerHelperListener = this
        )
    }

    override fun onResults(resultBundle: com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult) {
        val gesture = GestureClassifier.classify(resultBundle)

        if (gesture != lastGesture) {
            lastGesture = gesture
            if (gesture.contains("Tres")) {
                toggleTorch(true)   // Enciende linterna
            } else {
                toggleTorch(false)  // Apaga linterna
            }
        }

        // Integración con repository de Room y acciones dinámicas
        GestureActionHandler.processGesture(this, resultBundle, gesture)
    }

    private fun toggleTorch(on: Boolean) {
        try {
            cameraManager.setTorchMode(cameraId, on)
            isTorchOn = on
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onError(error: String) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        toggleTorch(false)
        handLandmarkerHelper.close()
    }
}