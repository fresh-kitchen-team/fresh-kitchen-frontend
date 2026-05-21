package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.viewmodel.ConsumptionDdayTone
import com.example.myfrigelocal.viewmodel.ConsumptionItemUi
import com.example.myfrigelocal.viewmodel.ConsumptionStorageFilter
import com.example.myfrigelocal.viewmodel.ConsumptionViewModel

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ConsumptionDetailScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ConsumptionViewModel = viewModel(),
) {
    val background = Color(0xFFF6F8F7)
    val accent = BottomNavSelected
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedFilter by rememberSaveable { mutableStateOf(ConsumptionStorageFilter.All) }

    val filteredItems = remember(uiState.items, selectedFilter) {
        if (selectedFilter == ConsumptionStorageFilter.All) {
            uiState.items
        } else {
            uiState.items.filter { it.storageType.equals(selectedFilter.apiValue, ignoreCase = true) }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    // 리스트가 비어있지 않을 때 발생한 에러(주로 삭제 실패)는 Snackbar 로 노출.
    // 리스트가 비어있는 로드 실패는 본문 ErrorCard 가 처리.
    LaunchedEffect(uiState.error) {
        val msg = uiState.error
        if (!msg.isNullOrBlank() && uiState.items.isNotEmpty()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ConsumptionDetailTopBar(
                title = "소비 권장 알림",
                onBackClick = { navController.popBackStack() },
                containerColor = Color(0xFFF1F6F6),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SummaryCard(
                        count = uiState.items.size,
                        accent = accent,
                    )
                }

                item {
                    FilterRow(
                        selected = selectedFilter,
                        onSelect = { selectedFilter = it },
                    )
                }

                when {
                    uiState.error != null && uiState.items.isEmpty() -> {
                        item {
                            ErrorCard(
                                message = uiState.error.orEmpty(),
                                onRetry = { viewModel.loadExpiringItems() },
                            )
                        }
                    }
                    !uiState.isLoading && filteredItems.isEmpty() -> {
                        item {
                            EmptyCard(filter = selectedFilter)
                        }
                    }
                    else -> {
                        items(
                            items = filteredItems,
                            key = { it.id },
                        ) { item ->
                            ConsumptionItemCard(
                                modifier = Modifier.animateItemPlacement(),
                                item = item,
                                accent = accent,
                                isConsuming = item.id in uiState.consumingIds,
                                onConsumeComplete = { viewModel.consumeItem(item.id) },
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.items.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp),
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun ConsumptionDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF101418),
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101418),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SummaryCard(
    count: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val bg = Color(0xFFEAF7F2)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (count > 0) {
                    "총 ${count}개의 품목의 유통기한이\n7일 이내입니다."
                } else {
                    "유통기한이 7일 이내로 남은\n식재료가 없습니다."
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101418),
            )
        }
    }
}

@Composable
private fun FilterRow(
    selected: ConsumptionStorageFilter,
    onSelect: (ConsumptionStorageFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(ConsumptionStorageFilter.entries, key = { it.name }) { filter ->
            FilterChip(
                text = filter.label,
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) Color(0xFF101418) else Color(0xFFE8ECEF)
    val fg = if (selected) Color.White else Color(0xFF5D6972)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
private fun ConsumptionItemCard(
    item: ConsumptionItemUi,
    accent: Color,
    onConsumeComplete: () -> Unit,
    modifier: Modifier = Modifier,
    isConsuming: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ItemEmojiBox(emoji = item.emoji)
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF101418),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.storageLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7680),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (!item.expiryDate.isNullOrBlank()) {
                            "유통기한: ${item.expiryDate}"
                        } else {
                            "유통기한: -"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7680),
                    )
                }

                DdayBadge(
                    text = item.ddayLabel,
                    tone = item.ddayTone,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ConsumeButton(
                text = if (isConsuming) "처리 중..." else "소비 완료",
                accent = accent,
                isLoading = isConsuming,
                onClick = onConsumeComplete,
            )
        }
    }
}

@Composable
private fun ItemEmojiBox(
    emoji: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF1F3F5))
            .border(1.dp, Color(0xFFE5EAEE), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (!emoji.isNullOrBlank()) {
            Text(
                text = emoji,
                fontSize = 26.sp,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = Color(0xFF9AA5AE),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun DdayBadge(
    text: String,
    tone: ConsumptionDdayTone,
    modifier: Modifier = Modifier,
) {
    val fg = when (tone) {
        ConsumptionDdayTone.Critical -> Color(0xFFFF3B30)
        ConsumptionDdayTone.Warning -> Color(0xFFFF6A00)
        ConsumptionDdayTone.Info -> Color(0xFF22A87E)
    }
    val bg = fg.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
private fun ConsumeButton(
    text: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val backgroundColor = if (isLoading) accent.copy(alpha = 0.6f) else accent
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun EmptyCard(
    filter: ConsumptionStorageFilter,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = if (filter == ConsumptionStorageFilter.All) {
                "유통기한이 7일 이내로 남은 식재료가 없습니다."
            } else {
                "${filter.label}에 임박한 식재료가 없습니다."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9AA4AE),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD64545),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFD64545))
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "다시 시도",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}
