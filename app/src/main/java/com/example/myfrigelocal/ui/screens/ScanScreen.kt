package com.example.myfrigelocal.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfrigelocal.navigation.BottomNavRoute
import com.example.myfrigelocal.navigation.ScanNav
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import com.example.myfrigelocal.data.scan.simulatedReceiptUiModel
import com.example.myfrigelocal.data.scan.toJson
import com.example.myfrigelocal.viewmodel.ScanOperationState
import com.example.myfrigelocal.viewmodel.ScanViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
    navController: NavController,
    lifecycleOwner: LifecycleOwner,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val scope = rememberCoroutineScope()
    val scanViewModel: ScanViewModel =
        viewModel(factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application))
    val operationState by scanViewModel.operationState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(ScanTab.Ingredient) }
    var scanState by rememberSaveable { mutableStateOf(ScanState.IDLE) }
    var lensFacing by rememberSaveable { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var lastSelectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var lastBarcodeRawValue by rememberSaveable { mutableStateOf<String?>(null) }
    var receiptItems by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var hasNavigatedToResult by rememberSaveable { mutableStateOf(false) }
    var previewEnabled by rememberSaveable { mutableStateOf(true) }
    val currentTab by rememberUpdatedState(selectedTab)
    var captureRequestToken by rememberSaveable { mutableStateOf(0L) }

    // Bounds tracking for crop mapping.
    var previewBounds by rememberSaveable { mutableStateOf<RectF?>(null) }
    var frameBounds by rememberSaveable { mutableStateOf<RectF?>(null) }

    // Kill camera preview immediately when navigating away to avoid "last frame" flashing.
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            previewEnabled = destination.route == BottomNavRoute.Scan.route
        }
        navController.addOnDestinationChangedListener(listener)
        // Initialize on first composition.
        previewEnabled = navController.currentDestination?.route == BottomNavRoute.Scan.route
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    // Reset signal coming back from the result screen (Cancel).
    val scanResetFlow: StateFlow<Boolean>? =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow(ScanNav.keyReset, false)
    val scanReset by (scanResetFlow?.collectAsState(initial = false) ?: rememberSaveable { mutableStateOf(false) })

    LaunchedEffect(scanReset) {
        if (scanReset) {
            // Important: clear SUCCESS so we don't auto-navigate again.
            scanState = ScanState.IDLE
            hasNavigatedToResult = false
            lastBarcodeRawValue = null
            receiptItems = emptyList()
            scanViewModel.resetOperation()
            navController.currentBackStackEntry?.savedStateHandle?.set(ScanNav.keyReset, false)
        }
    }

    // Defensive reset: any time the Scan composable (re)enters composition, start from a
    // clean state so users never see a stale SUCCESS/LOADING overlay after navigating away.
    LaunchedEffect(Unit) {
        scanState = ScanState.IDLE
        hasNavigatedToResult = false
        lastBarcodeRawValue = null
        lastSelectedImageUri = null
        receiptItems = emptyList()
        navController.currentBackStackEntry?.savedStateHandle?.apply {
            remove<String>(ScanNav.keyScanResultJson)
            remove<String>(ScanNav.keyImageUri)
            remove<String>(ScanNav.keyBarcodeValue)
            remove<ArrayList<String>>(ScanNav.keyReceiptItems)
            remove<Int>(ScanNav.keyReceiptIndex)
            set(ScanNav.keyReset, false)
        }
        scanViewModel.resetOperation()
    }

    LaunchedEffect(operationState) {
        when (val s = operationState) {
            is ScanOperationState.Success -> {
                lastSelectedImageUri = s.result.localPreviewImageUri
                receiptItems = emptyList()
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    ScanNav.keyScanResultJson,
                    s.result.toJson(),
                )
                navController.currentBackStackEntry?.savedStateHandle?.set(ScanNav.keyReceiptIndex, 0)
                scanState = ScanState.SUCCESS
                scanViewModel.acknowledgeSuccess()
            }
            is ScanOperationState.Error -> {
                scanState = ScanState.IDLE
                context.showShortToast(s.message)
                scanViewModel.acknowledgeError()
            }
            else -> Unit
        }
    }

    val requestCameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // If denied, we still show the UI, but the preview will show a placeholder.
            // (No real scanning/processing required per spec.)
            if (!granted) scanState = ScanState.IDLE
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            val originalBitmap = ScanImageCropper.loadBitmapFromUri(context, uri)
            val imageUriStr =
                if (originalBitmap != null && previewBounds != null && frameBounds != null) {
                    val cropRect = ScanImageCropper.calculateCropRectCenterCrop(
                        frameBoundsInWindowPx = frameBounds!!,
                        previewBoundsInWindowPx = previewBounds!!,
                        bitmapW = originalBitmap.width,
                        bitmapH = originalBitmap.height,
                    )
                    val cropped = ScanImageCropper.cropBitmapSafe(originalBitmap, cropRect)
                    val croppedUri = ScanImageCropper.saveJpegToInternal(context, cropped, prefix = "cropped")
                    val (targetW, targetH, prefix) =
                        when (currentTab) {
                            ScanTab.Receipt -> Triple(640, 1024, "resized_640x1024")
                            ScanTab.Ingredient -> Triple(1024, 1024, "resized_1024")
                        }
                    val resized = ScanImageCropper.resize(cropped, targetW = targetW, targetH = targetH)
                    val resizedUri = ScanImageCropper.saveJpegToInternal(context, resized, prefix = prefix)
                    ScanImageCropper.logDebug(
                        ScanImageCropper.CropDebug(
                            bitmapW = originalBitmap.width,
                            bitmapH = originalBitmap.height,
                            frame = frameBounds!!,
                            preview = previewBounds!!,
                            crop = cropRect,
                            croppedW = cropped.width,
                            croppedH = cropped.height,
                            resizedW = resized.width,
                            resizedH = resized.height,
                            croppedSavedPath = croppedUri.path,
                            resizedSavedPath = resizedUri.path,
                        ),
                    )
                    resizedUri.toString()
                } else {
                    Log.d(
                        "ScanCrop",
                        "Bounds missing; skipping crop. preview=$previewBounds frame=$frameBounds bmp=${originalBitmap?.width}x${originalBitmap?.height}",
                    )
                    uri.toString()
                }
            lastBarcodeRawValue = null
            scope.launch {
                scanState = ScanState.LOADING
                when (currentTab) {
                    ScanTab.Ingredient ->
                        scanViewModel.requestIngredientScan(Uri.parse(imageUriStr), imageUriStr)
                    ScanTab.Receipt ->
                        scanViewModel.requestReceiptScan(Uri.parse(imageUriStr), imageUriStr)
                }
            }
        }

    LaunchedEffect(Unit) {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Success → auto navigate to result screen after a short delay.
    LaunchedEffect(scanState) {
        if (scanState == ScanState.SUCCESS && !hasNavigatedToResult) {
            hasNavigatedToResult = true
            delay(1200)
            navController.currentBackStackEntry?.savedStateHandle?.set(
                ScanNav.keyImageUri,
                lastSelectedImageUri,
            )
            navController.currentBackStackEntry?.savedStateHandle?.set(
                ScanNav.keyBarcodeValue,
                lastBarcodeRawValue,
            )
            navController.currentBackStackEntry?.savedStateHandle?.set(
                ScanNav.keyReceiptItems,
                ArrayList(receiptItems),
            )
            navController.currentBackStackEntry?.savedStateHandle?.set(
                ScanNav.keyReceiptIndex,
                0,
            )
            navController.navigate(ScanNav.routeResult)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen camera preview background.
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            enabled = previewEnabled,
            analysisEnabled = selectedTab == ScanTab.Receipt && scanState == ScanState.SCANNING,
            captureRequestToken = captureRequestToken,
            lensFacing = lensFacing,
            lifecycleOwner = lifecycleOwner,
            onBarcodeRawValue = { raw ->
                // Only accept detections while actively scanning.
                if (selectedTab == ScanTab.Receipt && scanState == ScanState.SCANNING) {
                    lastSelectedImageUri = null
                    lastBarcodeRawValue = raw
                    scope.launch {
                        scanState = ScanState.LOADING
                        receiptItems = emptyList()
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            ScanNav.keyScanResultJson,
                            simulatedReceiptUiModel(null).toJson(),
                        )
                        navController.currentBackStackEntry?.savedStateHandle?.set(ScanNav.keyReceiptIndex, 0)
                        lastSelectedImageUri = null
                        scanState = ScanState.SUCCESS
                    }
                }
            },
            onPhotoUri = { photoUriString ->
                val originalUri = Uri.parse(photoUriString)
                val originalBitmap = ScanImageCropper.loadBitmapFromUri(context, originalUri)
                val imageUriStr =
                    if (originalBitmap != null && previewBounds != null && frameBounds != null) {
                        val cropRect = ScanImageCropper.calculateCropRectCenterCrop(
                            frameBoundsInWindowPx = frameBounds!!,
                            previewBoundsInWindowPx = previewBounds!!,
                            bitmapW = originalBitmap.width,
                            bitmapH = originalBitmap.height,
                        )
                        val cropped = ScanImageCropper.cropBitmapSafe(originalBitmap, cropRect)
                        val croppedUri = ScanImageCropper.saveJpegToInternal(context, cropped, prefix = "cropped")
                        val (targetW, targetH, prefix) =
                            when (selectedTab) {
                                ScanTab.Receipt -> Triple(640, 1024, "resized_640x1024")
                                ScanTab.Ingredient -> Triple(1024, 1024, "resized_1024")
                            }
                        val resized = ScanImageCropper.resize(cropped, targetW = targetW, targetH = targetH)
                        val resizedUri = ScanImageCropper.saveJpegToInternal(context, resized, prefix = prefix)
                        ScanImageCropper.logDebug(
                            ScanImageCropper.CropDebug(
                                bitmapW = originalBitmap.width,
                                bitmapH = originalBitmap.height,
                                frame = frameBounds!!,
                                preview = previewBounds!!,
                                crop = cropRect,
                                croppedW = cropped.width,
                                croppedH = cropped.height,
                                resizedW = resized.width,
                                resizedH = resized.height,
                                croppedSavedPath = croppedUri.path,
                                resizedSavedPath = resizedUri.path,
                            ),
                        )
                        resizedUri.toString()
                    } else {
                        Log.d("ScanCrop", "Bounds missing; skipping crop on capture.")
                        photoUriString
                    }
                lastBarcodeRawValue = null
                scope.launch {
                    scanState = ScanState.LOADING
                    when (selectedTab) {
                        ScanTab.Ingredient ->
                            scanViewModel.requestIngredientScan(Uri.parse(imageUriStr), imageUriStr)
                        ScanTab.Receipt ->
                            scanViewModel.requestReceiptScan(Uri.parse(imageUriStr), imageUriStr)
                    }
                }
            },
            onPreviewBoundsInWindow = { rect -> previewBounds = rect },
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // White top bar with light segmented control to match the app's light/green tone.
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ScanTopBar(
                    selectedTab = selectedTab,
                    onSelect = { tab -> selectedTab = tab },
                )
            }

            val guideText = when (selectedTab) {
                ScanTab.Ingredient -> "식재료를 프레임 안에 맞춰주세요"
                ScanTab.Receipt -> "영수증을 프레임 안에 맞춰주세요"
            }

            // Middle section (frame ONLY, centered, on top of camera preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (scanState == ScanState.SUCCESS) {
                    ScanSuccessOverlay()
                } else {
                    when (selectedTab) {
                        ScanTab.Ingredient -> ScanFrameBox(
                            frameStyle = FrameStyle.Corners,
                            widthFraction = 0.74f,
                            aspectRatio = 1f,
                            maxHeightFraction = 0.94f,
                            onFrameBoundsInWindow = { rect -> frameBounds = rect },
                        )

                        ScanTab.Receipt -> ScanFrameBox(
                            frameStyle = FrameStyle.RoundedRect,
                            widthFraction = 0.86f,
                            aspectRatio = 1f / 1.6f,
                            maxHeightFraction = 0.94f,
                            onFrameBoundsInWindow = { rect -> frameBounds = rect },
                        )
                    }
                }
            }

            // Bottom section: white surface with guide text + shutter + gallery.
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = guideText,
                        color = Color(0xFF0F172A),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 520.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScanBottomSection(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTab = selectedTab,
                        controlsEnabled = scanState != ScanState.LOADING,
                        onPrimaryAction = {
                            when (selectedTab) {
                                ScanTab.Ingredient -> {
                                    scanState = ScanState.SCANNING
                                    lastBarcodeRawValue = null
                                    receiptItems = emptyList()
                                    captureRequestToken = System.currentTimeMillis()
                                }
                                ScanTab.Receipt -> {
                                    lastSelectedImageUri = null
                                    lastBarcodeRawValue = null
                                    receiptItems = emptyList()
                                    scanState = ScanState.SCANNING
                                    captureRequestToken = System.currentTimeMillis()
                                }
                            }
                        },
                        onPickFromGallery = { galleryLauncher.launch("image/*") },
                    )
                }
            }
        }

        if (scanState == ScanState.LOADING || operationState is ScanOperationState.Loading) {
            ScanAiRecognitionLoadingOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f),
            )
        }
    }
}

