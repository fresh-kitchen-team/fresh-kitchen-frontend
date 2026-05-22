package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.myfrigelocal.ui.theme.FreshGreen
import com.example.myfrigelocal.ui.theme.FreshGreenDark
import com.example.myfrigelocal.ui.theme.LightGray
import com.example.myfrigelocal.viewmodel.*

// 상태별 색상
val StatusFreshColor = Color(0xFF22C55E)
val StatusNearExpiryColor = Color(0xFFF97316)
val StatusExpiredColor = Color(0xFFEF4444)
val StatusFreshBgColor = Color(0xFFDCFCE7)
val StatusNearExpiryBgColor = Color(0xFFFFF7ED)
val StatusExpiredBgColor = Color(0xFFFEF2F2)

// ───────────────────────────────────────────
// ViewModel 연결 진입점 (실제 앱에서 사용)
// ───────────────────────────────────────────
@Composable
fun InventoryListScreen(
    initialFilter: String = "all",
    onBackClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSelectModeChange: (Boolean) -> Unit = {},
    viewModel: InventoryListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 선택 모드 변경 시 상위(MainScaffold)에 알림 → FAB 숨김/표시 제어
    LaunchedEffect(uiState.isSelectMode) {
        onSelectModeChange(uiState.isSelectMode)
    }

    LaunchedEffect(initialFilter) {
        val filter = when (initialFilter) {
            "fridge"     -> InventoryFilter.FRIDGE
            "freezer"    -> InventoryFilter.FREEZER
            "pantry"     -> InventoryFilter.PANTRY
            "near_expiry" -> InventoryFilter.NEAR_EXPIRY
            "expired"    -> InventoryFilter.EXPIRED
            "recent"     -> InventoryFilter.RECENT
            else         -> InventoryFilter.ALL
        }
        viewModel.onFilterSelected(filter)
    }

    InventoryListContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToSettings = onNavigateToSettings,
        onFilterSelected = { viewModel.onFilterSelected(it) },
        onUpdateItem = { viewModel.updateItem(it) },
        onToggleSelectMode = { viewModel.toggleSelectMode() },
        onToggleItemSelection = { viewModel.toggleItemSelection(it) },
        onDeleteSelected = { viewModel.deleteSelected() },
        onConsumeSelected = { viewModel.consumeSelected() },
    )
}

