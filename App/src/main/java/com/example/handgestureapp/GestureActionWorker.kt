package com.example.handgestureapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class GestureActionWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    companion object {
        private const val CHANNEL_ID = "gesture_actions"
        private const val NOTIFICATION_ID = 1001
        private const val TARGET_PHONE = "900710184"
    }
    
    override fun doWork(): Result {
        return try {
            val actionType = inputData.getString("action_type") ?: return Result.retry()
            
            when (actionType) {
                "call" -> handleCall()
                "whatsapp" -> handleWhatsApp()
                "notification" -> handleNotification()
                else -> Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    /**
     * Hace una llamada telefónica usando Intent
     */
    private fun handleCall(): Result {
        return try {
            val phoneNumber = inputData.getString("phone_number") ?: return Result.retry()

            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                logAction("Permiso CALL_PHONE no otorgado", "error")
                return Result.failure()
            }

            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (applicationContext.packageManager.resolveActivity(intent, 0) != null) {
                applicationContext.startActivity(intent)
                logAction("Llamada iniciada a $phoneNumber", "success")
                Result.success()
            } else {
                logAction("No se puede hacer llamadas", "error")
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logAction("Error al hacer llamada: ${e.message}", "error")
            Result.retry()
        }
    }
    
    /**
     * Envía mensaje por WhatsApp usando Intent
     */
    private fun handleWhatsApp(): Result {
        return try {
            val message = inputData.getString("message") ?: "Hola :)"
            val phoneNumber = inputData.getString("phone_number") ?: TARGET_PHONE
            
            // Formato: https://wa.me/[country_code][phone_number]
            // Para Perú: +51
            val phoneWithCode = "+51$phoneNumber"
            val cleanPhone = phoneWithCode.replace(Regex("[^0-9+]"), "")
            
            val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
            
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // Verificar que WhatsApp esté instalado
            if (applicationContext.packageManager.resolveActivity(intent, 0) != null) {
                applicationContext.startActivity(intent)
                logAction("WhatsApp abierto con mensaje listo para $phoneNumber", "success")
                Result.success()
            } else {
                // WhatsApp no instalado, lo documentamos pero no es error crítico
                logAction("WhatsApp no está instalado", "info")
                Result.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logAction("Error en WhatsApp: ${e.message}", "error")
            Result.retry()
        }
    }
    
    /**
     * Envía notificación push local (sin Firebase)
     */
    private fun handleNotification(): Result {
        return try {
            val message = inputData.getString("message") ?: "Gesto detectado"
            
            // Crear canal de notificación (requerido en Android 8+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Acciones de Gesto",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de gestos detectados"
                }
                val notificationManager = applicationContext.getSystemService(
                    NotificationManager::class.java
                )
                notificationManager?.createNotificationChannel(channel)
            }
            
            // Crear y mostrar notificación
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("🖐️ Gesto Detectado")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            
            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            logAction("Notificación enviada: $message", "success")
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
    
    /**
     * Registra acciones para debugging
     */
    private fun logAction(message: String, type: String) {
        val timestamp = System.currentTimeMillis()
        android.util.Log.d("GestureActionWorker", "[$type] $timestamp: $message")
    }
}
