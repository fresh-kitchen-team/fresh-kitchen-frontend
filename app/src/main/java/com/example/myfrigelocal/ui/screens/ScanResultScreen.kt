package com.example.myfrigelocal.ui.screens

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
import com.example.myfrigelocal.data.scan.CreateItemRequest
import com.example.myfrigelocal.data.scan.ScanRepository
import com.example.myfrigelocal.data.scan.ScanResultItemUiModel
import com.example.myfrigelocal.data.scan.normalizeScanCategory
import com.example.myfrigelocal.data.scan.ScanResultUiModel
import com.example.myfrigelocal.data.scan.parseScanResultUiModel
import com.example.myfrigelocal.data.scan.resolveStorageIdForSave
import com.example.myfrigelocal.navigation.BottomNavRoute
import com.example.myfrigelocal.navigation.ScanNav
import com.example.myfrigelocal.network.HomeRepository
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

    /** 영수증 OCR: `sourceType == RECEIPT` 또는 레거시 `receiptItems` 목록 */
    val isReceiptOcrResult =
        when {
            parsedScan?.sourceType == "RECEIPT" -> true
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
        navController.previousBackStackEntry?.savedStateHandle?.set(ScanNav.keyReset, true)
        navController.popBackStack()
    }

    if (isReceiptOcrResult) {
        ReceiptResultRoute(
            parsedScan = parsedScan,
            receiptItems = receiptItems,
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
private fun ReceiptResultRoute(
    parsedScan: ScanResultUiModel?,
    receiptItems: ArrayList<String>?,
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

    ReceiptScanResultContent(
        items = receiptList,
        onItemsChange = { receiptList = it },
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
                    if (!ScanRepository.isApiConfigured()) {
                        onNavigateHome()
                        return@launch
                    }
                    val storages = scanRepo.fetchItemStorages().getOrElse { err ->
                        Toast.makeText(
                            context,
                            err.message ?: "보관함 목록을 불러오지 못했습니다.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@launch
                    }
                    if (storages.isEmpty()) {
                        Toast.makeText(context, "등록된 보관함이 없습니다.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val defaultPurchaseDate = parsedScan?.purchasedAt ?: todayIsoDate()
                    for (item in toSave) {
                        val storageId = resolveStorageIdForSave(
                            storages,
                            storageTypeToDisplay(item.storageType),
                            item.storageType,
                        )
                        if (storageId == null) {
                            Toast.makeText(
                                context,
                                "보관 장소와 일치하는 storageId를 찾을 수 없습니다.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@launch
                        }
                        val body = CreateItemRequest(
                            name = item.name.trim(),
                            storageId = storageId,
                            expiryDate = item.expiresAt.trim().takeIf { it.isNotEmpty() },
                            purchaseDate = item.registeredAt?.trim()?.takeIf { it.isNotEmpty() }
                                ?: defaultPurchaseDate,
                            memo = null,
                            imageAssetId = parsedScan?.imageAssetId,
                            category = normalizeScanCategory(item.category),
                        )
                        scanRepo.createItem(body).getOrElse { err ->
                            Toast.makeText(
                                context,
                                err.message ?: "저장에 실패했습니다.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@launch
                        }
                    }
                    onNavigateHome()
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
                    ReceiptResultItemUiState(
                        name = row.name,
                        category = row.category.ifBlank { ScanResultItemUiModel.DEFAULT_CATEGORY },
                        storageType = row.storageType.ifBlank { ScanResultItemUiModel.DEFAULT_STORAGE },
                        expiresAt = row.expiresAt?.trim()?.takeIf { it.isNotEmpty() } ?: today,
                        registeredAt = row.registeredAt,
                    )
                } else {
                    ReceiptResultItemUiState(
                        name = suggestedIngredientName.takeIf { it.isNotBlank() } ?: "",
                        storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                        expiresAt = today,
                        registeredAt = null,
                    )
                }
            }
            else -> ReceiptResultItemUiState(
                name = suggestedIngredientName.takeIf { it.isNotBlank() } ?: "",
                storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                expiresAt = today,
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

    val previewModel: Any? = remember(parsedScan, imageUriString) {
        when {
            parsedScan?.remotePreviewImageUrl?.isNotBlank() == true -> parsedScan.remotePreviewImageUrl
            parsedScan?.localPreviewImageUri?.isNotBlank() == true -> Uri.parse(parsedScan.localPreviewImageUri)
            !imageUriString.isNullOrBlank() -> Uri.parse(imageUriString)
            else -> null
        }
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
        saving = saving,
        onCancel = onCancel,
        onSave = onSave@{
            if (saving) return@onSave
            val trimmedName = item.name.trim()
            if (trimmedName.isEmpty()) {
                Toast.makeText(context, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@onSave
            }
            scope.launch {
                onSavingChange(true)
                try {
                    if (!ScanRepository.isApiConfigured()) {
                        onNavigateHome()
                        return@launch
                    }
                    val storages = scanRepo.fetchItemStorages().getOrElse { err ->
                        Toast.makeText(
                            context,
                            err.message ?: "보관함 목록을 불러오지 못했습니다.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@launch
                    }
                    if (storages.isEmpty()) {
                        Toast.makeText(context, "등록된 보관함이 없습니다.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val storageId = resolveStorageIdForSave(
                        storages,
                        storageTypeToDisplay(item.storageType),
                        item.storageType,
                    )
                    if (storageId == null) {
                        Toast.makeText(
                            context,
                            "보관 장소와 일치하는 storageId를 찾을 수 없습니다.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@launch
                    }
                    val imageAssetId =
                        if (parsedScan?.sourceType == "PHOTO") parsedScan.imageAssetId else null
                    val body = CreateItemRequest(
                        name = trimmedName,
                        storageId = storageId,
                        expiryDate = item.expiresAt.trim().takeIf { it.isNotEmpty() },
                        purchaseDate = item.registeredAt?.trim()?.takeIf { it.isNotEmpty() },
                        memo = null,
                        imageAssetId = imageAssetId,
                        category = normalizeScanCategory(item.category),
                    )
                    scanRepo.createItem(body).fold(
                        onSuccess = { onNavigateHome() },
                        onFailure = { err ->
                            Toast.makeText(
                                context,
                                err.message ?: "저장에 실패했습니다.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    )
                } finally {
                    onSavingChange(false)
                }
            }
        },
    )
}
