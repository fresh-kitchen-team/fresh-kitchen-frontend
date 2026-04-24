package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myfrigelocal.R

@Composable
fun StorageTipDetailScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val background = Color(0xFFF6F8F7)

    val categories = remember {
        listOf(
            StorageTipCategory(
                id = "veg_fruit",
                title = "채소 및 과일",
                headerIconRes = R.drawable.ic_tip_veg,
                headerIconBg = Color(0xFFEAF7F2),
                tip = StorageTip(
                    title = "에틸렌 가스 분리",
                    description = "사과, 복숭아 등 에틸렌 가스를 내뿜는 과일은\n다른 채소와 따로 보관해야 빨리 시드는 것을 막을 수 있습니다.",
                    iconRes = R.drawable.ic_tip_veg,
                    iconBg = Color(0xFFEAF7F2),
                ),
            ),
            StorageTipCategory(
                id = "meat",
                title = "육류",
                headerIconRes = R.drawable.ic_tip_meat,
                headerIconBg = Color(0xFFFFE9EA),
                tip = StorageTip(
                    title = "표면 산화 방지",
                    description = "고기 표면에 식용유를 살짝 바르면 공기와의 접촉을 막아 신선도를 더 오래 유지할 수 있습니다.",
                    iconRes = R.drawable.ic_tip_meat,
                    iconBg = Color(0xFFFFE9EA),
                ),
            ),
            StorageTipCategory(
                id = "seafood",
                title = "수산물",
                headerIconRes = R.drawable.ic_tip_fish,
                headerIconBg = Color(0xFFEAF2FF),
                tip = StorageTip(
                    title = "내장 제거",
                    description = "생선은 내장부터 부패가 시작되므로 반드시 내장을 제거하고 깨끗이 씻어 보관하세요.",
                    iconRes = R.drawable.ic_tip_fish,
                    iconBg = Color(0xFFEAF2FF),
                ),
            ),
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StorageTipTopBar(
                title = "보관 팁",
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Text(
                    text = "올바른 보관 팁",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101418),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                )
            }

            items(categories, key = { it.id }) { category ->
                CategorySection(
                    category = category,
                )
            }
        }
    }
}

@Composable
private fun StorageTipTopBar(
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

@Immutable
private data class StorageTipCategory(
    val id: String,
    val title: String,
    val headerIconRes: Int,
    val headerIconBg: Color,
    val tip: StorageTip,
)

@Immutable
private data class StorageTip(
    val title: String,
    val description: String,
    val iconRes: Int,
    val iconBg: Color,
)

@Composable
private fun CategorySection(
    category: StorageTipCategory,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CategoryHeaderRow(
            iconRes = category.headerIconRes,
            iconBg = category.headerIconBg,
            title = category.title,
        )

        StorageTipCard(
            tip = category.tip,
        )
    }
}

@Composable
private fun CategoryHeaderRow(
    iconRes: Int,
    iconBg: Color,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF101418),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        MoreChip()
    }
}

@Composable
private fun MoreChip(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8ECEF))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "더보기",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7680),
        )
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF6B7680),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun StorageTipCard(
    tip: StorageTip,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tip.iconBg)
                    .border(1.dp, Color(0xFFE5EAEE), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(tip.iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101418),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7680),
                )
            }
        }
    }
}

