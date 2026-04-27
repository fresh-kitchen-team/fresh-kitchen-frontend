package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfrigelocal.viewmodel.*
import com.example.myfrigelocal.ui.theme.FreshGreen
import com.example.myfrigelocal.ui.theme.FreshGreenDark
import com.example.myfrigelocal.ui.theme.LightGray
import androidx.compose.runtime.LaunchedEffect


// 상태별 색상
val StatusFreshColor = Color(0xFF22C55E)
val StatusNearExpiryColor = Color(0xFFF97316)
val StatusExpiredColor = Color(0xFFEF4444)
val StatusFreshBgColor = Color(0xFFDCFCE7)
val StatusNearExpiryBgColor = Color(0xFFFFF7ED)
val StatusExpiredBgColor = Color(0xFFFEF2F2)

// ───────────────────────────────────────────
// 재고 리스트 스크린
// ───────────────────────────────────────────
@Composable
fun InventoryListScreen(
    initialFilter: String = "all",
    onBackClick: () -> Unit = {},
    viewModel: InventoryListViewModel = viewModel(),
) {
    // 초기 필터 적용 ← 추가
    LaunchedEffect(initialFilter) {
        val filter = when (initialFilter) {
            "fridge" -> InventoryFilter.FRIDGE
            "freezer" -> InventoryFilter.FREEZER
            "pantry" -> InventoryFilter.PANTRY
            "recent" -> InventoryFilter.RECENT
            "near_expiry" -> InventoryFilter.NEAR_EXPIRY
            "expired" -> InventoryFilter.EXPIRED
            else -> InventoryFilter.ALL
        }
        viewModel.onFilterSelected(filter)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            InventoryTopBar(
                title = when (uiState.selectedFilter) {
                    InventoryFilter.ALL -> "전체 식재료"
                    InventoryFilter.FRIDGE -> "냉장실"
                    InventoryFilter.FREEZER -> "냉동실"
                    InventoryFilter.PANTRY -> "팬트리"
                    InventoryFilter.RECENT -> "최근 추가"
                    InventoryFilter.NEAR_EXPIRY -> "소비 임박"
                    InventoryFilter.EXPIRED -> "유통기한 경과"
                },
                onBackClick = onBackClick
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 필터 탭
            FilterTabRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.onFilterSelected(it) }
            )

            // 요약 카드
            SummaryCard(
                label = when (uiState.selectedFilter) {
                    InventoryFilter.ALL -> "전체"
                    InventoryFilter.FRIDGE -> "냉장실"
                    InventoryFilter.FREEZER -> "냉동실"
                    InventoryFilter.PANTRY -> "팬트리"
                    InventoryFilter.RECENT -> "최근 추가"
                    InventoryFilter.NEAR_EXPIRY -> "소비 임박"
                    InventoryFilter.EXPIRED -> "유통기한 경과"
                },
                totalCount = uiState.totalCount,
                freshCount = uiState.freshCount,
                nearExpiryCount = uiState.nearExpiryCount,
                expiredCount = uiState.expiredCount
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 식재료 리스트
            if (uiState.selectedFilter == InventoryFilter.NEAR_EXPIRY ||
                uiState.selectedFilter == InventoryFilter.EXPIRED
            ) {
                // 임박/경과는 저장공간별 그룹핑
                GroupedFoodList(items = uiState.filteredItems)
            } else {
                // 나머지는 일반 리스트
                FoodItemList(items = uiState.filteredItems)
            }
        }
    }
}

