package com.freshkitchen.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.freshkitchen.app.data.scan.CreateItemRequest
import com.freshkitchen.app.data.scan.ScanRepository
import com.freshkitchen.app.data.scan.ScanResultItemUiModel
import com.freshkitchen.app.data.scan.ScanResultUiModel
import com.freshkitchen.app.data.scan.ScanSourceType
import com.freshkitchen.app.data.scan.normalizeStorageTypeForApi
import com.freshkitchen.app.data.scan.parseScanResultUiModel
import com.freshkitchen.app.navigation.BottomNavRoute
import com.freshkitchen.app.navigation.ScanNav
import com.freshkitchen.app.network.HomeRepository
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScanResultScreen(
    navController: NavController,
) {
    val prev = navController.previousBackStackEntry
    val imageUriString = prev?.savedStateHandle?.get<String?>(ScanNav.keyImageUri)
    val receiptItems: ArrayList<String>? =
        prev?.savedStateHandle?.get<ArrayList<String>>(ScanNav.keyReceiptItems)
    val scanResultJson = prev?.savedStateHandle?.get<String>(ScanNav.keyScanResultJson)
    val parsedScan: ScanResultUiModel? = remember(scanResultJson) { parseScanResultUiModel(scanResultJson) }

    val suggestedIngredientName =
        prev?.savedStateHandle?.get<String>(ScanNav.keyIngredientSuggestion).orEmpty()

    val isFridgeScanResult = parsedScan?.sourceType == ScanSourceType.FRIDGE

    /** 영수증 OCR: `sourceType == RECEIPT` 또는 레거시 `receiptItems` 목록 */
    val isReceiptOcrResult =
        when {
            isFridgeScanResult -> false
            parsedScan?.sourceType == ScanSourceType.RECEIPT -> true
            parsedScan == null && !receiptItems.isNullOrEmpty() -> true
            else -> false
        }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    val scanRepo = remember { ScanRepository(context.applicationContext) }

    suspend fun navigateToHomeWithSummaryRefresh() {
        if (ScanRepository.isApiConfigured()) {
            withContext(Dispatchers.IO) {
                runCatching { HomeRepository().getHomeSummary() }
            }
        }
        val refreshAt = System.currentTimeMillis()
        withContext(Dispatchers.Main.immediate) {
            // Scan 탭 진입 시 home이 stack에서 제거되므로 startDestination popUpTo는 동작하지 않음.
            // scan → scan_result 흐름을 정리한 뒤 home으로 이동한다.
            navController.navigate(BottomNavRoute.Home.route) {
                popUpTo(BottomNavRoute.Scan.route) { inclusive = true }
                launchSingleTop = true
            }
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set(ScanNav.keyRefreshHome, refreshAt)
        }
    }

    fun onCancel() {
        val returnTab =
            when {
                isFridgeScanResult -> "FRIDGE"
                isReceiptOcrResult -> "RECEIPT"
                else -> "INGREDIENT"
            }
        navController.previousBackStackEntry?.savedStateHandle?.apply {
            set(ScanNav.keyReturnTab, returnTab)
            set(ScanNav.keyResetAt, System.currentTimeMillis())
            set(ScanNav.keyReset, true)
        }
        navController.popBackStack()
    }

    if (isFridgeScanResult) {
        FridgeResultRoute(
            parsedScan = parsedScan,
            imageUriString = imageUriString,
            scanRepo = scanRepo,
            saving = saving,
            onSavingChange = { saving = it },
            context = context,
            scope = scope,
            onCancel = ::onCancel,
            onNavigateHome = { navigateToHomeWithSummaryRefresh() },
        )
        return
    }

    if (isReceiptOcrResult) {
        ReceiptResultRoute(
            parsedScan = parsedScan,
            receiptItems = receiptItems,
            imageUriString = imageUriString,
            scanRepo = scanRepo,
            saving = saving,
            onSavingChange = { saving = it },
            context = context,
            scope = scope,
            onCancel = ::onCancel,
            onNavigateHome = { navigateToHomeWithSummaryRefresh() },
        )
        return
    }

    IngredientResultRoute(
        parsedScan = parsedScan,
        imageUriString = imageUriString,
        suggestedIngredientName = suggestedIngredientName,
        scanRepo = scanRepo,
        saving = saving,
        onSavingChange = { saving = it },
        context = context,
        scope = scope,
        onCancel = ::onCancel,
        onNavigateHome = { navigateToHomeWithSummaryRefresh() },
    )
}

