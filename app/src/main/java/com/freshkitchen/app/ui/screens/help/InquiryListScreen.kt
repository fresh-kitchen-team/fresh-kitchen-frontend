package com.freshkitchen.app.ui.screens.help

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshkitchen.app.ui.screens.chat.ChatDesign
import com.freshkitchen.app.ui.theme.BottomNavSelected
import com.freshkitchen.app.ui.theme.MyFrigeLocalTheme
import com.freshkitchen.app.viewmodel.InquiryListViewModel

@Composable
fun InquiryListScreen(
    onClose: () -> Unit,
    onAnsweredItemClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: InquiryListViewModel = viewModel(),
) {
    BackHandler { onClose() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChatDesign.ScreenBg),
    ) {
        InquiryListTopBar(
            onBack = onClose,
            onRefresh = { viewModel.load() },
            isRefreshing = uiState.isLoading && uiState.items.isNotEmpty(),
        )

        when {
            uiState.isLoading && uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = ChatDesign.ChatPrimary,
                        strokeWidth = 2.5.dp,
                    )
                }
            }

            uiState.error != null && uiState.items.isEmpty() -> {
                InquiryListError(
                    message = uiState.error!!,
                    onRetry = { viewModel.load() },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            uiState.items.isEmpty() -> {
                InquiryListEmpty(modifier = Modifier.fillMaxSize())
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.error != null) {
                        item(key = "error_banner") {
                            InquiryListInlineError(
                                message = uiState.error!!,
                                onDismiss = viewModel::dismissError,
                                onRetry = { viewModel.load() },
                            )
                        }
                    }

                    item(key = "header_hint") {
                        Text(
                            text = "총 ${uiState.items.size}건 · 최신순",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChatDesign.TextMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        )
                    }

                    items(
                        items = uiState.items,
                        key = { it.id },
                    ) { item ->
                        InquiryListRow(
                            item = item,
                            onClick = if (item.isAnswered) {
                                { onAnsweredItemClick(item.id) }
                            } else {
                                null
                            },
                        )
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InquiryListTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChatDesign.SurfaceWhite,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "뒤로",
                        tint = ChatDesign.TextPrimary,
                    )
                }
                Text(
                    text = "내 문의 내역",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChatDesign.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "새로고침",
                        tint = if (isRefreshing) ChatDesign.TextMuted else ChatDesign.TextSecondary,
                    )
                }
            }
            androidx.compose.material3.HorizontalDivider(thickness = 1.dp, color = ChatDesign.BorderSoft)
        }
    }
}

@Composable
private fun InquiryListRow(
    item: InquiryListItemUi,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val typeBg = if (item.isReport) Color(0xFFFDECEC) else Color(0xFFDFF7ED)
    val typeTint = if (item.isReport) Color(0xFFE24A4A) else BottomNavSelected
    val statusBg = if (item.isAnswered) Color(0xFFE8FAF2) else Color(0xFFF3F4F6)
    val statusTint = if (item.isAnswered) Color(0xFF059669) else Color(0xFF6B7280)
    val borderColor = if (item.isAnswered) Color(0xFFBBF7D0) else ChatDesign.BorderSoft

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ChatDesign.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (item.isAnswered) 1.5.dp else 1.dp,
            color = borderColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InquiryChip(text = item.typeLabel, background = typeBg, textColor = typeTint)
                InquiryChip(
                    text = item.categoryLabel,
                    background = Color(0xFFF6F8F7),
                    textColor = ChatDesign.TextSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                InquiryChip(text = item.statusLabel, background = statusBg, textColor = statusTint)
            }

            Text(
                text = item.contentPreview.ifBlank { "(내용 없음)" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                ),
                color = ChatDesign.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.createdAtLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = ChatDesign.TextMuted,
                    modifier = Modifier.weight(1f),
                )
                if (onClick != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "답변 보기",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BottomNavSelected,
                        )
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "상세 보기",
                            tint = BottomNavSelected,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InquiryChip(
    text: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = textColor,
    )
}

@Composable
private fun InquiryListEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFDFF7ED), RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = BottomNavSelected,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "아직 문의 내역이 없어요",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = ChatDesign.TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "문의 보내기에서 궁금한 점을 남기면\n여기에서 확인할 수 있어요.",
            style = MaterialTheme.typography.bodyMedium,
            color = ChatDesign.TextSecondary,
        )
    }
}

@Composable
private fun InquiryListError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ChatDesign.ErrorText,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text(
                text = "다시 시도",
                color = ChatDesign.ChatPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun InquiryListInlineError(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ChatDesign.ErrorBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.ErrorBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = ChatDesign.ErrorText,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) {
                Text("재시도", color = ChatDesign.ErrorText, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onDismiss) {
                Text("닫기", color = ChatDesign.TextSecondary)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun InquiryListRowPreview() {
    MyFrigeLocalTheme {
        InquiryListRow(
            item = InquiryListItemUi(
                id = 1L,
                typeLabel = "문의",
                categoryLabel = "AI",
                contentPreview = "레시피 추천이 잘못된 것 같습니다. 토마토가 없는데 포함돼요.",
                statusLabel = "답변 완료",
                isAnswered = true,
                isReport = false,
                createdAtLabel = "오늘 14:32",
                sortKey = 0L,
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