// ───────────────────────────────────────────
// 상단 앱바
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
        },
        actions = {
            IconButton(onClick = {}) { Icon(Icons.Default.Person, contentDescription = "프로필") }
            IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "검색") }
            IconButton(onClick = {}) { Icon(Icons.Default.Settings, contentDescription = "설정") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// ───────────────────────────────────────────
// 필터 탭 (가로 스크롤)
// ───────────────────────────────────────────
@Composable
fun FilterTabRow(
    selectedFilter: InventoryFilter,
    onFilterSelected: (InventoryFilter) -> Unit
) {
    val tabs = listOf(
        InventoryFilter.ALL to "전체",
        InventoryFilter.FRIDGE to "냉장실",
        InventoryFilter.FREEZER to "냉동실",
        InventoryFilter.PANTRY to "팬트리",
        InventoryFilter.RECENT to "최근 추가",
        InventoryFilter.NEAR_EXPIRY to "소비임박",
        InventoryFilter.EXPIRED to "유통기한경과",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (filter, label) ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(label, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FreshGreenDark,
                    selectedLabelColor = Color.White,
                    containerColor = LightGray,
                    labelColor = Color.DarkGray
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

// ───────────────────────────────────────────
// 요약 카드 (그린 그라데이션)
// ───────────────────────────────────────────
@Composable
fun SummaryCard(
    label: String,
    totalCount: Int,
    freshCount: Int,
    nearExpiryCount: Int,
    expiredCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(FreshGreen, FreshGreenDark)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text(
                text = "$totalCount",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusBadgeSmall("신선 $freshCount", StatusFreshColor)
            StatusBadgeSmall("임박 $nearExpiryCount", StatusNearExpiryColor)
            StatusBadgeSmall("경과 $expiredCount", StatusExpiredColor)
        }
    }
}

@Composable
fun StatusBadgeSmall(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ───────────────────────────────────────────
// 일반 식재료 리스트
// ───────────────────────────────────────────
@Composable
fun FoodItemList(items: List<FoodItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            FoodItemCard(item = item)
        }
    }
}

// ───────────────────────────────────────────
// 저장공간별 그룹핑 리스트 (임박/경과용)
// ───────────────────────────────────────────
@Composable
fun GroupedFoodList(items: List<FoodItem>) {
    val grouped = mapOf(
        StorageType.FRIDGE to (items.filter { it.storage == StorageType.FRIDGE } to ("🥛" to "냉장실")),
        StorageType.FREEZER to (items.filter { it.storage == StorageType.FREEZER } to ("❄️" to "냉동실")),
        StorageType.PANTRY to (items.filter { it.storage == StorageType.PANTRY } to ("🥫" to "팬트리")),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (_, pair) ->
            val (groupItems, info) = pair
            val (emoji, label) = info

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${groupItems.size}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            if (groupItems.isEmpty()) {
                item {
                    Text(
                        text = "해당 항목 없음",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                }
            } else {
                items(groupItems) { item ->
                    FoodItemCard(item = item)
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 식재료 카드
// ───────────────────────────────────────────
@Composable
fun FoodItemCard(item: FoodItem) {
    val (statusText, statusTextColor, statusBgColor) = when (item.status) {
        FoodStatus.FRESH -> Triple("신선", StatusFreshColor, StatusFreshBgColor)
        FoodStatus.NEAR_EXPIRY -> Triple("소비임박", StatusNearExpiryColor, StatusNearExpiryBgColor)
        FoodStatus.EXPIRED -> Triple("유통기한경과", StatusExpiredColor, StatusExpiredBgColor)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이모지 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 이름 + 카테고리 + 수량/유통기한
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Medium, fontSize = 15.sp)

                Spacer(modifier = Modifier.height(4.dp))

                // 저장공간 + 카테고리 뱃지
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StorageBadge(
                        text = when (item.storage) {
                            StorageType.FRIDGE -> "냉장실"
                            StorageType.FREEZER -> "냉동실"
                            StorageType.PANTRY -> "팬트리"
                            StorageType.ALL -> ""
                        }
                    )
                    Text(item.category, fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "수량: ${item.amount}  |  유통기한: ${item.expiryDate}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // 상태 뱃지
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBgColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(statusText, color = statusTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun StorageBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 11.sp, color = FreshGreenDark, fontWeight = FontWeight.Medium)
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InventoryListScreenPreview() {
    InventoryListScreen()
}