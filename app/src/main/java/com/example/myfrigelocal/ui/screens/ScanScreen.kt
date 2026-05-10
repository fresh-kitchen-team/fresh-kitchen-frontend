package com.example.myfrigelocal.ui.screens

import android.Manifest
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import com.example.myfrigelocal.navigation.BottomNavRoute
import com.example.myfrigelocal.navigation.ScanNav
import kotlinx.coroutines.delay

@Composable
fun ScanScreen(
    navController: NavController,
    lifecycleOwner: LifecycleOwner,
) {
    var selectedTab by rememberSaveable { mutableStateOf(ScanTab.Ingredient) }
    var scanState by rememberSaveable { mutableStateOf(ScanState.IDLE) }
    var lensFacing by rememberSaveable { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var lastSelectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var lastBarcodeRawValue by rememberSaveable { mutableStateOf<String?>(null) }
    var receiptItems by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var hasNavigatedToResult by rememberSaveable { mutableStateOf(false) }
    var previewEnabled by rememberSaveable { mutableStateOf(true) }
    val currentTab by rememberUpdatedState(selectedTab)

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
            navController.currentBackStackEntry?.savedStateHandle?.set(ScanNav.keyReset, false)
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
            lastSelectedImageUri = uri.toString()
            lastBarcodeRawValue = null
            // Same UX as receipt scan button: multi-item flow on Receipt tab; single on Ingredient.
            when (currentTab) {
                ScanTab.Receipt -> {
                    receiptItems = SimulatedReceiptItemNames
                    scanState = ScanState.SCANNING
                    scanState = ScanState.SUCCESS
                }
                ScanTab.Ingredient -> {
                    receiptItems = emptyList()
                    scanState = ScanState.SCANNING
                    scanState = ScanState.SUCCESS
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
            lensFacing = lensFacing,
            lifecycleOwner = lifecycleOwner,
            onBarcodeRawValue = { raw ->
                // Only accept detections while actively scanning.
                if (selectedTab == ScanTab.Receipt && scanState == ScanState.SCANNING) {
                    lastSelectedImageUri = null
                    lastBarcodeRawValue = raw
                    // Simulate receipt OCR: multiple detected items.
                    receiptItems = SimulatedReceiptItemNames
                    scanState = ScanState.SUCCESS
                }
            },
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top section (title + tabs)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                ScanTopBar(
                    onClose = {
                        // Scan is a bottom-tab destination; closing returns to Home.
                        navController.navigate("home") {
                            launchSingleTop = true
                            popUpTo("home") { inclusive = false }
                        }
                    },
                )
                ScanTabs(
                    selectedTab = selectedTab,
                    onSelect = { tab -> selectedTab = tab },
                )
            }

            val guideText = when (selectedTab) {
                ScanTab.Ingredient -> "식재료를 프레임 안에 맞춰주세요"
                ScanTab.Receipt -> "영수증을 프레임 안에 맞춰주세요"
            }

            // Middle section (frame ONLY, centered)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (scanState == ScanState.SUCCESS) {
                    // Keep success feedback out of the frame area (per requirement).
                    ScanSuccessOverlay()
                } else {
                    when (selectedTab) {
                        ScanTab.Ingredient -> ScanFrameBox(
                            frameStyle = FrameStyle.Corners,
                            widthFraction = 0.74f,
                            aspectRatio = 1f,
                            maxHeightFraction = 0.94f,
                        )

                        ScanTab.Receipt -> ScanFrameBox(
                            frameStyle = FrameStyle.RoundedRect,
                            widthFraction = 0.86f,
                            aspectRatio = 1f / 1.6f,
                            maxHeightFraction = 0.94f,
                        )
                    }
                }
            }

            // Guide section (same position/spacing for both tabs, never clipped)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 6.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = guideText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 520.dp),
                )
            }

            // Bottom section (guide text + primary button + floating gallery button)
            ScanBottomSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                selectedTab = selectedTab,
                onPrimaryAction = {
                    when (selectedTab) {
                        ScanTab.Ingredient -> {
                            scanState = ScanState.SCANNING
                            // No real processing per spec; simulate instant success.
                            lastBarcodeRawValue = null
                            receiptItems = emptyList()
                            scanState = ScanState.SUCCESS
                        }

                        ScanTab.Receipt -> {
                            lastSelectedImageUri = null
                            lastBarcodeRawValue = null
                            // Simulate receipt OCR: multiple detected items.
                            receiptItems = SimulatedReceiptItemNames
                            scanState = ScanState.SCANNING
                            // No real OCR yet; move forward immediately so UX doesn't look stuck.
                            scanState = ScanState.SUCCESS
                        }
                    }
                },
                onPickFromGallery = { galleryLauncher.launch("image/*") },
            )
        }
    }
}

