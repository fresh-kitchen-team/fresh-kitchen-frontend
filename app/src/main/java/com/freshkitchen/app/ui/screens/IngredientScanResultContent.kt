package com.freshkitchen.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

data class IngredientScanCandidate(
    val name: String,
    val confidence: Double?,
)

@Composable
fun IngredientScanResultContent(
    item: ReceiptResultItemUiState,
    onItemChange: (ReceiptResultItemUiState) -> Unit,
    previewModel: Any?,
    candidates: List<IngredientScanCandidate>,
    selectedCandidateIndex: Int,
    onSelectCandidate: (Int) -> Unit,
    quickOffsetDays: Int,
    onQuickAdd: (Int) -> Unit,
    onResetExpiry: () -> Unit,
    saving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScanScreenBg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(ScanDivider),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "스캔 결과 확인",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ScanTextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "인식된 정보를 확인하고 유통기한을 설정해주세요.",
                style = MaterialTheme.typography.bodySmall,
                color = ScanTextSecondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuickExpirySection(
                selectedCount = 1,
                totalCount = 1,
                enabledAll = !saving,
                onSelectAllToggle = null,
                onQuickAdd = onQuickAdd,
                overrideSubtitle = "버튼을 누를 때마다 오늘 기준으로 누적됩니다.",
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (candidates.size > 1) {
                CandidateDropdown(
                    candidates = candidates,
                    selectedIndex = selectedCandidateIndex,
                    onSelect = onSelectCandidate,
                    enabled = !saving,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            IngredientCard(
                item = item,
                offsetDays = quickOffsetDays.takeIf { it > 0 },
                previewModel = previewModel,
                onUpdate = onItemChange,
                onResetExpiry = onResetExpiry,
                enabled = !saving,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        ScanResultBottomBar(
            cancelEnabled = !saving,
            onCancel = onCancel,
            saveLabel = if (saving) "저장 중…" else "저장하기",
            saveEnabled = !saving && item.name.trim().isNotEmpty(),
            onSave = onSave,
        )
    }
}

@Composable
private fun CandidateDropdown(
    candidates: List<IngredientScanCandidate>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val current = candidates.getOrNull(selectedIndex)?.name.orEmpty()
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("인식 후보", color = ScanTextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "후보 목록")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ScanPrimary,
                unfocusedBorderColor = ScanDivider,
                disabledBorderColor = ScanDivider,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
            ),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            candidates.forEachIndexed { idx, c ->
                DropdownMenuItem(
                    text = {
                        Text(
                            c.name +
                                (c.confidence?.let { p -> " (${(p * 100).toInt()}%)" } ?: ""),
                        )
                    },
                    onClick = {
                        onSelect(idx)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun IngredientCard(
    item: ReceiptResultItemUiState,
    offsetDays: Int?,
    previewModel: Any?,
    onUpdate: (ReceiptResultItemUiState) -> Unit,
    onResetExpiry: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ScanCardBg,
        border = BorderStroke(1.dp, ScanDivider),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ScanChipBg),
                    contentAlignment = Alignment.Center,
                ) {
                    if (previewModel == null) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = ScanTextTertiary,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(model = previewModel),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    CompactNameField(
                        value = item.name,
                        onValueChange = { onUpdate(item.copy(name = it)) },
                        enabled = enabled,
                        placeholder = "식재료 이름",
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StorageDropdownChip(
                    storageType = item.storageType,
                    onChange = { onUpdate(item.copy(storageType = it)) },
                    enabled = enabled,
                )
                ExpiryChipRow(
                    expiresAt = item.expiresAt,
                    offsetDays = offsetDays,
                    initialExpiresAt = item.initialExpiresAt,
                    onResetExpiry = onResetExpiry,
                    enabled = enabled,
                )
            }
        }
    }
}
