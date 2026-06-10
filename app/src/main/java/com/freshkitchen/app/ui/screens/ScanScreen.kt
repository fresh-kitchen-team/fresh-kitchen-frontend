package com.freshkitchen.app.ui.screens

import android.Manifest
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.freshkitchen.app.data.scan.ScanSourceType
import com.freshkitchen.app.data.scan.simulatedReceiptUiModel
import com.freshkitchen.app.data.scan.toJson
import com.freshkitchen.app.navigation.BottomNavRoute
import com.freshkitchen.app.navigation.ScanNav
import com.freshkitchen.app.viewmodel.ScanOperationState
import com.freshkitchen.app.viewmodel.ScanViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 스캔 카메라 화면 탭. 결과 화면 복귀(returnTab) 매핑과도 연결됩니다. */
internal enum class ScanTab { Ingredient, Fridge, Receipt }

enum class ScanState {
    IDLE,

    /** 카메라/바코드 활성 — 영수증 바코드 분석에 사용. */
    SCANNING,

    /** AI 인식 진행 중 (Scan API 또는 로컬 시뮬레이션). */
    LOADING,
    SUCCESS,
}

private const val SCAN_SUCCESS_NAV_DELAY_MS = 1200L

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
    var showEmptyReceiptDialog by rememberSaveable { mutableStateOf(false) }
    var showEmptyFridgeDialog by rememberSaveable { mutableStateOf(false) }
    var previewEnabled by rememberSaveable { mutableStateOf(true) }
    val currentTab by rememberUpdatedState(selectedTab)
    var captureRequestToken by rememberSaveable { mutableStateOf(0L) }

    // 크롭 매핑용 bounds.
    var previewBounds by rememberSaveable { mutableStateOf<RectF?>(null) }
    var frameBounds by rememberSaveable { mutableStateOf<RectF?>(null) }

    /** LOADING 표시 후 탭별 스캔 API 호출 (갤러리/카메라 공통). */
    fun startScanForTab(tab: ScanTab, uploadUriString: String) {
        scanState = ScanState.LOADING
        val uri = Uri.parse(uploadUriString)
        when (tab) {
            ScanTab.Fridge -> scanViewModel.requestFridgeScan(uri, uploadUriString)
            ScanTab.Ingredient -> scanViewModel.requestIngredientScan(uri, uploadUriString)
            ScanTab.Receipt -> scanViewModel.requestReceiptScan(uri, uploadUriString)
        }
    }

    // 다른 화면으로 이동하면 카메라 프리뷰를 즉시 끄고 "마지막 프레임" 잔상을 막는다.
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            previewEnabled = destination.route == BottomNavRoute.Scan.route
        }
        navController.addOnDestinationChangedListener(listener)
        previewEnabled = navController.currentDestination?.route == BottomNavRoute.Scan.route
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    // 결과 화면에서 취소(reset)하고 돌아왔을 때의 신호.
    val scanResetFlow: StateFlow<Boolean>? =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow(ScanNav.keyReset, false)
    val scanReset by (scanResetFlow?.collectAsState(initial = false) ?: rememberSaveable { mutableStateOf(false) })

    val scanResetAtFlow: StateFlow<Long>? =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow(ScanNav.keyResetAt, 0L)
    val scanResetAt by (scanResetAtFlow?.collectAsState(initial = 0L) ?: rememberSaveable { mutableStateOf(0L) })

    fun applyScanReset(returnTab: String?) {
        // 중요: SUCCESS 상태를 지워 결과 화면으로 다시 자동 이동하지 않도록 한다.
        scanState = ScanState.IDLE
        hasNavigatedToResult = false
        lastBarcodeRawValue = null
        lastSelectedImageUri = null
        receiptItems = emptyList()
        captureRequestToken = 0L
        scanViewModel.resetOperation()

        selectedTab = returnTab.toScanTabOrDefault()

        navController.currentBackStackEntry?.savedStateHandle?.apply {
            remove<String>(ScanNav.keyScanResultJson)
            remove<String>(ScanNav.keyImageUri)
            remove<String>(ScanNav.keyBarcodeValue)
            remove<ArrayList<String>>(ScanNav.keyReceiptItems)
            remove<Int>(ScanNav.keyReceiptIndex)
            remove<String>(ScanNav.keyReturnTab)
            set(ScanNav.keyReset, false)
            set(ScanNav.keyResetAt, 0L)
        }
    }

    LaunchedEffect(scanReset) {
        if (scanReset) {
            applyScanReset(navController.currentBackStackEntry?.savedStateHandle?.get(ScanNav.keyReturnTab))
        }
    }

    LaunchedEffect(scanResetAt) {
        if (scanResetAt > 0L) {
            applyScanReset(navController.currentBackStackEntry?.savedStateHandle?.get(ScanNav.keyReturnTab))
        }
    }

    // 방어적 초기화: Scan 화면이 다시 구성될 때 stale SUCCESS/LOADING 오버레이가 보이지 않도록 정리.
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
                // 영수증/냉장고는 인식 결과가 비면 결과 화면 대신 "다시 스캔" 안내.
                val emptyResultTab = emptyResultTabOrNull(currentTab, s.result.sourceType, s.result.items.isEmpty())
                if (emptyResultTab != null) {
                    scanState = ScanState.SCANNING
                    hasNavigatedToResult = false
                    lastSelectedImageUri = null
                    if (emptyResultTab == ScanTab.Receipt) receiptItems = emptyList()
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        remove<String>(ScanNav.keyScanResultJson)
                        remove<String>(ScanNav.keyImageUri)
                    }
                    when (emptyResultTab) {
                        ScanTab.Receipt -> showEmptyReceiptDialog = true
                        ScanTab.Fridge -> showEmptyFridgeDialog = true
                        else -> Unit
                    }
                    scanViewModel.acknowledgeSuccess()
                    return@LaunchedEffect
                }
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
            if (!granted) scanState = ScanState.IDLE
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            val originalBitmap = ScanImageCropper.loadBitmapFromUri(context, uri)
            val imageUriStr =
                context.prepareImageForScanUpload(
                    originalBitmap = originalBitmap,
                    fallbackUri = uri,
                    tab = currentTab,
                    previewBounds = previewBounds,
                    frameBounds = frameBounds,
                )
            lastBarcodeRawValue = null
            scope.launch { startScanForTab(currentTab, imageUriStr) }
        }

    LaunchedEffect(Unit) {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Success → 짧은 지연 후 결과 화면으로 자동 이동.
    LaunchedEffect(scanState) {
        if (scanState == ScanState.SUCCESS && !hasNavigatedToResult) {
            hasNavigatedToResult = true
            delay(SCAN_SUCCESS_NAV_DELAY_MS)
            navController.currentBackStackEntry?.savedStateHandle?.apply {
                set(ScanNav.keyImageUri, lastSelectedImageUri)
                set(ScanNav.keyBarcodeValue, lastBarcodeRawValue)
                set(ScanNav.keyReceiptItems, ArrayList(receiptItems))
                set(ScanNav.keyReceiptIndex, 0)
            }
            navController.navigate(ScanNav.routeResult)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 전체 화면 카메라 프리뷰 (배경).
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            enabled = previewEnabled,
            analysisEnabled = selectedTab == ScanTab.Receipt && scanState == ScanState.SCANNING,
            captureRequestToken = captureRequestToken,
            lensFacing = lensFacing,
            lifecycleOwner = lifecycleOwner,
            onBarcodeRawValue = { raw ->
                // 실제 스캔 중일 때만 바코드 인식 결과를 수용.
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
                    context.prepareImageForScanUpload(
                        originalBitmap = originalBitmap,
                        fallbackUri = originalUri,
                        tab = selectedTab,
                        previewBounds = previewBounds,
                        frameBounds = frameBounds,
                    )
                lastBarcodeRawValue = null
                scope.launch { startScanForTab(selectedTab, imageUriStr) }
            },
            onPreviewBoundsInWindow = { rect -> previewBounds = rect },
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 앱 톤에 맞춘 밝은 상단 세그먼트 탭.
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
                ScanTab.Fridge -> "냉장고 내부 전체가 보이게 촬영해주세요"
                ScanTab.Receipt -> "영수증을 프레임 안에 맞춰주세요"
            }
            val guideSubtext = when (selectedTab) {
                ScanTab.Fridge -> "문을 열고 선반 전체가 프레임 안에 들어오게 맞춰주세요"
                else -> null
            }

            // 가운데 영역: 카메라 위에 프레임 가이드만 표시 (탭 무관 동일 높이 유지).
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
                            frameStyle = ScanFrameStyle.Corners,
                            widthFraction = 0.74f,
                            aspectRatio = 1f,
                            maxHeightFraction = 0.94f,
                            onFrameBoundsInWindow = { rect -> frameBounds = rect },
                        )

                        ScanTab.Fridge -> ScanFrameBox(
                            frameStyle = ScanFrameStyle.FridgeInterior,
                            widthFraction = 0.88f,
                            aspectRatio = 0.72f,
                            maxHeightFraction = 0.94f,
                            onFrameBoundsInWindow = { rect -> frameBounds = rect },
                        )

                        ScanTab.Receipt -> ScanFrameBox(
                            frameStyle = ScanFrameStyle.RoundedRect,
                            widthFraction = 0.86f,
                            aspectRatio = 1f / 1.6f,
                            maxHeightFraction = 0.94f,
                            onFrameBoundsInWindow = { rect -> frameBounds = rect },
                        )
                    }
                }
            }

            // 하단 영역: 고정 높이로 모든 탭에서 카메라 영역 크기를 동일하게 유지.
            Surface(
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = guideText,
                            color = Color(0xFF0F172A),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 520.dp),
                        )
                        if (guideSubtext != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = guideSubtext,
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.widthIn(max = 520.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ScanBottomSection(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTab = selectedTab,
                        controlsEnabled = scanState != ScanState.LOADING,
                        onPrimaryAction = {
                            lastBarcodeRawValue = null
                            receiptItems = emptyList()
                            if (selectedTab == ScanTab.Receipt) lastSelectedImageUri = null
                            scanState = ScanState.SCANNING
                            captureRequestToken = System.currentTimeMillis()
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

    if (showEmptyReceiptDialog) {
        EmptyScanResultDialog(
            icon = Icons.Filled.Receipt,
            subtitle = "다시 영수증 스캔해주세요",
            onDismiss = { showEmptyReceiptDialog = false },
        )
    }
    if (showEmptyFridgeDialog) {
        EmptyScanResultDialog(
            icon = Icons.Filled.Kitchen,
            subtitle = "다시 냉장고 스캔해주세요",
            onDismiss = { showEmptyFridgeDialog = false },
        )
    }
}

/** 결과 화면 취소 시 전달된 returnTab 문자열을 [ScanTab] 으로 매핑 (기본: 식재료). */
private fun String?.toScanTabOrDefault(): ScanTab =
    when (this?.uppercase()) {
        "RECEIPT" -> ScanTab.Receipt
        "FRIDGE" -> ScanTab.Fridge
        else -> ScanTab.Ingredient
    }

/**
 * 인식 결과가 비어 "다시 스캔" 안내가 필요한 탭을 반환 (없으면 null).
 * 영수증/냉장고만 빈 결과 안내 대상이며, 현재 탭과 결과 sourceType 이 일치할 때만 처리.
 */
private fun emptyResultTabOrNull(
    currentTab: ScanTab,
    sourceType: String,
    isEmpty: Boolean,
): ScanTab? = when {
    currentTab == ScanTab.Receipt && sourceType == ScanSourceType.RECEIPT && isEmpty -> ScanTab.Receipt
    currentTab == ScanTab.Fridge && sourceType == ScanSourceType.FRIDGE && isEmpty -> ScanTab.Fridge
    else -> null
}

// 크롭/리사이즈 목표 해상도 (탭별 업로드 규격).
private const val INGREDIENT_FIT_MAX_EDGE = 1024
private const val RECEIPT_TARGET_W = 640
private const val RECEIPT_TARGET_H = 1024
private const val FRIDGE_TARGET_W = 1024
private const val FRIDGE_TARGET_H = 1365
private const val INGREDIENT_TARGET_W = 1024
private const val INGREDIENT_TARGET_H = 1024

/**
 * 식재료: 프레임 크롭 없이 원본 비율 유지 + 긴 변 최대 1024 리사이즈 (API·미리보기 동일 파일).
 * 영수증/냉장고: 프레임 영역 크롭 후 지정 해상도로 리사이즈.
 */
private fun Context.prepareImageForScanUpload(
    originalBitmap: Bitmap?,
    fallbackUri: Uri,
    tab: ScanTab,
    previewBounds: RectF?,
    frameBounds: RectF?,
): String {
    if (originalBitmap == null) {
        Log.d("ScanCrop", "Bitmap load failed; using fallback uri=$fallbackUri")
        return fallbackUri.toString()
    }
    if (tab == ScanTab.Ingredient) {
        val resized = ScanImageCropper.resizeFitWithinMax(originalBitmap, maxSize = INGREDIENT_FIT_MAX_EDGE)
        val uri = ScanImageCropper.saveJpegToInternal(this, resized, prefix = "ingredient_fit")
        Log.d(
            "ScanCrop",
            "ingredient fit-resize ${originalBitmap.width}x${originalBitmap.height} -> ${resized.width}x${resized.height}",
        )
        return uri.toString()
    }
    if (previewBounds == null || frameBounds == null) {
        Log.d("ScanCrop", "Bounds missing; skipping crop. tab=$tab")
        return fallbackUri.toString()
    }
    val cropRect = ScanImageCropper.calculateCropRectCenterCrop(
        frameBoundsInWindowPx = frameBounds,
        previewBoundsInWindowPx = previewBounds,
        bitmapW = originalBitmap.width,
        bitmapH = originalBitmap.height,
    )
    val cropped = ScanImageCropper.cropBitmapSafe(originalBitmap, cropRect)
    val croppedUri = ScanImageCropper.saveJpegToInternal(this, cropped, prefix = "cropped")
    val (targetW, targetH, prefix) =
        when (tab) {
            ScanTab.Receipt -> Triple(RECEIPT_TARGET_W, RECEIPT_TARGET_H, "resized_640x1024")
            ScanTab.Fridge -> Triple(FRIDGE_TARGET_W, FRIDGE_TARGET_H, "resized_fridge")
            ScanTab.Ingredient -> Triple(INGREDIENT_TARGET_W, INGREDIENT_TARGET_H, "resized_1024")
        }
    val resized = ScanImageCropper.resize(cropped, targetW = targetW, targetH = targetH)
    val resizedUri = ScanImageCropper.saveJpegToInternal(this, resized, prefix = prefix)
    ScanImageCropper.logDebug(
        ScanImageCropper.CropDebug(
            bitmapW = originalBitmap.width,
            bitmapH = originalBitmap.height,
            frame = frameBounds,
            preview = previewBounds,
            crop = cropRect,
            croppedW = cropped.width,
            croppedH = cropped.height,
            resizedW = resized.width,
            resizedH = resized.height,
            croppedSavedPath = croppedUri.path,
            resizedSavedPath = resizedUri.path,
        ),
    )
    return resizedUri.toString()
}

private fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
