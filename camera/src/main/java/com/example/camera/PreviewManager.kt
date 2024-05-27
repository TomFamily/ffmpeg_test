package com.example.camera

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object PreviewManager {
    private const val TAG = "PreviewManager"
    @Volatile
    private var previewing: Boolean = false
    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    @Synchronized
    fun valve(lf: LifecycleOwner, preview: PreviewView, context: Context) {
        Log.d(TAG, "valve: $previewing")
        if (previewing) {
            onStop()
        } else {
             onStart(lf, preview, context)
        }
        previewing = !previewing
    }

    @Synchronized
    fun onStart(lf: LifecycleOwner, viewFinder: PreviewView, context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val executor = Executors.newFixedThreadPool(5)
        imageAnalysis.setAnalyzer(executor) {
        }

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lf, cameraSelector, preview
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    @Synchronized
    fun onStop() {
        cameraExecutor.shutdown()
    }
}