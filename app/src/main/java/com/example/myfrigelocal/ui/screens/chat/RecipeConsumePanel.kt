package com.example.myfrigelocal.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme
import kotlinx.coroutines.launch

private val PanelShape = RoundedCornerShape(16.dp)
private val RowShape = RoundedCornerShape(12.dp)

@Composable
fun RecipeConsumeSection(
    recipe: RecipeUiModel,
    stateKey: String,
    modifier: Modifier = Modifier,
    onEnrichItems: suspend (List<RecipeMatchedItemUi>) -> List<RecipeMatchedItemUi> = { it },
    onConsume: suspend (List<RecipeMatchedItemUi>) -> Result<Int> = {
        Result.failure(UnsupportedOperationException())
    },
) {
    val sourceItems = recipe.matchedItems
    if (sourceItems.isEmpty()) return

    var isPanelOpen by rememberSaveable(stateKey) { mutableStateOf(false) }
    var selectedRowKeys by rememberSaveable(stateKey) { mutableStateOf(listOf<String>()) }
    var showSuccess by rememberSaveable(stateKey) { mutableStateOf(false) }
    var successCount by rememberSaveable(stateKey) { mutableStateOf(0) }
    var enrichedItems by remember(sourceItems) { mutableStateOf(sourceItems) }
    var isLoadingInventory by remember { mutableStateOf(false) }
    var isConsuming by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable(stateKey) { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val availableRowKeys = remember(enrichedItems) {
        enrichedItems.filter { it.isAvailable }.map { it.rowKey }.toSet()
    }

    LaunchedEffect(isPanelOpen, sourceItems) {
        if (!isPanelOpen) return@LaunchedEffect
        isLoadingInventory = true
        errorMessage = null
        enrichedItems = onEnrichItems(sourceItems)
        isLoadingInventory = false
        if (selectedRowKeys.isEmpty()) {
            selectedRowKeys = enrichedItems.filter { it.isAvailable }.map { it.rowKey }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(10.dp))

        if (showSuccess) {
            ConsumeSuccessBanner(
                count = successCount,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            RecipeConsumeTriggerCard(
                isOpen = isPanelOpen,
                itemCount = enrichedItems.count { it.isAvailable },
                onClick = {
                    isPanelOpen = !isPanelOpen
                    errorMessage = null
                },
            )

            AnimatedVisibility(
                visible = isPanelOpen,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    errorMessage?.let { err ->
                        ConsumeErrorBanner(
                            message = err,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        )
                    }

                    if (isLoadingInventory) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = PanelShape,
                            color = ChatDesign.SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = ChatDesign.ChatPrimary,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "저장소 재고 확인 중...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ChatDesign.TextSecondary,
                                )
                            }
                        }
                    } else {
                        RecipeConsumeSelectionPanel(
                            items = enrichedItems,
                            selectedRowKeys = selectedRowKeys.toSet(),
                            isConsuming = isConsuming,
                            onToggleItem = { rowKey, checked ->
                                selectedRowKeys = if (checked) {
                                    selectedRowKeys + rowKey
                                } else {
                                    selectedRowKeys - rowKey
                                }
                            },
                            onSelectAll = { selectAll ->
                                selectedRowKeys = if (selectAll) {
                                    availableRowKeys.toList()
                                } else {
                                    emptyList()
                                }
                            },
                            onCancel = {
                                isPanelOpen = false
                                errorMessage = null
                                selectedRowKeys = availableRowKeys.toList()
                            },
                            onConsume = {
                                val selected = enrichedItems.filter { it.rowKey in selectedRowKeys && it.isAvailable }
                                if (selected.isEmpty()) {
                                    errorMessage = "소비할 재료를 선택해 주세요."
                                    return@RecipeConsumeSelectionPanel
                                }
                                scope.launch {
                                    isConsuming = true
                                    errorMessage = null
                                    onConsume(selected)
                                        .onSuccess { count ->
                                            successCount = count
                                            isPanelOpen = false
                                            showSuccess = true
                                        }
                                        .onFailure { e ->
                                            errorMessage = e.message ?: "재료 소비 처리에 실패했습니다."
                                        }
                                    isConsuming = false
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeConsumeTriggerCard(
    isOpen: Boolean,
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = PanelShape,
        color = ChatDesign.SurfaceWhite,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8FAF2)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = ChatDesign.ChatPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "사용한 재료를 저장소에서 소비할까요?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                    color = ChatDesign.TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (itemCount > 0) "보유 재료 ${itemCount}개 · 탭하여 선택" else "매칭된 재료 없음",
                    style = MaterialTheme.typography.labelSmall,
                    color = ChatDesign.TextMuted,
                )
            }
            Icon(
                imageVector = if (isOpen) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = ChatDesign.TextSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun RecipeConsumeSelectionPanel(
    items: List<RecipeMatchedItemUi>,
    selectedRowKeys: Set<String>,
    isConsuming: Boolean,
    onToggleItem: (rowKey: String, checked: Boolean) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onConsume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val availableItems = items.filter { it.isAvailable }
    val allSelected = availableItems.isNotEmpty() &&
        availableItems.all { it.rowKey in selectedRowKeys }
    val selectedCount = items.count { it.rowKey in selectedRowKeys && it.isAvailable }
    val canConsume = selectedCount > 0 && !isConsuming

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PanelShape,
        color = ChatDesign.SurfaceWhite,
        shadowElevation = 3.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "소비할 재료 선택",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    ),
                    color = ChatDesign.TextPrimary,
                )
                Text(
                    text = "유통기한이 가장 임박한 재고부터 소비됩니다",
                    style = MaterialTheme.typography.labelSmall,
                    color = ChatDesign.TextMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (availableItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RowShape)
                        .clickable(enabled = !isConsuming) { onSelectAll(!allSelected) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = onSelectAll,
                        enabled = !isConsuming,
                        colors = CheckboxDefaults.colors(
                            checkedColor = ChatDesign.ChatPrimary,
                            uncheckedColor = ChatDesign.TextMuted,
                            checkmarkColor = Color.White,
                        ),
                    )
                    Text(
                        text = "전체 선택",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = ChatDesign.TextSecondary,
                    )
                }

                HorizontalDivider(color = ChatDesign.BorderSoft)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items.forEach { item ->
                    MatchedItemRow(
                        item = item,
                        checked = item.rowKey in selectedRowKeys,
                        enabled = !isConsuming,
                        onCheckedChange = { checked -> onToggleItem(item.rowKey, checked) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isConsuming,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ChatDesign.TextPrimary,
                    ),
                ) {
                    Text(
                        text = "취소",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    )
                }
                Button(
                    onClick = onConsume,
                    enabled = canConsume,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChatDesign.ChatPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = ChatDesign.ChatPrimary.copy(alpha = 0.35f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f),
                    ),
                ) {
                    if (isConsuming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Text(
                            text = if (selectedCount > 0) "소비 ($selectedCount)" else "소비",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchedItemRow(
    item: RecipeMatchedItemUi,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowEnabled = enabled && item.isAvailable
    val bg = when {
        !item.isAvailable -> Color(0xFFF9FAFB)
        checked -> Color(0xFFE8FAF2)
        else -> Color.Transparent
    }
    val borderColor = when {
        !item.isAvailable -> ChatDesign.BorderSoft
        checked -> Color(0xFFB8F0D4)
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RowShape)
            .background(bg)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, RowShape)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = rowEnabled) { onCheckedChange(!checked) }
            .padding(start = 4.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked && item.isAvailable,
            onCheckedChange = onCheckedChange,
            enabled = rowEnabled,
            colors = CheckboxDefaults.colors(
                checkedColor = ChatDesign.ChatPrimary,
                uncheckedColor = ChatDesign.TextMuted,
                checkmarkColor = Color.White,
                disabledCheckedColor = ChatDesign.TextMuted,
                disabledUncheckedColor = ChatDesign.BorderSoft,
            ),
        )

        val emoji = item.emoji
        if (!emoji.isNullOrBlank()) {
            Text(
                text = emoji,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF6F8F7)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.name.take(1),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ChatDesign.TextSecondary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = if (item.isAvailable) ChatDesign.TextPrimary else ChatDesign.TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!item.isAvailable) {
                Text(
                    text = "재고에서 찾을 수 없음",
                    style = MaterialTheme.typography.labelSmall,
                    color = ChatDesign.ErrorText.copy(alpha = 0.85f),
                )
            } else if (!item.storageLabel.isNullOrBlank()) {
                Text(
                    text = item.storageLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = ChatDesign.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun ConsumeSuccessBanner(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = PanelShape,
        color = Color(0xFFE8FAF2),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB8F0D4)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(22.dp),
            )
            Column {
                Text(
                    text = "재료 ${count}개 소비 처리됨",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF065F46),
                )
                Text(
                    text = "저장소에서 소비 처리되었습니다",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF047857),
                )
            }
        }
    }
}

@Composable
private fun ConsumeErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ChatDesign.ErrorBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.ErrorBorder),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = ChatDesign.ErrorText,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RecipeConsumeSectionPreview() {
    MyFrigeLocalTheme {
        RecipeConsumeSection(
            recipe = RecipeUiModel(
                title = "토마토 계란 볶음",
                cookTime = "10분",
                ingredients = listOf("계란", "토마토"),
                steps = emptyList(),
                tip = null,
                missingIngredients = listOf("소금"),
                imageUrl = "",
                matchedItems = listOf(
                    RecipeMatchedItemUi(itemId = 101, name = "계란", rowKey = "k1", emoji = "🥚", storageLabel = "냉장실"),
                    RecipeMatchedItemUi(itemId = 102, name = "토마토", rowKey = "k2", emoji = "🍅", storageLabel = "냉장실"),
                ),
            ),
            stateKey = "preview-consume",
            modifier = Modifier.padding(16.dp),
        )
    }
}
