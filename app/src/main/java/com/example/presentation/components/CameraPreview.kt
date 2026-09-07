package com.example.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandTextSecondary

@Composable
fun CameraPreview(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasCameraPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    if (!hasCameraPermission) {
        Box(
            modifier = modifier.background(BrandBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera preview active",
                color = BrandTextSecondary
            )
        }
        return
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(context) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                cameraProvider = future.get()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error getting camera provider: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(cameraProvider, isFrontCamera, previewViewInstance) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val view = previewViewInstance ?: return@LaunchedEffect

        try {
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = view.surfaceProvider
            }
            val selector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error binding camera: ${e.message}")
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                // safely ignored
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                previewViewInstance = this
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