// ───────────────────────────────────────────
// 실제 UI (프리뷰/테스트용으로 분리)
// ───────────────────────────────────────────
@Composable
fun InventoryListContent(
    uiState: InventoryListUiState,
    onBackClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onFilterSelected: (InventoryFilter) -> Unit = {},
    onUpdateItem: (FoodItem) -> Unit = {},
    onToggleSelectMode: () -> Unit = {},
    onToggleItemSelection: (Int) -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onConsumeSelected: () -> Unit = {},
) {
    // 상세 팝업 상태 (선택 모드가 아닐 때만 열림)
    var selectedItem by remember { mutableStateOf<FoodItem?>(null) }
    // 수정 팝업 상태
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }

    // 선택 모드 진입 시 팝업 닫기
    LaunchedEffect(uiState.isSelectMode) {
        if (uiState.isSelectMode) {
            selectedItem = null
            editingItem = null
        }
    }

    // 상세 팝업
    selectedItem?.let { item ->
        FoodItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onEdit = {
                selectedItem = null
                editingItem = it
            }
        )
    }

    // 수정 팝업
    editingItem?.let { item ->
        FoodItemEditDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { updatedItem ->
                onUpdateItem(updatedItem)
                editingItem = null
            }
        )
    }

    Scaffold(
        topBar = {
            InventoryTopBar(
                title = when (uiState.selectedFilter) {
                    InventoryFilter.ALL        -> "전체 식재료"
                    InventoryFilter.FRIDGE     -> "냉장실"
                    InventoryFilter.FREEZER    -> "냉동실"
                    InventoryFilter.PANTRY     -> "팬트리"
                    InventoryFilter.RECENT     -> "최근 추가"
                    InventoryFilter.NEAR_EXPIRY -> "소비 임박"
                    InventoryFilter.EXPIRED    -> "유통기한 경과"
                },
                onBackClick = onBackClick,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        // 선택 모드일 때 하단 액션 바 표시
        bottomBar = {
            if (uiState.isSelectMode) {
                SelectionActionBar(
                    selectedCount = uiState.selectedItemIds.size,
                    isProcessing = uiState.isProcessing,
                    onConsume = onConsumeSelected,
                    onDispose = onDeleteSelected,
                )
            }
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
                onFilterSelected = onFilterSelected
            )

            // 요약 카드 (선택하기 버튼 포함)
            SummaryCard(
                label = when (uiState.selectedFilter) {
                    InventoryFilter.ALL        -> "전체"
                    InventoryFilter.FRIDGE     -> "냉장실"
                    InventoryFilter.FREEZER    -> "냉동실"
                    InventoryFilter.PANTRY     -> "팬트리"
                    InventoryFilter.RECENT     -> "최근 추가"
                    InventoryFilter.NEAR_EXPIRY -> "소비 임박"
                    InventoryFilter.EXPIRED    -> "유통기한 경과"
                },
                totalCount = uiState.totalCount,
                freshCount = uiState.freshCount,
                nearExpiryCount = uiState.nearExpiryCount,
                expiredCount = uiState.expiredCount,
                isSelectMode = uiState.isSelectMode,
                onToggleSelectMode = onToggleSelectMode,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 식재료 리스트
            if (uiState.selectedFilter == InventoryFilter.NEAR_EXPIRY ||
                uiState.selectedFilter == InventoryFilter.EXPIRED
            ) {
                GroupedFoodList(
                    items = uiState.filteredItems,
                    onItemClick = { if (!uiState.isSelectMode) selectedItem = it },
                    isSelectMode = uiState.isSelectMode,
                    selectedItemIds = uiState.selectedItemIds,
                    onToggleSelection = onToggleItemSelection,
                )
            } else {
                FoodItemList(
                    items = uiState.filteredItems,
                    onItemClick = { if (!uiState.isSelectMode) selectedItem = it },
                    isSelectMode = uiState.isSelectMode,
                    selectedItemIds = uiState.selectedItemIds,
                    onToggleSelection = onToggleItemSelection,
                )
            }
        }
    }
}

// ───────────────────────────────────────────
// 상단 앱바
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTopBar(
    title: String,
    onBackClick: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
        },
        actions = {
            IconButton(onClick = onNavigateToProfile) { Icon(Icons.Default.Person, contentDescription = "프로필") }
            IconButton(onClick = onNavigateToSearch) { Icon(Icons.Default.Search, contentDescription = "검색") }
            IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, contentDescription = "설정") }
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
        InventoryFilter.ALL        to "전체",
        InventoryFilter.FRIDGE     to "냉장실",
        InventoryFilter.FREEZER    to "냉동실",
        InventoryFilter.PANTRY     to "팬트리",
        InventoryFilter.RECENT     to "최근 추가",
        InventoryFilter.NEAR_EXPIRY to "소비임박",
        InventoryFilter.EXPIRED    to "유통기한경과",
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
    expiredCount: Int,
    isSelectMode: Boolean = false,
    onToggleSelectMode: () -> Unit = {},
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
        // 왼쪽: 라벨 + 총 개수
        Column {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text(
                text = "$totalCount",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 오른쪽 하단: 상태 뱃지
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusBadgeSmall("신선 $freshCount", StatusFreshColor)
            StatusBadgeSmall("임박 $nearExpiryCount", StatusNearExpiryColor)
            StatusBadgeSmall("경과 $expiredCount", StatusExpiredColor)
        }
    }

    // 선택하기 버튼 — 카드 아래 오른쪽 정렬
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = onToggleSelectMode,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isSelectMode) "✕ 취소" else "선택하기",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FreshGreenDark
            )
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
// 하단 선택 액션 바 (소비 / 폐기)
// ───────────────────────────────────────────
@Composable
fun SelectionActionBar(
    selectedCount: Int,
    isProcessing: Boolean,
    onConsume: () -> Unit,
    onDispose: () -> Unit,
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCount > 0) "${selectedCount}개 선택됨" else "항목을 선택하세요",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = if (selectedCount > 0) Color.Black else Color.Gray,
                modifier = Modifier.weight(1f)
            )

            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = FreshGreenDark,
                    strokeWidth = 3.dp
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 소비 버튼 (녹색 outlined)
                    OutlinedButton(
                        onClick = onConsume,
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FreshGreenDark,
                            disabledContentColor = Color.LightGray
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedCount > 0) FreshGreenDark else Color.LightGray
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("소비", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    // 폐기 버튼 (빨간 filled)
                    Button(
                        onClick = onDispose,
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("폐기", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 일반 식재료 리스트
// ───────────────────────────────────────────
@Composable
fun FoodItemList(
    items: List<FoodItem>,
    onItemClick: (FoodItem) -> Unit = {},
    isSelectMode: Boolean = false,
    selectedItemIds: Set<Int> = emptySet(),
    onToggleSelection: (Int) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            FoodItemCard(
                item = item,
                onClick = { onItemClick(item) },
                isSelectMode = isSelectMode,
                isSelected = item.id in selectedItemIds,
                onToggleSelection = { onToggleSelection(item.id) }
            )
        }
    }
}

// ───────────────────────────────────────────
// 저장공간별 그룹핑 리스트 (임박/경과용)
// ───────────────────────────────────────────
@Composable
fun GroupedFoodList(
    items: List<FoodItem>,
    onItemClick: (FoodItem) -> Unit = {},
    isSelectMode: Boolean = false,
    selectedItemIds: Set<Int> = emptySet(),
    onToggleSelection: (Int) -> Unit = {},
) {
    val grouped = mapOf(
        StorageType.FRIDGE  to (items.filter { it.storage == StorageType.FRIDGE }  to ("🥛" to "냉장실")),
        StorageType.FREEZER to (items.filter { it.storage == StorageType.FREEZER } to ("❄️" to "냉동실")),
        StorageType.PANTRY  to (items.filter { it.storage == StorageType.PANTRY }  to ("🥫" to "팬트리")),
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
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${groupItems.size}", fontSize = 14.sp, color = Color.Gray)
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
                    FoodItemCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        isSelectMode = isSelectMode,
                        isSelected = item.id in selectedItemIds,
                        onToggleSelection = { onToggleSelection(item.id) }
                    )
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 식재료 카드 (선택 모드 지원)
// ───────────────────────────────────────────
@Composable
fun FoodItemCard(
    item: FoodItem,
    onClick: () -> Unit = {},
    isSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
) {
    val (statusText, statusTextColor, statusBgColor) = when (item.status) {
        FoodStatus.FRESH       -> Triple("신선", StatusFreshColor, StatusFreshBgColor)
        FoodStatus.NEAR_EXPIRY -> Triple("소비임박", StatusNearExpiryColor, StatusNearExpiryBgColor)
        FoodStatus.EXPIRED     -> Triple("유통기한경과", StatusExpiredColor, StatusExpiredBgColor)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectMode) onToggleSelection() else onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // 선택된 카드는 연한 녹색 배경
            containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
        ),
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

            // 이름 + 카테고리 + 유통기한
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Medium, fontSize = 15.sp)

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StorageBadge(
                        text = when (item.storage) {
                            StorageType.FRIDGE  -> "냉장실"
                            StorageType.FREEZER -> "냉동실"
                            StorageType.PANTRY  -> "팬트리"
                            StorageType.ALL     -> ""
                        }
                    )
                    Text(item.category, fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "유통기한: ${item.expiryDate}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // 오른쪽: 선택 모드면 체크박스, 아니면 상태 뱃지
            if (isSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = FreshGreenDark,
                        uncheckedColor = Color.Gray
                    )
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusText,
                        color = statusTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
@Preview(showBackground = true, showSystemUi = true, name = "전체 리스트")
@Composable
fun InventoryListPreview() {
    val dummyState = InventoryListUiState(
        selectedFilter = InventoryFilter.ALL,
        totalCount = 42,
        freshCount = 35,
        nearExpiryCount = 5,
        expiredCount = 2,
        filteredItems = listOf(
            FoodItem(1, "신선한 우유", "유제품", StorageType.FRIDGE, "1L", "2026-03-25", FoodStatus.FRESH, "🥛"),
            FoodItem(2, "소고기 안심", "육류", StorageType.FREEZER, "500g", "2026-04-15", FoodStatus.FRESH, "🥩"),
            FoodItem(3, "유기농 브로콜리", "채소", StorageType.FRIDGE, "1개", "2026-03-28", FoodStatus.FRESH, "🥦"),
            FoodItem(4, "계란", "유제품", StorageType.FRIDGE, "10개", "2026-03-24", FoodStatus.NEAR_EXPIRY, "🥚"),
            FoodItem(5, "토마토", "채소", StorageType.FRIDGE, "5개", "2026-03-23", FoodStatus.NEAR_EXPIRY, "🍅"),
            FoodItem(6, "햄", "육류", StorageType.FRIDGE, "300g", "2026-03-20", FoodStatus.EXPIRED, "🍖"),
        )
    )
    InventoryListContent(uiState = dummyState)
}

@Preview(showBackground = true, showSystemUi = true, name = "선택 모드")
@Composable
fun InventoryListSelectModePreview() {
    val dummyState = InventoryListUiState(
        selectedFilter = InventoryFilter.ALL,
        totalCount = 6,
        freshCount = 4,
        nearExpiryCount = 1,
        expiredCount = 1,
        isSelectMode = true,
        selectedItemIds = setOf(1, 3),
        filteredItems = listOf(
            FoodItem(1, "신선한 우유", "유제품", StorageType.FRIDGE, "1L", "2026-03-25", FoodStatus.FRESH, "🥛"),
            FoodItem(2, "소고기 안심", "육류", StorageType.FREEZER, "500g", "2026-04-15", FoodStatus.FRESH, "🥩"),
            FoodItem(3, "유기농 브로콜리", "채소", StorageType.FRIDGE, "1개", "2026-03-28", FoodStatus.FRESH, "🥦"),
            FoodItem(4, "계란", "유제품", StorageType.FRIDGE, "10개", "2026-03-24", FoodStatus.NEAR_EXPIRY, "🥚"),
            FoodItem(6, "햄", "육류", StorageType.FRIDGE, "300g", "2026-03-20", FoodStatus.EXPIRED, "🍖"),
        )
    )
    InventoryListContent(uiState = dummyState)
}
