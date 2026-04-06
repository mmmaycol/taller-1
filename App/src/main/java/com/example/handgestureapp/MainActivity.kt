package com.example.handgestureapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.handgestureapp.databinding.ActivityMainBinding
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), HandLandmarkerHelper.LandmarkerListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraManager: CameraManager
    private var torchCameraId: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(this, "Se necesita permiso de cámara", Toast.LENGTH_LONG).show()
    }

    private val requestCallPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Permiso CALL_PHONE necesario para realizar llamadas", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar linterna
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        torchCameraId = cameraManager.cameraIdList[0] // cámara trasera

        cameraExecutor = Executors.newSingleThreadExecutor()
        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            handLandmarkerHelperListener = this
        )

        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        handLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera = true)
                    }
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResults(resultBundle: HandLandmarkerResult) {
        runOnUiThread {
            binding.overlayView.setResults(resultBundle)
            val gesture = GestureClassifier.classify(resultBundle)
            binding.tvGesture.text = gesture

            // Procesar gesto con filtro de confianza, cooldown y acciones
            GestureActionHandler.processGesture(this, resultBundle, gesture)

            // Enciende linterna si detecta "Tres", apaga con cualquier otro gesto
            try {
                if (gesture.contains("Tres")) {
                    cameraManager.setTorchMode(torchCameraId, true)
                } else {
                    cameraManager.setTorchMode(torchCameraId, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Apagar linterna al cerrar la app
        try {
            cameraManager.setTorchMode(torchCameraId, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handLandmarkerHelper.close()
        cameraExecutor.shutdown()
    }
}