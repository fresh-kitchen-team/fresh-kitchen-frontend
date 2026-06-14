package com.freshkitchen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.freshkitchen.app.ui.theme.FreshGreenDark
import com.freshkitchen.app.ui.theme.LightGray
import com.freshkitchen.app.ui.theme.WarnOrange
import com.freshkitchen.app.ui.theme.WarnRed
import com.freshkitchen.app.viewmodel.FoodItem
import com.freshkitchen.app.viewmodel.FoodStatus
import com.freshkitchen.app.viewmodel.SearchViewModel

// ───────────────────────────────────────────
// 검색 화면
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 상세 팝업 / 수정 팝업 상태
    var selectedItem by remember { mutableStateOf<FoodItem?>(null) }
    var itemToEdit by remember { mutableStateOf<FoodItem?>(null) }

    // 상세 팝업
    selectedItem?.let { item ->
        FoodItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onEdit = {
                selectedItem = null
                itemToEdit = it
            }
        )
    }

    // 수정 팝업
    itemToEdit?.let { item ->
        FoodItemEditDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                viewModel.updateItem(updatedItem)
                itemToEdit = null
            }
        )
    }

    // 화면 진입 시 키보드 자동 포커스
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("전체 검색", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "무엇을 찾으시나요?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── 검색 입력창 ──
            SearchInputField(
                query = uiState.query,
                onQueryChange = { viewModel.onQueryChange(it) },
                onSearch = {
                    viewModel.search()
                    keyboardController?.hide()
                },
                onClear = { viewModel.clearQuery() },
                focusRequester = focusRequester
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── 로딩 ──
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FreshGreenDark)
                }
            }
            // ── 검색 결과 ──
            else if (uiState.searchResults != null) {
                SearchResultsSection(
                    query = uiState.query,
                    results = uiState.searchResults!!,
                    onCardClick = { selectedItem = it }
                )
            }
            // ── 기본 화면 (최근 검색어 + 소비임박) ──
            else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.recentSearches.isNotEmpty()) {
                        RecentSearchesSection(
                            searches = uiState.recentSearches,
                            onRemove = { viewModel.removeRecentSearch(it) },
                            onClearAll = { viewModel.clearRecentSearches() },
                            onSearchClick = {
                                viewModel.search(it)
                                keyboardController?.hide()
                            }
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    NearExpirySearchSection(
                        items = uiState.nearExpiryItems,
                        onCardClick = { selectedItem = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 검색 입력창
// ───────────────────────────────────────────
@Composable
fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(50.dp),
        placeholder = {
            Text(
                "품목명 또는 카테고리 검색",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = if (query.isNotEmpty()) FreshGreenDark else Color(0xFFAAAAAA)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "지우기", tint = Color.Gray)
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FreshGreenDark,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFF5F5F5)
        )
    )
}

// ───────────────────────────────────────────
// 자동완성 드롭다운
// ───────────────────────────────────────────
@Composable
fun SuggestionDropdown(
    suggestions: List<String>,
    onSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Column {
            suggestions.forEachIndexed { index, suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(suggestion, fontSize = 15.sp)
                }
                if (index < suggestions.lastIndex) {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 최근 검색어 섹션
// ───────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentSearchesSection(
    searches: List<String>,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
    onSearchClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("최근 검색어", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onClearAll, contentPadding = PaddingValues(0.dp)) {
            Text("전체 삭제", fontSize = 13.sp, color = FreshGreenDark)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        searches.forEach { keyword ->
            RecentSearchChip(
                keyword = keyword,
                onClick = { onSearchClick(keyword) },
                onRemove = { onRemove(keyword) }
            )
        }
    }
}

// ───────────────────────────────────────────
// 최근 검색어 칩
// ───────────────────────────────────────────
@Composable
fun RecentSearchChip(
    keyword: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(keyword, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "삭제",
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() },
                tint = Color.Gray
            )
        }
    }
}

// ───────────────────────────────────────────
// 소비임박 섹션 (검색 기본 화면)
// ───────────────────────────────────────────
@Composable
fun NearExpirySearchSection(items: List<FoodItem>, onCardClick: (FoodItem) -> Unit = {}) {
    Column {
        Text("소비 임박", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "빨리 먹어야 하는 식재료",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (items.isEmpty()) {
            Text("소비임박 식재료가 없어요 👍", fontSize = 14.sp, color = Color.Gray)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items) { item ->
                    NearExpirySearchCard(item = item, onClick = { onCardClick(item) })
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 소비임박 카드
// ───────────────────────────────────────────
@Composable
fun NearExpirySearchCard(item: FoodItem, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(Color(0xFFFFF7ED))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔥", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(item.emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            item.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            item.expiryDate,
            fontSize = 11.sp,
            color = WarnOrange
        )
    }
}

// ───────────────────────────────────────────
// 검색 결과 섹션
// ───────────────────────────────────────────
@Composable
fun SearchResultsSection(
    query: String,
    results: List<FoodItem>,
    onCardClick: (FoodItem) -> Unit = {}
) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (results.isEmpty()) "\"$query\" 검색 결과가 없어요"
            else "\"$query\" 검색 결과 ${results.size}개",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("일치하는 식재료가 없어요", fontSize = 15.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results) { item ->
                    SearchResultCard(item = item, onClick = { onCardClick(item) })
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 검색 결과 카드
// ───────────────────────────────────────────
@Composable
fun SearchResultCard(item: FoodItem, onClick: () -> Unit = {}) {
    val statusColor = when (item.status) {
        FoodStatus.FRESH -> FreshGreenDark
        FoodStatus.NEAR_EXPIRY -> WarnOrange
        FoodStatus.EXPIRED -> WarnRed
    }
    val statusLabel = when (item.status) {
        FoodStatus.FRESH -> "신선"
        FoodStatus.NEAR_EXPIRY -> "소비임박"
        FoodStatus.EXPIRED -> "기한경과"
    }
    val storageLabel = when (item.storage) {
        com.freshkitchen.app.viewmodel.StorageType.FRIDGE -> "냉장"
        com.freshkitchen.app.viewmodel.StorageType.FREEZER -> "냉동"
        com.freshkitchen.app.viewmodel.StorageType.PANTRY -> "팬트리"
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이모지
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 식재료 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${item.category} · $storageLabel",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "유통기한 ${item.expiryDate}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // 상태 뱃지
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen()
}