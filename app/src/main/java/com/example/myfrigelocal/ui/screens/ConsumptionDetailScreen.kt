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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myfrigelocal.ui.theme.BottomNavSelected

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ConsumptionDetailScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val background = Color(0xFFF6F8F7)
    val accent = BottomNavSelected

    var selectedFilter by rememberSaveable { mutableStateOf(StorageFilter.All) }

    val items = remember {
        mutableStateListOf(
            ConsumptionItem(
                id = "milk",
                name = "신선한 우유",
                storage = "냉장실",
                expiry = "2026-03-25",
                ddayLabel = "D-1",
                ddayTone = DdayTone.Critical,
                filter = StorageFilter.Fridge,
            ),
            ConsumptionItem(
                id = "chicken",
                name = "닭가슴살",
                storage = "냉장실",
                expiry = "2026-03-25",
                ddayLabel = "D-1",
                ddayTone = DdayTone.Critical,
                filter = StorageFilter.Fridge,
            ),
            ConsumptionItem(
                id = "tomato",
                name = "방울토마토",
                storage = "냉장실",
                expiry = "2026-03-27",
                ddayLabel = "D-3",
                ddayTone = DdayTone.Warning,
                filter = StorageFilter.Fridge,
            ),
            ConsumptionItem(
                id = "frozen-veg",
                name = "냉동 브로콜리",
                storage = "냉동실",
                expiry = "2026-03-26",
                ddayLabel = "D-2",
                ddayTone = DdayTone.Warning,
                filter = StorageFilter.Freezer,
            ),
            ConsumptionItem(
                id = "pasta",
                name = "스파게티면",
                storage = "팬트리",
                expiry = "2026-03-26",
                ddayLabel = "D-2",
                ddayTone = DdayTone.Warning,
                filter = StorageFilter.Pantry,
            ),
        )
    }

    val filteredItems by remember(selectedFilter) {
        derivedStateOf {
            // SnapshotStateList mutation won't change the list reference,
            // so we must read it inside derivedStateOf to trigger updates.
            val snapshot = items.toList()
            snapshot.filter { selectedFilter == StorageFilter.All || it.filter == selectedFilter }
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
        containerColor = background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SummaryCard(
                    count = items.size,
                    accent = accent,
                )
            }

            item {
                FilterRow(
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it },
                )
            }

            items(
                items = filteredItems,
                key = { it.id },
            ) { item ->
                ConsumptionItemCard(
                    modifier = Modifier.animateItemPlacement(),
                    item = item,
                    accent = accent,
                    onConsumeComplete = { items.remove(item) },
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
                text = "총 ${count}개의 품목의 유통기한이\n3일 이내입니다.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101418),
            )
        }
    }
}

private enum class StorageFilter(val label: String) {
    All("전체"),
    Fridge("냉장"),
    Freezer("냉동"),
    Pantry("팬트리"),
}

@Composable
private fun FilterRow(
    selected: StorageFilter,
    onSelect: (StorageFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(StorageFilter.entries, key = { it.name }) { filter ->
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

private enum class DdayTone { Critical, Warning }

@Immutable
private data class ConsumptionItem(
    val id: String,
    val name: String,
    val storage: String,
    val expiry: String,
    val ddayLabel: String,
    val ddayTone: DdayTone,
    val filter: StorageFilter,
)

@Composable
private fun ConsumptionItemCard(
    item: ConsumptionItem,
    accent: Color,
    onConsumeComplete: () -> Unit,
    modifier: Modifier = Modifier,
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
                ItemImagePlaceholder()
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
                        text = item.storage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7680),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "유통기한: ${item.expiry}",
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
                text = "소비 완료",
                accent = accent,
                onClick = onConsumeComplete,
            )
        }
    }
}

@Composable
private fun ItemImagePlaceholder(
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
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = Color(0xFF9AA5AE),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun DdayBadge(
    text: String,
    tone: DdayTone,
    modifier: Modifier = Modifier,
) {
    val fg = when (tone) {
        DdayTone.Critical -> Color(0xFFFF3B30)
        DdayTone.Warning -> Color(0xFFFF6A00)
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
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

// Reusable card helper kept local to this screen for now (no backend / no shared design system yet).
@Composable
private fun MyFridgeCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            content = content,
        )
    }
}

