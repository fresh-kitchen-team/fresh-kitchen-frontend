package com.example.myfrigelocal.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myfrigelocal.navigation.BottomNavRoute
import com.example.myfrigelocal.network.HomeRepository
import coil.compose.rememberAsyncImagePainter
import com.example.myfrigelocal.data.scan.CreateItemRequest
import com.example.myfrigelocal.data.scan.ScanRepository
import com.example.myfrigelocal.data.scan.ScanResultItemUiModel
import com.example.myfrigelocal.data.scan.ScanResultUiModel
import com.example.myfrigelocal.data.scan.parseScanResultUiModel
import com.example.myfrigelocal.data.scan.resolveStorageIdForSave
import com.example.myfrigelocal.navigation.ScanNav

private val PrimaryGreen = Color(0xFF00C853)

private fun storageTypeToDisplay(code: String): String =
    when (code.uppercase()) {
        "FRIDGE" -> "냉장실"
        "FREEZER" -> "냉동실"
        "PANTRY" -> "실온"
        "ROOM" -> "실온"
        else -> code
    }

@Composable
fun ScanResultScreen(
    navController: NavController,
) {
    val prev = navController.previousBackStackEntry
    val imageUriString =
        prev
            ?.savedStateHandle
            ?.get<String?>(ScanNav.keyImageUri)
    val receiptItems: ArrayList<String>? =
        prev
            ?.savedStateHandle
            ?.get<ArrayList<String>>(ScanNav.keyReceiptItems)
    val scanResultJson = prev?.savedStateHandle?.get<String>(ScanNav.keyScanResultJson)
    val parsedScan: ScanResultUiModel? = remember(scanResultJson) { parseScanResultUiModel(scanResultJson) }

    var currentItemIndex by rememberSaveable {
        mutableStateOf(
            prev
                ?.savedStateHandle
                ?.get<Int>(ScanNav.keyReceiptIndex)
                ?: 0,
        )
    }
    val totalItems = receiptItems?.size ?: 0

    val suggestedIngredientName =
        prev
            ?.savedStateHandle
            ?.get<String>(ScanNav.keyIngredientSuggestion)
            .orEmpty()

    val isReceiptSequence =
        when {
            parsedScan != null -> parsedScan.sourceType == "RECEIPT" && parsedScan.items.size > 1
            else -> !receiptItems.isNullOrEmpty()
        }

    // --- Form state (legacy vs API-driven) ---
    var name by rememberSaveable { mutableStateOf("") }
    var storage by rememberSaveable { mutableStateOf("냉장실") }
    var expiration by rememberSaveable { mutableStateOf("") }
    var registeredAt by rememberSaveable { mutableStateOf("") }

    var photoCandidateIndex by rememberSaveable { mutableStateOf(0) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    val scanRepo = remember { ScanRepository(context.applicationContext) }

    suspend fun navigateToHomeWithSummaryRefresh() {
        if (ScanRepository.isApiConfigured()) {
            withContext(Dispatchers.IO) {
                HomeRepository().getHomeSummary()
            }
        }
        navController.getBackStackEntry(BottomNavRoute.Home.route)
            ?.savedStateHandle
            ?.set(ScanNav.keyRefreshHome, System.currentTimeMillis())
        navController.navigate(BottomNavRoute.Home.route) {
            popUpTo(BottomNavRoute.Home.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    suspend fun proceedAfterSuccessfulSave() {
        when {
            parsedScan != null && parsedScan.sourceType == "RECEIPT" -> {
                val nextIndex = currentItemIndex + 1
                if (nextIndex < parsedScan.items.size) {
                    currentItemIndex = nextIndex
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(ScanNav.keyReceiptIndex, nextIndex)
                } else {
                    navigateToHomeWithSummaryRefresh()
                }
            }
            isReceiptSequence && receiptItems != null -> {
                val nextIndex = currentItemIndex + 1
                if (nextIndex < totalItems) {
                    currentItemIndex = nextIndex
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(ScanNav.keyReceiptIndex, nextIndex)
                } else {
                    navigateToHomeWithSummaryRefresh()
                }
            }
            else -> {
                navigateToHomeWithSummaryRefresh()
            }
        }
    }

    LaunchedEffect(parsedScan, currentItemIndex, photoCandidateIndex, receiptItems, suggestedIngredientName) {
        when {
            parsedScan != null -> {
                val item =
                    when (parsedScan.sourceType) {
                        "RECEIPT" ->
                            parsedScan.items.getOrNull(currentItemIndex)
                                ?: parsedScan.items.firstOrNull()
                                ?: return@LaunchedEffect
                        else ->
                            parsedScan.items.getOrNull(photoCandidateIndex)
                                ?: parsedScan.items.firstOrNull()
                                ?: return@LaunchedEffect
                    }
                name = item.name
                storage = storageTypeToDisplay(item.storageType)
                registeredAt = item.registeredAt.orEmpty()
                expiration = item.expiresAt.orEmpty()
            }
            !receiptItems.isNullOrEmpty() -> {
                val nextName = receiptItems.getOrNull(currentItemIndex)
                if (!nextName.isNullOrBlank()) {
                    name = nextName
                    expiration = ""
                }
            }
            else -> {
                name =
                    suggestedIngredientName.takeIf { it.isNotBlank() } ?: "신선한 우유"
                storage = "냉장실"
                registeredAt = ""
                expiration = ""
            }
        }
    }

    val previewModel: Any? =
        remember(parsedScan, imageUriString) {
            when {
                parsedScan?.remotePreviewImageUrl?.isNotBlank() == true ->
                    parsedScan.remotePreviewImageUrl
                parsedScan?.localPreviewImageUri?.isNotBlank() == true ->
                    Uri.parse(parsedScan.localPreviewImageUri)
                !imageUriString.isNullOrBlank() -> Uri.parse(imageUriString)
                else -> null
            }
        }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F9FC),
    ) {
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 2.dp, bottom = 10.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFE5E7EB)),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "스캔 결과 확인",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                )
                if (isReceiptSequence) {
                    val count =
                        when {
                            parsedScan != null -> parsedScan.items.size
                            else -> totalItems
                        }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "${currentItemIndex + 1} / $count",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "인식된 정보를 확인하고 수정해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            if (parsedScan?.sourceType == "RECEIPT" &&
                (!parsedScan.purchasedAt.isNullOrBlank() || !parsedScan.purchasedAtSourceType.isNullOrBlank())
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        buildString {
                            parsedScan.purchasedAt?.let { append("구매일: $it  ") }
                            parsedScan.purchasedAtSourceType?.let { append("(출처: $it)") }
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEFF3F8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (previewModel == null) {
                            Image(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                            )
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(model = previewModel),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp, end = 12.dp),
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Chip(text = storage, tint = PrimaryGreen)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (parsedScan != null && parsedScan.sourceType == "PHOTO" && parsedScan.items.size > 1) {
                var candidateMenuExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = parsedScan.items.getOrNull(photoCandidateIndex)?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("인식 후보") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { candidateMenuExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { candidateMenuExpanded = !candidateMenuExpanded }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "후보 목록",
                                )
                            }
                        },
                    )
                    DropdownMenu(
                        expanded = candidateMenuExpanded,
                        onDismissRequest = { candidateMenuExpanded = false },
                    ) {
                        parsedScan.items.forEachIndexed { idx, item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        item.name +
                                            (item.confidence?.let { c -> " (${(c * 100).toInt()}%)" } ?: ""),
                                    )
                                },
                                onClick = {
                                    photoCandidateIndex = idx
                                    candidateMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            FieldLabel(text = "이름", required = true)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "등록일", required = true)
            OutlinedTextField(
                value = registeredAt,
                onValueChange = { registeredAt = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD") },
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "보관장소", required = true)
            OutlinedTextField(
                value = storage,
                onValueChange = { storage = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "유통기한", required = false)
            OutlinedTextField(
                value = expiration,
                onValueChange = { expiration = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD (미입력 가능)") },
            )
            if (expiration.isBlank()) {
                Text(
                    text = "유통기한은 직접 입력할 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9)),
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(ScanNav.keyReset, true)
                        navController.popBackStack()
                    },
                ) {
                    Text(
                        text = "취소",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        if (saving) return@Button
                        val trimmedName = name.trim()
                        if (trimmedName.isEmpty()) {
                            Toast.makeText(context, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            saving = true
                            try {
                                if (!ScanRepository.isApiConfigured()) {
                                    proceedAfterSuccessfulSave()
                                    return@launch
                                }
                                val storages =
                                    scanRepo.fetchItemStorages().getOrElse { err ->
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
                                val row =
                                    when {
                                        parsedScan != null ->
                                            when (parsedScan.sourceType) {
                                                "RECEIPT" ->
                                                    parsedScan.items.getOrNull(currentItemIndex)
                                                        ?: parsedScan.items.firstOrNull()
                                                else ->
                                                    parsedScan.items.getOrNull(photoCandidateIndex)
                                                        ?: parsedScan.items.firstOrNull()
                                            }
                                        else -> null
                                    }
                                val fallbackType =
                                    row?.storageType ?: ScanResultItemUiModel.DEFAULT_STORAGE
                                val storageId =
                                    resolveStorageIdForSave(storages, storage, fallbackType)
                                if (storageId == null) {
                                    Toast.makeText(
                                        context,
                                        "보관 장소와 일치하는 storageId를 찾을 수 없습니다.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    return@launch
                                }
                                val imageAssetId =
                                    if (parsedScan != null && parsedScan.sourceType == "PHOTO") {
                                        parsedScan.imageAssetId
                                    } else {
                                        null
                                    }
                                val body =
                                    CreateItemRequest(
                                        name = trimmedName,
                                        storageId = storageId,
                                        expiryDate = expiration.trim().takeIf { it.isNotEmpty() },
                                        purchaseDate = registeredAt.trim().takeIf { it.isNotEmpty() },
                                        memo = null,
                                        imageAssetId = imageAssetId,
                                    )
                                scanRepo.createItem(body).fold(
                                    onSuccess = { proceedAfterSuccessfulSave() },
                                    onFailure = { err ->
                                        Toast.makeText(
                                            context,
                                            err.message ?: "저장에 실패했습니다.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    },
                                )
                            } finally {
                                saving = false
                            }
                        }
                    },
                ) {
                    Text(
                        text = "저장",
                        color = Color(0xFF062115),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    required: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
        )
        if (required) {
            Text(
                text = " *",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEF4444),
            )
        }
    }
}

@Composable
private fun Chip(
    text: String,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(width = 1.dp, color = tint.copy(alpha = 0.18f), shape = RoundedCornerShape(999.dp))
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
