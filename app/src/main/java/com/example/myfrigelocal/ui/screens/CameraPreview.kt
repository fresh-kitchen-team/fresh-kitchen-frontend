package com.example.myfrigelocal.ui.screens

import android.annotation.SuppressLint
import android.media.Image
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.util.concurrent.Executor

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    analysisEnabled: Boolean = false,
    captureRequestToken: Long = 0L,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    lifecycleOwner: LifecycleOwner,
    onBarcodeRawValue: ((String) -> Unit)? = null,
    onPhotoUri: ((String) -> Unit)? = null,
    onPreviewBoundsInWindow: ((android.graphics.RectF) -> Unit)? = null,
) {
    val context = LocalContext.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor: Executor = remember { ContextCompat.getMainExecutor(context) }
    val captureExecutor: Executor = remember { ContextCompat.getMainExecutor(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
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

    // Bind/unbind based on visibility (`enabled`), analysis toggle, and lens choice.
    LaunchedEffect(cameraProvider, enabled, analysisEnabled, lensFacing, lifecycleOwner, onBarcodeRawValue) {
        val provider = cameraProvider ?: return@LaunchedEffect
        if (!enabled) {
            runCatching { provider.unbindAll() }
            return@LaunchedEffect
        }

        val previewUseCase = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysisUseCase =
            if (analysisEnabled && onBarcodeRawValue != null) {
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(
                            analysisExecutor,
                            MlKitBarcodeAnalyzer(onBarcodeRawValue = onBarcodeRawValue),
                        )
                    }
            } else {
                null
            }
        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        runCatching {
            provider.unbindAll()
            if (analysisUseCase != null) {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    previewUseCase,
                    analysisUseCase,
                    imageCapture,
                )
            } else {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    previewUseCase,
                    imageCapture,
                )
            }
        }
    }

    LaunchedEffect(captureRequestToken, enabled, onPhotoUri) {
        if (!enabled) return@LaunchedEffect
        if (captureRequestToken == 0L) return@LaunchedEffect
        val callback = onPhotoUri ?: return@LaunchedEffect

        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        val file = File(dir, "capture_$captureRequestToken.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            output,
            captureExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    callback(file.toURI().toString())
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraPreview", "Capture failed", exception)
                }
            },
        )
    }

    Box(modifier = modifier) {
        if (enabled) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        onPreviewBoundsInWindow?.invoke(coordinates.boundsInWindow().toAndroidRectF())
                    },
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

private fun androidx.compose.ui.geometry.Rect.toAndroidRectF(): android.graphics.RectF =
    android.graphics.RectF(left, top, right, bottom)

private class MlKitBarcodeAnalyzer(
    private val onBarcodeRawValue: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build(),
    )

    @Volatile
    private var delivered = false

    override fun analyze(imageProxy: ImageProxy) {
        if (delivered) {
            imageProxy.close()
            return
        }

        val mediaImage: Image? = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (delivered) return@addOnSuccessListener
                val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue }
                if (!rawValue.isNullOrBlank()) {
                    delivered = true
                    onBarcodeRawValue(rawValue)
                }
            }
            .addOnFailureListener {
                // Ignore; UI-only flow.
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

