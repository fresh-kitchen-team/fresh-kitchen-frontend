package com.example.myfrigelocal.ui.screens.chat

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraCaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFrigeLocalTheme {
                CameraCaptureRoute(
                    onResult = { uri ->
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(RESULT_IMAGE_URI, uri.toString()),
                        )
                        finish()
                    },
                    onClose = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                )
            }
        }
    }

    companion object {
        const val RESULT_IMAGE_URI = "result_image_uri"
    }
}

@Composable
private fun CameraCaptureRoute(
    onResult: (Uri) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA,
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    var permissionGranted by remember { mutableStateOf(hasPermission) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionGranted = granted
            if (!granted) onClose()
        },
    )

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!permissionGranted) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {}
        return
    }

    CameraCaptureScreen(
        onResult = onResult,
        onClose = onClose,
    )
}

@Composable
private fun CameraCaptureScreen(
    onResult: (Uri) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = remember { Preview.Builder().build() }
    val capture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var imageCapture: ImageCapture? by remember { mutableStateOf(capture) }
    var bindError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lifecycleOwner) {
        try {
            val provider = context.getCameraProvider()
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                capture,
            )

            imageCapture = capture
        } catch (t: Throwable) {
            bindError = t.message ?: "Camera bind failed"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Lifecycle binding will unbind automatically; no-op.
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { pv ->
                    pv.scaleType = PreviewView.ScaleType.FILL_CENTER
                    preview.setSurfaceProvider(pv.surfaceProvider)
                }
            },
            update = { pv ->
                preview.setSurfaceProvider(pv.surfaceProvider)
            },
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "촬영",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp)) // symmetry
        }

        if (bindError != null) {
            Text(
                text = bindError ?: "",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                color = Color.White,
            )
        }

        // Capture button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {
                            val capture = imageCapture ?: return@IconButton
                            val file = context.createTempJpeg()
                            val output = ImageCapture.OutputFileOptions.Builder(file).build()
                            capture.takePicture(
                                output,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file,
                                        )
                                        onResult(uri)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        bindError = exception.message ?: "Capture failed"
                                    }
                                },
                            )
                        },
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = CircleShape,
                            color = BottomNavSelected,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Capture",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun android.content.Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                try {
                    cont.resume(future.get())
                } catch (t: Throwable) {
                    cont.resumeWithException(t)
                }
            },
            ContextCompat.getMainExecutor(this),
        )
    }

private fun android.content.Context.createTempJpeg(): File {
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val dir = File(cacheDir, "camera").apply { mkdirs() }
    return File(dir, "IMG_$stamp.jpg")
}

