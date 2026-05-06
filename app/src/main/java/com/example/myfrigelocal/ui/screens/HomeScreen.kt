package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfrigelocal.viewmodel.HomeViewModel

val FreshGreen = Color(0xFF4ADE80)
val FreshGreenDark = Color(0xFF22C55E)
val WarnOrange = Color(0xFFF97316)
val WarnRed = Color(0xFFEF4444)
val LightGray = Color(0xFFF5F5F5)

// ───────────────────────────────────────────
// 홈 스크린
// ───────────────────────────────────────────
@Composable
fun HomeScreen(
    onNavigateToInventory: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { HomeTopBar(onProfileClick = onNavigateToProfile) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 전체 품목 카드 → 전체 리스트로 이동
            TotalItemCard(
                totalCount = uiState.totalItemCount,
                recentAdded = uiState.recentAddedCount,
                onClick = { onNavigateToInventory("all") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 소비임박 / 유통기한 경과
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlertCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning,
                    iconColor = WarnOrange,
                    label = "소비임박",
                    count = uiState.nearExpiryCount,
                    onClick = { onNavigateToInventory("near_expiry") }
                )
                AlertCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Error,
                    iconColor = WarnRed,
                    label = "유통기한 경과",
                    count = uiState.expiredCount,
                    onClick = { onNavigateToInventory("expired") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 저장 공간별 관리
            SectionTitle(title = "저장 공간별 관리")
            Spacer(modifier = Modifier.height(12.dp))

            uiState.storageList.forEach { storage ->
                StorageItem(
                    emoji = storage.emoji,
                    name = storage.name,
                    itemCount = storage.itemCount,
                    onClick = { onNavigateToInventory(storage.filterKey) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 최근 추가된 품목
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(title = "최근 추가된 품목")
                TextButton(onClick = { onNavigateToInventory("all") }) {
                    Text("전체보기", color = FreshGreenDark, fontSize = 13.sp)
                }
            }

            RecentItemsRow(items = uiState.recentItems)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ───────────────────────────────────────────
// 상단 앱바 (프로필 / 검색 / 설정)
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onProfileClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FreshGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍳", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "주방 인벤토리",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.Person, contentDescription = "프로필")
            }
            IconButton(onClick = { /* TODO: 검색 이동 */ }) {
                Icon(Icons.Default.Search, contentDescription = "검색")
            }
            IconButton(onClick = { /* TODO: 설정 이동 */ }) {
                Icon(Icons.Default.Settings, contentDescription = "설정")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// ───────────────────────────────────────────
// 전체 품목 카드 (그린 그라데이션)
// ───────────────────────────────────────────
@Composable
fun TotalItemCard(totalCount: Int, recentAdded: Int, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(
                Brush.horizontalGradient(
                    colors = listOf(FreshGreen, FreshGreenDark)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text("전체 품목", color = Color.White, fontSize = 13.sp)
            Text(
                text = "$totalCount",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("+$recentAdded", color = Color.White, fontSize = 13.sp)
        }
    }
}

// ───────────────────────────────────────────
// 소비임박 / 유통기한 경과 카드
// ───────────────────────────────────────────
@Composable
fun AlertCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    count: Int,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "$count",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ───────────────────────────────────────────
// 섹션 타이틀
// ───────────────────────────────────────────
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
}

// ───────────────────────────────────────────
// 저장 공간 아이템 (냉동/냉장/팬트리)
// ───────────────────────────────────────────
@Composable
fun StorageItem(
    emoji: String,
    name: String,
    itemCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text("${itemCount}개의 아이템", fontSize = 12.sp, color = Color.Gray)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

// ───────────────────────────────────────────
// 최근 추가된 품목 가로 스크롤
// ───────────────────────────────────────────
@Composable
fun RecentItemsRow(items: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items.size) { index ->
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(items[index], fontSize = 32.sp)
            }
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}