enum class ScanState {
    IDLE,
    /** Camera/barcode active; receipt barcode analysis uses this. */
    SCANNING,
    /** AI recognition in progress (Scan API or local simulation). */
    LOADING,
    SUCCESS,
}

private val PrimaryGreen = Color(0xFF00C853)

private enum class ScanTab { Ingredient, Receipt }

private fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

@Composable
private fun ScanTopBar(
    selectedTab: ScanTab,
    onSelect: (ScanTab) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        ScanTabSegmented(
            selectedTab = selectedTab,
            onSelect = onSelect,
            modifier = Modifier.widthIn(max = 480.dp),
        )
    }
}

@Composable
private fun ScanTabSegmented(
    selectedTab: ScanTab,
    onSelect: (ScanTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFF1F5F9),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            SegmentChip(
                modifier = Modifier.weight(1f),
                selected = selectedTab == ScanTab.Ingredient,
                icon = Icons.Outlined.PhotoCamera,
                label = "식재료",
                onClick = { onSelect(ScanTab.Ingredient) },
            )
            SegmentChip(
                modifier = Modifier.weight(1f),
                selected = selectedTab == ScanTab.Receipt,
                icon = Icons.Filled.Receipt,
                label = "영수증",
                onClick = { onSelect(ScanTab.Receipt) },
            )
        }
    }
}

