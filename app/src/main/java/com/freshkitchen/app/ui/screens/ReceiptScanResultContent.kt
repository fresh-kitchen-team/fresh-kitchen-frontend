package com.freshkitchen.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun ReceiptScanResultContent(
    items: List<ReceiptResultItemUiState>,
    onItemsChange: (List<ReceiptResultItemUiState>) -> Unit,
    previewModel: Any? = null,
    saving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var quickDaysOffsetByItem by remember { mutableStateOf(mapOf<String, Int>()) }
    var showReceiptFullscreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScanScreenBg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(ScanDivider),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "스캔 결과 확인",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScanTextPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "인식된 품목을 확인하고 유통기한을 설정해주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ScanTextSecondary,
                )
            }

            if (previewModel != null) {
                item {
                    ReceiptScanPreviewThumbnail(
                        previewModel = previewModel,
                        onClick = { showReceiptFullscreen = true },
                    )
                }
            }

            item {
                QuickExpirySection(
                    selectedCount = selectedIds.size,
                    totalCount = items.size,
                    enabledAll = items.isNotEmpty() && !saving,
                    onSelectAllToggle = {
                        selectedIds = if (selectedIds.size == items.size && items.isNotEmpty()) {
                            emptySet()
                        } else {
                            items.map { it.id }.toSet()
                        }
                    },
                    onQuickAdd = { days ->
                        if (selectedIds.isEmpty()) return@QuickExpirySection
                        val updatedOffsets = quickDaysOffsetByItem.toMutableMap()
                        selectedIds.forEach { id ->
                            updatedOffsets[id] = (updatedOffsets[id] ?: 0) + days
                        }
                        quickDaysOffsetByItem = updatedOffsets
                        val today = LocalDate.now()
                        onItemsChange(
                            items.map { item ->
                                val offset = updatedOffsets[item.id]
                                if (offset != null && item.id in selectedIds) {
                                    item.copy(expiresAt = today.plusDays(offset.toLong()).toString())
                                } else {
                                    item
                                }
                            },
                        )
                    },
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "인식된 품목",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ScanTextPrimary,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(ScanPrimarySoft)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "${items.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ScanPrimaryDark,
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "인식된 품목이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ScanTextTertiary,
                        )
                    }
                }
            } else {
                items(items, key = { it.id }) { item ->
                    ReceiptItemCard(
                        item = item,
                        isSelected = item.id in selectedIds,
                        offsetDays = quickDaysOffsetByItem[item.id],
                        onResetExpiry = {
                            quickDaysOffsetByItem = quickDaysOffsetByItem - item.id
                            onItemsChange(
                                items.map {
                                    if (it.id == item.id) {
                                        it.copy(expiresAt = it.initialExpiresAt)
                                    } else {
                                        it
                                    }
                                },
                            )
                        },
                        onSelectToggle = {
                            selectedIds = if (item.id in selectedIds) {
                                selectedIds - item.id
                            } else {
                                selectedIds + item.id
                            }
                        },
                        onUpdate = { updated ->
                            onItemsChange(items.map { if (it.id == updated.id) updated else it })
                        },
                        onRemove = {
                            selectedIds = selectedIds - item.id
                            quickDaysOffsetByItem = quickDaysOffsetByItem - item.id
                            onItemsChange(items.filter { it.id != item.id })
                        },
                        enabled = !saving,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        ScanResultBottomBar(
            cancelEnabled = !saving,
            onCancel = onCancel,
            saveLabel = if (saving) "저장 중…" else "${items.size}개 저장하기",
            saveEnabled = !saving && items.any { it.name.trim().isNotEmpty() },
            onSave = onSave,
        )
    }

    if (showReceiptFullscreen && previewModel != null) {
        ReceiptFullscreenImageDialog(
            previewModel = previewModel,
            onDismiss = { showReceiptFullscreen = false },
        )
    }
}

@Composable
internal fun QuickExpirySection(
    selectedCount: Int,
    totalCount: Int,
    enabledAll: Boolean,
    onSelectAllToggle: (() -> Unit)?,
    onQuickAdd: (Int) -> Unit,
    overrideSubtitle: String? = null,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ScanCardBg,
        border = BorderStroke(1.dp, ScanDivider),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = ScanPrimaryDark,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "유통기한 빠른 설정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ScanTextPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (onSelectAllToggle != null && totalCount > 0) {
                    val allSelected = selectedCount == totalCount
                    TextButton(
                        onClick = onSelectAllToggle,
                        enabled = enabledAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = if (allSelected) "전체 해제" else "전체 선택",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ScanPrimaryDark,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            val subtitle = overrideSubtitle ?: if (selectedCount > 0) {
                "선택된 ${selectedCount}개 품목에 적용됩니다 · 누적"
            } else {
                "카드를 선택한 뒤 +1, +3, +7, +10을 눌러주세요."
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (selectedCount > 0 || onSelectAllToggle == null) ScanPrimaryDark else ScanTextTertiary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1, 3, 7, 10).forEach { days ->
                    QuickDayChip(
                        days = days,
                        enabled = enabledAll && (onSelectAllToggle == null || selectedCount > 0),
                        onClick = { onQuickAdd(days) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptItemCard(
    item: ReceiptResultItemUiState,
    isSelected: Boolean,
    offsetDays: Int?,
    onResetExpiry: () -> Unit,
    onSelectToggle: () -> Unit,
    onUpdate: (ReceiptResultItemUiState) -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean,
) {
    val cardShape = RoundedCornerShape(14.dp)
    val cardBg = if (isSelected) ScanPrimaryTint else ScanCardBg
    val borderColor = if (isSelected) ScanPrimary else ScanDivider
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Surface(
        shape = cardShape,
        color = cardBg,
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelectToggle,
            ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SelectionIndicator(isSelected = isSelected)
            Spacer(modifier = Modifier.size(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                CompactNameField(
                    value = item.name,
                    onValueChange = { onUpdate(item.copy(name = it)) },
                    enabled = enabled,
                    placeholder = "식재료 이름",
                )
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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

            Spacer(modifier = Modifier.size(4.dp))

            IconButton(
                onClick = onRemove,
                enabled = enabled,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "삭제",
                    tint = ScanTextTertiary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectionIndicator(isSelected: Boolean) {
    val ringColor = if (isSelected) ScanPrimary else Color(0xFFCBD5E1)
    val fillColor = if (isSelected) ScanPrimary else Color.Transparent
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(fillColor)
            .border(width = 1.5.dp, color = ringColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
internal fun ScanResultFieldLabel(
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
            color = ScanTextPrimary,
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
