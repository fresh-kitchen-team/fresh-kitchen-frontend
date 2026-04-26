package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myfrigelocal.ui.theme.BottomNavSelected

@Composable
fun AnalyticsTipsScreen(
    navController: NavHostController,
) {
    AnalyticsTipsContent(
        onConsumptionCardClick = { navController.navigate("consumption_detail") },
        onStorageTipCardClick = { navController.navigate("storage_tip_detail") },
        onDisposalGuideCardClick = { navController.navigate("disposal_guide") },
    )
}

@Composable
private fun AnalyticsTipsContent(
    modifier: Modifier = Modifier,
    onConsumptionCardClick: () -> Unit,
    onStorageTipCardClick: () -> Unit,
    onDisposalGuideCardClick: () -> Unit,
) {
    val background = Color(0xFFF6F8F7)
    val accent = BottomNavSelected

    val categoryRates = remember {
        listOf(
            CategoryRate(label = "채소류", percent = 40, barColor = Color(0xFF32E0A1)),
            CategoryRate(label = "유제품", percent = 25, barColor = Color(0xFF7BECC3)),
            CategoryRate(label = "육류", percent = 15, barColor = Color(0xFF7BECC3)),
            CategoryRate(label = "기타", percent = 20, barColor = Color(0xFF7BECC3)),
        )
    }
    val expiringItems = remember {
        listOf(
            "닭가슴살 (D-5)",
            "우유 (D-3)",
            "시금치 (D-2)",
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnalyticsTipsTopBar(
                title = "소비 분석 & 팁",
                accent = accent,
                containerColor = Color(0xFFF1F6F6),
            )
        },
        containerColor = background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                DisposalRateCard(
                    title = "카테고리 별 폐기율",
                    rates = categoryRates,
                    accent = accent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                SectionHeader(title = "냉장고 관리 팁")
            }

            item {
                TipCard(
                    icon = Icons.Outlined.Notifications,
                    iconBackground = Color(0xFFFFEEE3),
                    iconTint = Color(0xFFFF7A2F),
                    title = "소비 권장 알림",
                    body = "유통기한이 7일 이내로 남은 식재료가 있습니다.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onConsumptionCardClick),
                ) {
                    ChipsRow(
                        chips = expiringItems,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            item {
                TipCard(
                    icon = Icons.Outlined.AcUnit,
                    iconBackground = Color(0xFFEAF2FF),
                    iconTint = Color(0xFF2F6BFF),
                    title = "올바른 보관 팁",
                    body = "오래 보관해야 하는 식재료는 냉동실로 옮겨보세요.\n신선도를 더 오래 유지할 수 있습니다.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onStorageTipCardClick),
                ) {
                    LinkRow(
                        text = "냉동 보관 가이드 보기",
                        accent = accent,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }

            item {
                TipCard(
                    icon = Icons.Outlined.Delete,
                    iconBackground = Color(0xFFFFE9EA),
                    iconTint = Color(0xFFFF3B30),
                    title = "폐기 가이드",
                    body = "만료된 식재료는 음식물 쓰레기 분류 기준에 맞춰 배출해주세요.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDisposalGuideCardClick),
                ) {
                    Text(
                        text = "• 채소, 껍질, 씨앗 등은 일반 쓰레기입니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A939B),
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsTipsTopBar(
    title: String,
    accent: Color,
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFE0ECE8), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF101418),
            )
        }
    }
}

@Immutable
private data class CategoryRate(
    val label: String,
    val percent: Int,
    val barColor: Color,
)

@Composable
private fun DisposalRateCard(
    title: String,
    rates: List<CategoryRate>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    MyFridgeCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF101418),
        )

        Spacer(modifier = Modifier.height(18.dp))

        MiniBarChart(
            rates = rates,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoBanner(
            accent = accent,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MiniBarChart(
    rates: List<CategoryRate>,
    modifier: Modifier = Modifier,
) {
    val maxPercent = remember(rates) { (rates.maxOfOrNull { it.percent } ?: 1).coerceAtLeast(1) }
    val maxBarHeight = 72.dp

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        rates.forEach { rate ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val heightFraction = rate.percent.toFloat() / maxPercent.toFloat()
                Box(
                    modifier = Modifier
                        .height((maxBarHeight.value * heightFraction).dp)
                        .fillMaxWidth()
                        .background(rate.barColor, RoundedCornerShape(12.dp)),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = rate.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7680),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${rate.percent}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101418),
                )
            }
        }
    }
}

@Composable
private fun InfoBanner(
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val bannerBg = Color(0xFFEAF7F2)
    val text = buildAnnotatedString {
        append("이번 달 평균 폐기율은 ")
        withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) { append("26%") }
        append("입니다.\n지난달보다 ")
        withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) { append("3%") }
        append(" 감소했습니다.")
    }

    Row(
        modifier = modifier
            .background(bannerBg, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD3EEE4), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2A343B),
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF101418),
        modifier = modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun TipCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    footer: @Composable (() -> Unit)? = null,
) {
    MyFridgeCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101418),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7680),
                )
                if (footer != null) footer()
            }
        }
    }
}

@Composable
private fun ChipsRow(
    chips: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowChips(
        chips = chips,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(
    chips: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { label -> Chip(text = label) }
    }
}

@Composable
private fun Chip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(Color(0xFFF1F3F5), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF5D6972),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun LinkRow(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
        Text(
            text = " 〉",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
    }
}

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