enum class ScanState {
    IDLE,
    SCANNING,
    SUCCESS,
}

private enum class ScanTab { Ingredient, Receipt }

private val PrimaryGreen = Color(0xFF00C853)

/** Simulated OCR output for receipt flows (scan button / gallery / barcode). */
private val SimulatedReceiptItemNames = listOf("신선한 우유", "사과", "돼지고기")

@Composable
private fun ScanTopBar(
    onClose: () -> Unit,
) {
    Surface(
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable(onClick = onClose),
            )
            Text(
                text = "식재료 등록",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun ScanTabs(
    selectedTab: ScanTab,
    onSelect: (ScanTab) -> Unit,
) {
    Surface(
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            TabItem(
                modifier = Modifier.weight(1f),
                selected = selectedTab == ScanTab.Ingredient,
                icon = Icons.Outlined.PhotoCamera,
                label = "식재료 촬영",
                onClick = { onSelect(ScanTab.Ingredient) },
            )
            TabItem(
                modifier = Modifier.weight(1f),
                selected = selectedTab == ScanTab.Receipt,
                icon = Icons.Filled.Receipt,
                label = "영수증 촬영",
                onClick = { onSelect(ScanTab.Receipt) },
            )
        }
    }
}

@Composable
private fun TabItem(
    modifier: Modifier,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val tint = if (selected) PrimaryGreen else Color(0xFF9CA3AF)
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth()
                .background(if (selected) PrimaryGreen else Color.Transparent),
        )
    }
}

private enum class FrameStyle { Corners, RoundedRect }

@Composable
private fun ScanFrameBox(
    frameStyle: FrameStyle,
    widthFraction: Float,
    aspectRatio: Float,
    maxHeightFraction: Float,
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

        val frameModifier = Modifier.size(frameW, frameH)

        when (frameStyle) {
            FrameStyle.Corners -> CornerFrame(modifier = frameModifier)
            FrameStyle.RoundedRect -> RoundedRectFrame(modifier = frameModifier)
        }
    }
}

@Composable
private fun CornerFrame(modifier: Modifier = Modifier) {
    // Minimal corner-only frame, like the reference.
    Box(modifier = modifier) {
        val cornerSize = 44.dp
        val stroke = 4.dp

        // Top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerSize),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(stroke)
                    .background(PrimaryGreen),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(stroke)
                    .fillMaxSize()
                    .background(PrimaryGreen),
            )
        }
        // Top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth()
                    .height(stroke)
                    .background(PrimaryGreen),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(stroke)
                    .fillMaxSize()
                    .background(PrimaryGreen),
            )
        }
        // Bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(stroke)
                    .background(PrimaryGreen),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(stroke)
                    .fillMaxSize()
                    .background(PrimaryGreen),
            )
        }
        // Bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .height(stroke)
                    .background(PrimaryGreen),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(stroke)
                    .fillMaxSize()
                    .background(PrimaryGreen),
            )
        }
    }
}

@Composable
private fun RoundedRectFrame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(width = 3.dp, color = PrimaryGreen, shape = RoundedCornerShape(18.dp))
            .background(Color.Transparent),
    ) {
        // Empty: border is the frame.
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
    onPrimaryAction: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    val hintText = when (selectedTab) {
        ScanTab.Ingredient -> "촬영 버튼을 눌러 스캔하세요"
        ScanTab.Receipt -> "스캔 버튼을 눌러주세요"
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (selectedTab) {
                ScanTab.Ingredient -> CaptureButton(onClick = onPrimaryAction)
                ScanTab.Receipt -> ScanButton(onClick = onPrimaryAction)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hintText,
                color = Color(0xCCFFFFFF),
                style = MaterialTheme.typography.labelMedium,
            )
        }

        FloatingGalleryButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = onPickFromGallery,
        )
    }
}

@Composable
private fun FloatingGalleryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = "Gallery",
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(PrimaryGreen)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = "Capture",
            tint = Color(0xFF062115),
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun ScanButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(PrimaryGreen)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.QrCodeScanner,
            contentDescription = "Scan",
            tint = Color(0xFF062115),
            modifier = Modifier.size(28.dp),
        )
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
