# Sistema de Acciones por Gestos - Guía de Implementación

## Características Implementadas

### 1. **Filtro de Confianza (85%)**
- Solo procesa gestos detectados con confianza ≥ 85%
- Verifica `handedness.score()` del MediaPipe HandLandmarker
- Rechaza gestos con confianza inferior

### 2. **Cooldown de 3 Segundos**
- Evita múltiples acciones del mismo gesto en ciclos rápidos
- Tiempo entre acciones: 3000 ms (3 segundos)
- Implementado en `GestureActionHandler.isOnCooldown()`

### 3. **Motor de Acciones Inteligente**

#### Mapeo de Gestos → Acciones:
| Gesto | Acción | Descripción |
|-------|--------|-------------|
| **0** (Puño cerrado) | 📞 Llamada | Llama automáticamente a 900710184 |
| **1** (Solo índice) | 💬 WhatsApp | Abre WhatsApp con "Hola :)" listo para enviar a 900710184 |
| **2** (Índice + Medio) | 📞 Llamada | Llama automáticamente a 933745474 |
| **3** (3 dedos) | 💬 WhatsApp | Abre WhatsApp con "Hola :)" listo para enviar a 933745474 |
| **4** (4 dedos) | 💬 WhatsApp | Abre WhatsApp con "Hola :)" listo para enviar a 900710184 |
| **5** (Mano abierta) | 💬 WhatsApp | Abre WhatsApp con "Hola :)" listo para enviar a 900710184 |

### 4. **Métodos de Envío**

#### A) **Llamadas Telefónicas (Intent.ACTION_CALL)**
- Usa `Intent.ACTION_CALL` nativo de Android
- Requiere permiso `CALL_PHONE`
- Inicia llamada automáticamente al número especificado
- Compatible con Android 6+ (M) y versiones anteriores

#### B) **WhatsApp (Intent)**
- Formato: `https://wa.me/+51900710184?text=Hola%20:)`
- Abre WhatsApp automáticamente si está instalado
- Mensaje "Hola :)" preescrito y listo para enviar
- Requiere confirmación manual del usuario para enviar

### 5. **WorkManager para Robustez**
- Garantiza entrega incluso si el sistema recicla procesos
- Reintentos automáticos con backoff exponencial
- Tareas persistentes en la BD de WorkManager
- No usa Firebase, solo WorkManager nativo

## Permisos Agregados

```xml
<!-- Llamadas telefónicas -->
<uses-permission android:name="android.permission.CALL_PHONE" />

<!-- Notificaciones (por si se agregan en el futuro) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Verificar paquetes instalados (WhatsApp, etc.) -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

## Dependencias Agregadas

```toml
# libs.versions.toml
workManager = "2.8.1"

# build.gradle.kts
implementation(libs.androidx.work)
```

## Archivos Creados

### 1. **GestureActionHandler.kt**
- Lógica principal de procesamiento
- Filtros de confianza y cooldown
- Router de acciones

### 2. **GestureActionWorker.kt**
- Worker para WorkManager
- Implementa SMS, WhatsApp, Notificaciones
- Manejo de errores y reintentos

## Configuración del Número de Teléfono

El número destino está configurado en:
- `GestureActionHandler.kt`: `TARGET_PHONE = "900710184"`
- `GestureActionWorker.kt`: `TARGET_PHONE = "900710184"`

Para cambiar el número, actualiza ambas constantes.

## Flujo de Ejecución

```
1. MainActivity detecta frame
   ↓
2. HandLandmarkerHelper procesa frame
   ↓
3. GestureClassifier.classify() → "3️⃣ Tres"
   ↓
4. GestureActionHandler.processGesture()
   - Verifica confianza >= 85% ✓
   - Verifica cooldown (no está dentro) ✓
   - Ejecuta acción (Gesto 3 → SMS)
   ↓
5. Enqueue WorkManager SMS task
   ↓
6. GestureActionWorker.doWork()
   - SmsManager.sendMultipartTextMessage()
   - Resultado: SUCCESS / RETRY
```

## Prueba Manual

### 1. **Requisitos Previos**
- Permisos de SMS/Notificaciones aprobados en runtime
- (Opcional) WhatsApp instalado para pruebas de WhatsApp

### 2. **Pasos de Prueba**

```kotlin
// En Build & Debug en Android Studio:

1. Ejecutar en emulador/dispositivo
2. Permitir CAMERA, SEND_SMS, POST_NOTIFICATIONS
3. Mostrar gesto "3" (3 dedos)
   → Esperar 3 segundos → Verá notificación/SMS
4. Mostrar gesto "1" (dedo índice)
   → Abrirá WhatsApp automáticamente

// Logcat para debugging:
adb logcat | grep GestureActionWorker
```

### 3. **En Emulador**
- SMS: Se guarda en bandeja de emulador
- WhatsApp: Abre emulador de WhatsApp (si instalado)
- Notificaciones: Visible en bandeja del emulador

## Notas Importantes

1. **Formato de Teléfono**: El número "900710184" NO tiene código de país
   - Para WhatsApp se agrega automáticamente "+51" (Perú)
   - Para SMS se envía sin modificar

2. **Confianza de 85%**
   - `handedness.score()` debe ser >= 0.85
   - Ajusta en `GestureActionHandler.CONFIDENCE_THRESHOLD`

3. **Cooldown de 3 segundos**
   - Configurable en `GestureActionHandler.COOLDOWN_MS`
   - Evita spam de acciones

4. **WorkManager**
   - Le WorkManager se inicializa automáticamente vía ContentProvider
   - No requiere setup manual
   - Tasks se guardan automáticamente

## Debugging

### Ver logs en Android Studio:
```bash
# Todos los logs de GestureActionWorker
adb logcat *:S GestureActionWorker:V

# WorkManager tasks
adb logcat *:S WorkManager:V

# SMS envios
adb logcat *:S Telephony:V
```

### En código:
```kotlin
android.util.Log.d("GestureActionWorker", "Mi mensaje $variable")
```

## Flujo Completo de Ejemplo

**Cuando el usuario muestra el gesto "3 dedos":**

1. ✅ MediaPipe detecta 21 landmarks de mano
2. ✅ GestureClassifier verifica dedos extendidos → "3️⃣ Tres"
3. ✅ MainActivity llama `GestureActionHandler.processGesture()`
4. ✅ Verifica confianza >= 85% → ✅ PASS
5. ✅ Verifica cooldown >= 3s → ✅ PASS (primera vez)
6. ✅ Ejecuta `sendSMS()` → Crea WorkManager task
7. ✅ WorkManager llama `GestureActionWorker.doWork()`
8. ✅ `handleSMS()` envía mensaje a 900710184
9. ✅ Resultado: Message sent OR queued for retry

**Resultado final**: Contacto 900710184 recibe SMS dentro de segundos

---

**Entregable cumplido**: ✅ Dado un gesto confirmado (confianza 85% + cooldown 3s), el contacto 900710184 recibe mensaje vía SMS, WhatsApp o notificación.