@Composable
private fun FridgeResultRoute(
    parsedScan: ScanResultUiModel?,
    imageUriString: String?,
    scanRepo: ScanRepository,
    saving: Boolean,
    onSavingChange: (Boolean) -> Unit,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onCancel: () -> Unit,
    onNavigateHome: suspend () -> Unit,
) {
    var itemList by remember { mutableStateOf<List<ReceiptResultItemUiState>>(emptyList()) }
    var listInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(parsedScan) {
        if (!listInitialized && parsedScan != null) {
            itemList = buildFridgeListFromScan(parsedScan)
            listInitialized = true
        }
    }

    val previewModel: Any? = remember(parsedScan, imageUriString) {
        resolveScanPreviewModel(parsedScan, imageUriString)
    }

    ReceiptScanResultContent(
        items = itemList,
        onItemsChange = { itemList = it },
        previewModel = previewModel,
        previewImageTitle = "냉장고 사진",
        previewAspectRatio = FRIDGE_PREVIEW_ASPECT_RATIO,
        saving = saving,
        onCancel = onCancel,
        onSave = {
            if (saving) return@ReceiptScanResultContent
            val toSave = itemList.filter { it.name.trim().isNotEmpty() }
            if (toSave.isEmpty()) {
                Toast.makeText(context, "저장할 품목이 없습니다.", Toast.LENGTH_SHORT).show()
                return@ReceiptScanResultContent
            }
            scope.launch {
                onSavingChange(true)
                try {
                    saveScannedItems(
                        context = context,
                        scanRepo = scanRepo,
                        items = toSave,
                        sourceType = ScanSourceType.PHOTO,
                        defaultPurchaseDate = todayIsoDate(),
                        imageAssetId = parsedScan?.imageAssetId,
                        requireStorageGuard = true,
                        onNavigateHome = onNavigateHome,
                    )
                } finally {
                    onSavingChange(false)
                }
            }
        },
    )
}

@Composable
private fun ReceiptResultRoute(
    parsedScan: ScanResultUiModel?,
    receiptItems: ArrayList<String>?,
    imageUriString: String?,
    scanRepo: ScanRepository,
    saving: Boolean,
    onSavingChange: (Boolean) -> Unit,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onCancel: () -> Unit,
    onNavigateHome: suspend () -> Unit,
) {
    var receiptList by remember { mutableStateOf<List<ReceiptResultItemUiState>>(emptyList()) }
    var receiptListInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(parsedScan, receiptItems) {
        if (!receiptListInitialized) {
            receiptList = when {
                parsedScan?.sourceType == "RECEIPT" -> buildReceiptListFromScan(parsedScan)
                !receiptItems.isNullOrEmpty() -> buildReceiptListFromLegacyNames(receiptItems)
                else -> emptyList()
            }
            receiptListInitialized = true
        }
    }

    val previewModel: Any? = remember(parsedScan, imageUriString) {
        resolveScanPreviewModel(parsedScan, imageUriString)
    }

    ReceiptScanResultContent(
        items = receiptList,
        onItemsChange = { receiptList = it },
        previewModel = previewModel,
        saving = saving,
        onCancel = onCancel,
        onSave = {
            if (saving) return@ReceiptScanResultContent
            val toSave = receiptList.filter { it.name.trim().isNotEmpty() }
            if (toSave.isEmpty()) {
                Toast.makeText(context, "저장할 품목이 없습니다.", Toast.LENGTH_SHORT).show()
                return@ReceiptScanResultContent
            }
            scope.launch {
                onSavingChange(true)
                try {
                    saveScannedItems(
                        context = context,
                        scanRepo = scanRepo,
                        items = toSave,
                        sourceType = ScanSourceType.RECEIPT,
                        defaultPurchaseDate = parsedScan?.purchasedAt ?: todayIsoDate(),
                        imageAssetId = parsedScan?.imageAssetId,
                        requireStorageGuard = false,
                        onNavigateHome = onNavigateHome,
                    )
                } finally {
                    onSavingChange(false)
                }
            }
        },
    )
}

