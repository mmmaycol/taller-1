package com.example.handgestureapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private val pointPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 12f
        style = Paint.Style.FILL
    }
    private val linePaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    // Conexiones entre landmarks (esqueleto de la mano)
    private val connections = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4,       // Pulgar
        0 to 5, 5 to 6, 6 to 7, 7 to 8,         // Índice
        0 to 9, 9 to 10, 10 to 11, 11 to 12,    // Medio
        0 to 13, 13 to 14, 14 to 15, 15 to 16,  // Anular
        0 to 17, 17 to 18, 18 to 19, 19 to 20,  // Meñique
        5 to 9, 9 to 13, 13 to 17               // Palma
    )

    fun setResults(handLandmarkerResult: HandLandmarkerResult) {
        results = handLandmarkerResult
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val result = results ?: return

        for (landmarks in result.landmarks()) {
            // Dibujar conexiones
            for ((start, end) in connections) {
                val s = landmarks[start]
                val e = landmarks[end]
                canvas.drawLine(
                    s.x() * width, s.y() * height,
                    e.x() * width, e.y() * height,
                    linePaint
                )
            }
            // Dibujar puntos
            for (landmark in landmarks) {
                canvas.drawCircle(landmark.x() * width, landmark.y() * height, 8f, pointPaint)
            }
        }
    }
}