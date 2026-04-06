package com.example.handgestureapp

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object GestureActionHandler {

    private const val CONFIDENCE_THRESHOLD = 0.85f // 85% confianza
    private const val COOLDOWN_MS = 3000L // 3 segundos

    private var lastActionTime = 0L
    private var lastGestureType = ""

    private val handlerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun processGesture(
        context: Context,
        result: HandLandmarkerResult,
        gestureText: String
    ) {
        val gestureNumber = extractGestureNumber(gestureText) ?: return

        if (!isConfidenceAboveThreshold(result)) {
            return
        }

        if (isOnCooldown()) {
            return
        }

        val contactRepository = ContactRepository(
            AppDatabase.getInstance(context.applicationContext).contactDao()
        )

        handlerScope.launch {
            val contact = contactRepository.getContactByGesture(gestureNumber)
            if (contact != null) {
                executeAction(context, gestureNumber, contact)
                lastActionTime = System.currentTimeMillis()
                lastGestureType = gestureText
            }
        }
    }

    private fun extractGestureNumber(gestureText: String): Int? {
        return when {
            gestureText.contains("0️⃣") || gestureText.contains("Cero") -> 0
            gestureText.contains("1️⃣") || gestureText.contains("Uno") -> 1
            gestureText.contains("2️⃣") || gestureText.contains("Dos") -> 2
            gestureText.contains("3️⃣") || gestureText.contains("Tres") -> 3
            gestureText.contains("4️⃣") || gestureText.contains("Cuatro") -> 4
            gestureText.contains("5️⃣") || gestureText.contains("Cinco") -> 5
            else -> null
        }
    }

    private fun isConfidenceAboveThreshold(result: HandLandmarkerResult): Boolean {
        if (result.landmarks().isEmpty()) return false

        return try {
            val score = result.handedness()[0].score()
            score >= CONFIDENCE_THRESHOLD
        } catch (e: Exception) {
            false
        }
    }

    private fun isOnCooldown(): Boolean {
        val timeSinceLastAction = System.currentTimeMillis() - lastActionTime
        return timeSinceLastAction < COOLDOWN_MS
    }

    private fun executeAction(context: Context, gestureNumber: Int, contact: Contact) {
        when (gestureNumber) {
            0, 2 -> makeCall(context, contact.phoneNumber)
            1, 3, 4, 5 -> sendWhatsApp(context, contact.phoneNumber, contact.message.ifEmpty { "Hola :)" })
        }
    }

    private fun makeCall(context: Context, phoneNumber: String) {
        val data = Data.Builder()
            .putString("action_type", "call")
            .putString("phone_number", phoneNumber)
            .build()

        val callRequest = OneTimeWorkRequestBuilder<GestureActionWorker>()
            .setInputData(data)
            .setBackoffPolicy(
                BackoffPolicy.EXPONENTIAL,
                OneTimeWorkRequestBuilder.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "call_${System.currentTimeMillis()}",
            androidx.work.ExistingWorkPolicy.KEEP,
            callRequest
        )
    }

    private fun sendWhatsApp(context: Context, phoneNumber: String, message: String) {
        val data = Data.Builder()
            .putString("action_type", "whatsapp")
            .putString("phone_number", phoneNumber)
            .putString("message", message)
            .build()

        val whatsappRequest = OneTimeWorkRequestBuilder<GestureActionWorker>()
            .setInputData(data)
            .setBackoffPolicy(
                BackoffPolicy.EXPONENTIAL,
                OneTimeWorkRequestBuilder.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "whatsapp_${System.currentTimeMillis()}",
            androidx.work.ExistingWorkPolicy.KEEP,
            whatsappRequest
        )
    }
}