@Composable
private fun SegmentChip(
    modifier: Modifier,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val bg = if (selected) PrimaryGreen else Color.Transparent
    val fg = if (selected) Color.White else Color(0xFF64748B)
    Surface(
        shape = CircleShape,
        color = bg,
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = fg,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private enum class FrameStyle { Corners, RoundedRect }

@Composable
private fun ScanFrameBox(
    frameStyle: FrameStyle,
    widthFraction: Float,
    aspectRatio: Float,
    maxHeightFraction: Float,
    onFrameBoundsInWindow: (RectF) -> Unit,
) {
    // Make the frame responsive in BOTH dimensions.
    // We start from a width fraction, but clamp the resulting height to the available maxHeight
    // so the frame never visually collides with the bottom section on smaller screens.
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val maxW: Dp = maxWidth
        val maxH: Dp = maxHeight

        // Desired size from width fraction.
        var frameW: Dp = maxW * widthFraction
        var frameH: Dp = frameW / aspectRatio

        // Clamp by height (leave a little breathing room).
        val maxFrameH = maxH * maxHeightFraction
        if (frameH > maxFrameH) {
            frameH = maxFrameH
            frameW = frameH * aspectRatio
        }

        val frameModifier = Modifier
            .size(frameW, frameH)
            .onGloballyPositioned { coordinates ->
                val r = coordinates.boundsInWindow()
                onFrameBoundsInWindow(RectF(r.left, r.top, r.right, r.bottom))
            }

        when (frameStyle) {
            FrameStyle.Corners -> CornerFrame(modifier = frameModifier)
            FrameStyle.RoundedRect -> RoundedRectFrame(modifier = frameModifier)
        }
    }
}

@Composable
private fun CornerFrame(modifier: Modifier = Modifier) {
    // Clean, minimal L-shaped corners with rounded stroke ends.
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cornerLen = minOf(w, h) * 0.14f
        val stroke = 3.dp.toPx()

        drawCornerL(0f, 0f, cornerLen, stroke, isTopLeft = true)
        drawCornerL(w, 0f, cornerLen, stroke, isTopRight = true)
        drawCornerL(0f, h, cornerLen, stroke, isBottomLeft = true)
        drawCornerL(w, h, cornerLen, stroke, isBottomRight = true)
    }
}