@Composable
private fun IngredientResultRoute(
    parsedScan: ScanResultUiModel?,
    imageUriString: String?,
    suggestedIngredientName: String,
    scanRepo: ScanRepository,
    saving: Boolean,
    onSavingChange: (Boolean) -> Unit,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onCancel: () -> Unit,
    onNavigateHome: suspend () -> Unit,
) {
    var candidateIndex by remember { mutableIntStateOf(0) }
    var itemState by remember { mutableStateOf<ReceiptResultItemUiState?>(null) }
    var quickOffsetDays by remember { mutableIntStateOf(0) }

    val candidates = remember(parsedScan) {
        parsedScan?.items?.map {
            IngredientScanCandidate(name = it.name, confidence = it.confidence)
        }.orEmpty()
    }

    LaunchedEffect(parsedScan, candidateIndex, suggestedIngredientName) {
        val today = todayIsoDate()
        val base = when {
            parsedScan != null -> {
                val row = parsedScan.items.getOrNull(candidateIndex)
                    ?: parsedScan.items.firstOrNull()
                if (row != null) {
                    val expiry = row.expiresAt?.trim()?.takeIf { it.isNotEmpty() } ?: today
                    ReceiptResultItemUiState(
                        name = row.name,
                        category = row.category.ifBlank { ScanResultItemUiModel.DEFAULT_CATEGORY },
                        storageType = row.storageType.ifBlank { ScanResultItemUiModel.DEFAULT_STORAGE },
                        expiresAt = expiry,
                        initialExpiresAt = expiry,
                        registeredAt = row.registeredAt,
                    )
                } else {
                    ReceiptResultItemUiState(
                        name = suggestedIngredientName.takeIf { it.isNotBlank() } ?: "",
                        storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                        expiresAt = today,
                        initialExpiresAt = today,
                        registeredAt = null,
                    )
                }
            }
            else -> ReceiptResultItemUiState(
                name = suggestedIngredientName.takeIf { it.isNotBlank() } ?: "",
                storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                expiresAt = today,
                initialExpiresAt = today,
                registeredAt = null,
            )
        }
        // 후보 변경 시에는 기본값으로 갱신, 그 외 최초 1회만 세팅.
        if (itemState == null || parsedScan != null) {
            itemState = base
            if (parsedScan != null) quickOffsetDays = 0
        }
    }

    val item = itemState ?: return

    // API 업로드와 동일한 로컬 파일을 우선 표시 (S3 URL은 서버 가공본일 수 있음).
    val previewModel: Any? = remember(parsedScan, imageUriString) {
        resolveScanPreviewModel(parsedScan, imageUriString)
    }

    IngredientScanResultContent(
        item = item,
        onItemChange = { itemState = it },
        previewModel = previewModel,
        candidates = candidates,
        selectedCandidateIndex = candidateIndex,
        onSelectCandidate = { candidateIndex = it },
        quickOffsetDays = quickOffsetDays,
        onQuickAdd = { days ->
            val newOffset = quickOffsetDays + days
            quickOffsetDays = newOffset
            itemState = item.copy(
                expiresAt = LocalDate.now().plusDays(newOffset.toLong()).toString(),
            )
        },
        onResetExpiry = {
            quickOffsetDays = 0
            itemState = item.copy(expiresAt = item.initialExpiresAt)
        },
        saving = saving,
        onCancel = onCancel,
        onSave = onSave@{
            if (saving) return@onSave
            if (item.name.trim().isEmpty()) {
                Toast.makeText(context, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@onSave
            }
            val imageAssetId =
                if (parsedScan?.sourceType == ScanSourceType.PHOTO) parsedScan.imageAssetId else null
            scope.launch {
                onSavingChange(true)
                try {
                    saveScannedItems(
                        context = context,
                        scanRepo = scanRepo,
                        items = listOf(item),
                        sourceType = ScanSourceType.PHOTO,
                        defaultPurchaseDate = null,
                        imageAssetId = imageAssetId,
                        requireStorageGuard = false,
                        onNavigateHome = onNavigateHome,
                    )
                } finally {
                    onSavingChange(false)
                }
            }
        },
    )
}

/**
 * 스캔 결과 미리보기 모델 우선순위: 로컬 캡처본 → 화면 전달 URI → 서버 가공 URL.
 * (S3 가공본보다 업로드한 로컬 파일을 우선 표시)
 */
private fun resolveScanPreviewModel(
    parsedScan: ScanResultUiModel?,
    imageUriString: String?,
): Any? = when {
    parsedScan?.localPreviewImageUri?.isNotBlank() == true -> Uri.parse(parsedScan.localPreviewImageUri)
    !imageUriString.isNullOrBlank() -> Uri.parse(imageUriString)
    parsedScan?.remotePreviewImageUrl?.isNotBlank() == true -> parsedScan.remotePreviewImageUrl
    else -> null
}

/**
 * 스캔 결과 품목을 메인 식재료 저장 API(`POST /api/v1/items`)로 순차 저장합니다.
 * 세 결과 화면(식재료/냉장고/영수증)이 공유하는 저장 흐름을 한 곳으로 모읍니다.
 *
 * @param requireStorageGuard 저장 전 보관함 목록 조회로 사전 검증할지 (냉장고 흐름과 동일).
 */
private suspend fun saveScannedItems(
    context: android.content.Context,
    scanRepo: ScanRepository,
    items: List<ReceiptResultItemUiState>,
    sourceType: String,
    defaultPurchaseDate: String?,
    imageAssetId: Long?,
    requireStorageGuard: Boolean,
    onNavigateHome: suspend () -> Unit,
) {
    if (!ScanRepository.isApiConfigured()) {
        onNavigateHome()
        return
    }
    if (requireStorageGuard) {
        val storages = scanRepo.fetchItemStorages().getOrElse { err ->
            context.showSaveToast(err.message ?: "보관함 목록을 불러오지 못했습니다.")
            return
        }
        if (storages.isEmpty()) {
            context.showSaveToast("등록된 보관함이 없습니다.")
            return
        }
    }
    for (item in items) {
        val body = buildScanCreateItemRequest(
            item = item,
            sourceType = sourceType,
            defaultPurchaseDate = defaultPurchaseDate,
            imageAssetId = imageAssetId,
        )
        scanRepo.createItem(body).getOrElse { err ->
            context.showSaveToast(err.message ?: "저장에 실패했습니다.")
            return
        }
    }
    onNavigateHome()
}

private fun buildScanCreateItemRequest(
    item: ReceiptResultItemUiState,
    sourceType: String,
    defaultPurchaseDate: String?,
    imageAssetId: Long?,
): CreateItemRequest =
    CreateItemRequest(
        name = item.name.trim(),
        storageType = normalizeStorageTypeForApi(item.storageType),
        sourceType = sourceType,
        expiryDate = item.expiresAt.trim().takeIf { it.isNotEmpty() },
        purchaseDate = item.registeredAt?.trim()?.takeIf { it.isNotEmpty() } ?: defaultPurchaseDate,
        memo = null,
        imageAssetId = imageAssetId,
    )

private fun android.content.Context.showSaveToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
