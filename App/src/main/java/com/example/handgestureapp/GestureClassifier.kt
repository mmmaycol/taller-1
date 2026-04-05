package com.example.handgestureapp

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

object GestureClassifier {

    /**
     * Índices de landmarks de MediaPipe (21 puntos por mano):
     * 0=WRIST, 1-4=PULGAR, 5-8=ÍNDICE, 9-12=MEDIO, 13-16=ANULAR, 17-20=MEÑIQUE
     * TIP = punta del dedo (4, 8, 12, 16, 20)
     * PIP = segunda articulación (3, 7, 11, 15, 19)
     */

    private val FINGER_TIP_IDS = listOf(4, 8, 12, 16, 20)
    private val FINGER_PIP_IDS = listOf(3, 7, 11, 15, 19)

    fun classify(result: HandLandmarkerResult): String {
        if (result.landmarks().isEmpty()) return "Sin mano detectada ✋"

        val landmarks = result.landmarks()[0] // Primera mano
        val fingers = getExtendedFingers(landmarks)

        return when {
            isNumber0(fingers) -> "0️⃣ Cero"
            isNumber1(fingers) -> "1️⃣ Uno"
            isNumber2(fingers) -> "2️⃣ Dos"
            isNumber3(fingers) -> "3️⃣ Tres"
            isNumber4(fingers) -> "4️⃣ Cuatro"
            isNumber5(fingers) -> "5️⃣ Cinco"
            else -> "❓ Gesto desconocido"
        }
    }

    /**
     * Devuelve lista de booleanos: [pulgar, índice, medio, anular, meñique]
     * true = dedo extendido
     */
    private fun getExtendedFingers(landmarks: List<NormalizedLandmark>): List<Boolean> {
        val extended = mutableListOf<Boolean>()

        // Pulgar: comparar X (horizontal) porque se mueve lateralmente
        val thumbTip = landmarks[4]
        val thumbIP = landmarks[3]
        extended.add(thumbTip.x() < thumbIP.x()) // Ajustar según mano derecha/izquierda

        // Los 4 dedos restantes: comparar Y (vertical)
        for (i in 1..4) {
            val tip = landmarks[FINGER_TIP_IDS[i]]
            val pip = landmarks[FINGER_PIP_IDS[i]]
            extended.add(tip.y() < pip.y()) // Menor Y = más arriba = extendido
        }

        return extended
    }

    // [pulgar, índice, medio, anular, meñique]
    private fun isNumber0(f: List<Boolean>) = !f[1] && !f[2] && !f[3] && !f[4]
    private fun isNumber1(f: List<Boolean>) = f[1] && !f[2] && !f[3] && !f[4]
    private fun isNumber2(f: List<Boolean>) = f[1] && f[2] && !f[3] && !f[4]
    private fun isNumber3(f: List<Boolean>) = f[1] && f[2] && f[3] && !f[4]
    private fun isNumber4(f: List<Boolean>) = f[1] && f[2] && f[3] && f[4]
    private fun isNumber5(f: List<Boolean>) = f[0] && f[1] && f[2] && f[3] && f[4]
}