private fun DrawScope.drawCornerL(
    anchorX: Float,
    anchorY: Float,
    length: Float,
    strokeWidth: Float,
    isTopLeft: Boolean = false,
    isTopRight: Boolean = false,
    isBottomLeft: Boolean = false,
    isBottomRight: Boolean = false,
) {
    val xDir = if (isTopLeft || isBottomLeft) 1f else -1f
    val yDir = if (isTopLeft || isTopRight) 1f else -1f
    drawLine(
        color = PrimaryGreen,
        start = Offset(anchorX, anchorY),
        end = Offset(anchorX + xDir * length, anchorY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = PrimaryGreen,
        start = Offset(anchorX, anchorY),
        end = Offset(anchorX, anchorY + yDir * length),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

@Composable
private fun RoundedRectFrame(modifier: Modifier = Modifier) {
    // Simple rounded rectangle border. No animation.
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(width = 2.dp, color = PrimaryGreen, shape = RoundedCornerShape(20.dp)),
    )
}

@Composable
private fun ScanAiRecognitionLoadingOverlay(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color(0xCC0B1220)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                color = PrimaryGreen,
                trackColor = PrimaryGreen.copy(alpha = 0.22f),
                strokeWidth = 5.dp,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "AI 인식중",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "잠시 기다려 주세요.",
                color = Color(0xFFE5E7EB),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ScanSuccessOverlay() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text(
            text = "스캔 완료!",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ScanBottomSection(
    modifier: Modifier = Modifier,
    selectedTab: ScanTab,
    controlsEnabled: Boolean = true,
    onPrimaryAction: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    val innerIcon = when (selectedTab) {
        ScanTab.Ingredient -> Icons.Outlined.PhotoCamera
        ScanTab.Receipt -> Icons.Outlined.QrCodeScanner
    }

    Box(modifier = modifier) {
        ShutterButton(
            enabled = controlsEnabled,
            innerIcon = innerIcon,
            onClick = onPrimaryAction,
            modifier = Modifier.align(Alignment.Center),
        )
        FloatingGalleryButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            enabled = controlsEnabled,
            onClick = onPickFromGallery,
        )
    }
}

@Composable
private fun FloatingGalleryButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = "Gallery",
            tint = Color(0xFF475569),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ShutterButton(
    enabled: Boolean = true,
    innerIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val innerBg = if (enabled) PrimaryGreen else PrimaryGreen.copy(alpha = 0.45f)
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFFDCFCE7))
            .border(width = 2.dp, color = PrimaryGreen.copy(alpha = 0.4f), shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(innerBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = innerIcon,
                contentDescription = "Capture",
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}
