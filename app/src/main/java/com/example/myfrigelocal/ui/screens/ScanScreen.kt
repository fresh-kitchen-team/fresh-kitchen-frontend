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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FlipCameraAndroid
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var selectedTab by rememberSaveable { mutableStateOf(ScanTab.Camera) }
    var scanState by rememberSaveable { mutableStateOf(ScanState.IDLE) }
    var lensFacing by rememberSaveable { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var lastSelectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var hasNavigatedToResult by rememberSaveable { mutableStateOf(false) }
    var previewEnabled by rememberSaveable { mutableStateOf(true) }

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
            if (uri != null) {
                lastSelectedImageUri = uri.toString()
                scanState = ScanState.SCANNING
                // Simulate processing.
                scanState = ScanState.SUCCESS
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
            navController.navigate(ScanNav.routeResult)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen camera preview background.
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            enabled = previewEnabled,
            lensFacing = lensFacing,
            lifecycleOwner = lifecycleOwner,
        )

        // Dark vignette overlay (helps match design + legibility).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99010B17)),
        )

        Column(modifier = Modifier.fillMaxSize()) {
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
                onSelect = { tab ->
                    selectedTab = tab
                    if (tab == ScanTab.Gallery) {
                        // Launch system picker immediately, as requested.
                        galleryLauncher.launch("image/*")
                    }
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (selectedTab) {
                    ScanTab.Camera -> CameraGuideOverlay(
                        headline = "음식물을 프레임 안에 맞춰주세요",
                        frameStyle = FrameStyle.Corners,
                    )

                    ScanTab.Gallery -> CameraGuideOverlay(
                        headline = "갤러리에서 이미지를 선택해주세요",
                        frameStyle = FrameStyle.Corners,
                    )

                    ScanTab.Barcode -> CameraGuideOverlay(
                        headline = "바코드를 프레임 안에 맞춰주세요",
                        frameStyle = FrameStyle.RoundedRect,
                    )
                }

                if (scanState == ScanState.SUCCESS) {
                    ScanSuccessOverlay()
                }

                // Bottom controls (positioned like the references).
                ScanBottomControls(
                    // Keep controls above the bottom navigation bar so it doesn't block tab clicks.
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                    selectedTab = selectedTab,
                    onCapture = {
                        scanState = ScanState.SCANNING
                        // No real processing per spec; simulate instant success.
                        scanState = ScanState.SUCCESS
                    },
                    onPickFromGallery = { galleryLauncher.launch("image/*") },
                    onSwitchCamera = {
                        lensFacing =
                            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                    },
                    onBarcodeScan = {
                        scanState = ScanState.SCANNING
                        scanState = ScanState.SUCCESS
                    },
                )
            }
        }
    }
}

enum class ScanState {
    IDLE,
    SCANNING,
    SUCCESS,
}

private enum class ScanTab { Camera, Gallery, Barcode }

private val PrimaryGreen = Color(0xFF00C853)

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
                selected = selectedTab == ScanTab.Camera,
                icon = Icons.Outlined.PhotoCamera,
                label = "촬영하기",
                onClick = { onSelect(ScanTab.Camera) },
            )
            TabItem(
                modifier = Modifier.weight(1f),
                selected = selectedTab == ScanTab.Gallery,
                icon = Icons.Outlined.Image,
                label = "갤러리",
                onClick = { onSelect(ScanTab.Gallery) },
            )
            TabItem(
                modifier = Modifier.weight(1f),
                selected = selectedTab == ScanTab.Barcode,
                icon = Icons.Outlined.QrCodeScanner,
                label = "바코드",
                onClick = { onSelect(ScanTab.Barcode) },
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
private fun CameraGuideOverlay(
    headline: String,
    frameStyle: FrameStyle,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = headline,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp),
        )

        when (frameStyle) {
            FrameStyle.Corners -> CornerFrame(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 36.dp)
                    .fillMaxWidth()
                    .height(340.dp),
            )

            FrameStyle.RoundedRect -> RoundedRectFrame(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 42.dp)
                    .fillMaxWidth()
                    .height(160.dp),
            )
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
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(110.dp))
        Text(
            text = "스캔 완료!",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(90.dp))

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
private fun ScanBottomControls(
    modifier: Modifier = Modifier,
    selectedTab: ScanTab,
    onCapture: () -> Unit,
    onPickFromGallery: () -> Unit,
    onSwitchCamera: () -> Unit,
    onBarcodeScan: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (selectedTab) {
            ScanTab.Camera -> {
                CaptureButton(onClick = onCapture)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "촬영 버튼을 눌러 스캔하세요",
                    color = Color(0xCCFFFFFF),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            ScanTab.Gallery -> {
                Spacer(modifier = Modifier.height(72.dp))
                Text(
                    text = "갤러리에서 사진을 선택하세요",
                    color = Color(0xCCFFFFFF),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            ScanTab.Barcode -> {
                ScanButton(onClick = onBarcodeScan)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "스캔 버튼을 눌러주세요",
                    color = Color(0xCCFFFFFF),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
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
