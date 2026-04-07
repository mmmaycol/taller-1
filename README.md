##NOTA IMPORTANTE: SI SALE ERROR EN LA APP, NO HAY PROBLEMA, SE DEBE EJECUTAR EL 'TALLER 1' JUNTO CON EL GRADLE
# Hand Gesture App

Aplicación Android que detecta gestos de mano en tiempo real usando la cámara frontal y los convierte en acciones como llamadas telefónicas o mensajes SMS.

## ¿Qué hace?

La app usa la cámara para reconocer qué número estás mostrando con los dedos (del 0 al 5) y ejecuta automáticamente una acción asociada a ese gesto: llama a un contacto o le manda un mensaje de texto.

Desde la pantalla de configuración podés asignar un contacto distinto a cada gesto. Si no hay nadie configurado para un gesto en particular, la app no hace nada.

## Gestos reconocidos

| Gesto | Acción |
|-------|--------|
| ✊ 0 dedos | Llamada al contacto asignado |
| ☝️ 1 dedo | SMS al contacto asignado |
| ✌️ 2 dedos | Llamada al contacto asignado |
| 🤟 3 dedos | SMS al contacto asignado |
| 🖖 4 dedos | SMS al contacto asignado |
| 🖐️ 5 dedos | SMS al contacto asignado |

La detección tiene un umbral de confianza del 60% y un cooldown de 3 segundos entre acciones para evitar disparos accidentales.

## Tecnologías usadas

- **Kotlin** — lenguaje principal
- **MediaPipe Hand Landmarker** — detección de landmarks de la mano (21 puntos por mano)
- **CameraX** — acceso a la cámara en tiempo real
- **Room** — base de datos local para guardar los contactos
- **WorkManager** — ejecución en background de llamadas y SMS
- **ViewModel + LiveData** — arquitectura de la pantalla de configuración

## Estructura del proyecto

```
handgestureapp/
├── MainActivity.kt           # Pantalla principal con la cámara y el overlay
├── GestureClassifier.kt      # Lógica para clasificar los gestos (0-5)
├── HandLandmarkerHelper.kt   # Wrapper de MediaPipe para el stream en vivo
├── OverlayView.kt            # Vista que dibuja el esqueleto de la mano sobre la cámara
├── GestureActionHandler.kt   # Decide qué acción ejecutar según el gesto detectado
├── GestureActionWorker.kt    # Worker que ejecuta llamadas, SMS y notificaciones
├── GestureService.kt         # Foreground service (no usado activamente en el flujo principal)
├── SettingsActivity.kt       # Pantalla para configurar contactos por gesto
├── SettingsViewModel.kt      # ViewModel de la pantalla de configuración
├── Contact.kt                # Entidad de base de datos
├── ContactDao.kt             # Queries de Room
├── ContactRepository.kt      # Repositorio de acceso a datos
└── AppDatabase.kt            # Instancia singleton de Room
```

## Permisos requeridos

La app solicita los siguientes permisos al instalarse o en el primer uso:

- `CAMERA` — para acceder a la cámara frontal
- `CALL_PHONE` — para realizar llamadas directas
- `SEND_SMS` — para enviar mensajes de texto automáticamente
- `POST_NOTIFICATIONS` — para mostrar notificaciones locales
- `FOREGROUND_SERVICE` — necesario para correr la detección en segundo plano

## Cómo correrlo

1. Clonar el repositorio
2. Abrir el proyecto en **Android Studio**
3. Conectar un dispositivo Android o iniciar un emulador con cámara
4. Darle a **Run** (`Shift + F10`)

> Requiere Android 8.0 (API 26) o superior.

## Notas

El modelo de MediaPipe (`hand_landmarker.task`) debe estar dentro de `App/src/main/assets/models/`. Si no está incluido en el repositorio, hay que descargarlo desde la [documentación oficial de MediaPipe](https://developers.google.com/mediapipe/solutions/vision/hand_landmarker).
