package com.example.myfrigelocal.ui.screens

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    lifecycleOwner: LifecycleOwner,
) {
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Acquire ProcessCameraProvider once (listener is registered once).
    DisposableEffect(cameraProviderFuture) {
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            runCatching {
                cameraProvider = cameraProviderFuture.get()
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            // Best-effort cleanup without blocking.
            runCatching { cameraProvider?.unbindAll() }
        }
    }

    // Bind/unbind based on visibility (`enabled`) and lens choice.
    LaunchedEffect(cameraProvider, enabled, lensFacing, lifecycleOwner) {
        val provider = cameraProvider ?: return@LaunchedEffect
        if (!enabled) {
            runCatching { provider.unbindAll() }
            return@LaunchedEffect
        }

        val previewUseCase = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        runCatching {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                previewUseCase,
            )
        }
    }

    Box(modifier = modifier) {
        if (enabled) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { previewView },
            )
        } else {
            // When disabled, don't keep showing the last frame (prevents "flash" during navigation).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }

        // Fallback hint overlay (if preview is black due to permission/device constraints).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            // Keep subtle; design wants camera-first UI.
            Text(
                text = "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Transparent,
            )
        }
    }
}

