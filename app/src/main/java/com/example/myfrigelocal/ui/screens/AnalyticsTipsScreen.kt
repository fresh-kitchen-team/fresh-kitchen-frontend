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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.viewmodel.AnalyticsViewModel
import com.example.myfrigelocal.viewmodel.CategoryRateUi
import com.example.myfrigelocal.viewmodel.ExpiringChipUi
import com.example.myfrigelocal.viewmodel.formatDdayLabel

@Composable
fun AnalyticsTipsScreen(
    navController: NavHostController,
    viewModel: AnalyticsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AnalyticsTipsContent(
        categoryRates = uiState.categoryRates,
        disposalRatePercent = uiState.disposalRatePercent,
        expiringChips = uiState.expiringChips,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRetry = { viewModel.loadAnalytics() },
        onConsumptionCardClick = { navController.navigate("consumption_detail") },
        onStorageTipCardClick = { navController.navigate("storage_tip_detail") },
        onDisposalGuideCardClick = { navController.navigate("disposal_guide") },
    )
}

@Composable
private fun AnalyticsTipsContent(
    modifier: Modifier = Modifier,
    categoryRates: List<CategoryRateUi>,
    disposalRatePercent: Int?,
    expiringChips: List<ExpiringChipUi>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onConsumptionCardClick: () -> Unit,
    onStorageTipCardClick: () -> Unit,
    onDisposalGuideCardClick: () -> Unit,
) {
    val background = Color(0xFFF6F8F7)
    val accent = BottomNavSelected

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
                    disposalRatePercent = disposalRatePercent,
                    isLoading = isLoading,
                    error = error,
                    onRetry = onRetry,
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
                    body = if (expiringChips.isEmpty()) {
                        "유통기한이 7일 이내로 남은 식재료가 없습니다."
                    } else {
                        "유통기한이 7일 이내로 남은 식재료가 있습니다."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onConsumptionCardClick),
                ) {
                    if (expiringChips.isNotEmpty()) {
                        ChipsRow(
                            chips = expiringChips.map { it.toChipLabel() },
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
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

@Composable
private fun DisposalRateCard(
    title: String,
    rates: List<CategoryRateUi>,
    disposalRatePercent: Int?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
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

        when {
            isLoading && rates.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = accent,
                    )
                }
            }
            error != null && rates.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7680),
                    )
                    Box(
                        modifier = Modifier
                            .background(accent, RoundedCornerShape(999.dp))
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
            else -> {
                MiniBarChart(
                    rates = rates,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        InfoBanner(
            accent = accent,
            disposalRatePercent = disposalRatePercent,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MiniBarChart(
    rates: List<CategoryRateUi>,
    modifier: Modifier = Modifier,
) {
    val maxPercent = remember(rates) { (rates.maxOfOrNull { it.percent } ?: 1).coerceAtLeast(1) }
    val topCategory = remember(rates) { rates.maxByOrNull { it.percent }?.category }
    val maxBarHeight = 72.dp
    val minBarHeight = 6.dp

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
                val computedHeight = (maxBarHeight.value * heightFraction).dp
                val barHeight = if (rate.percent <= 0) minBarHeight else computedHeight
                val isTop = rate.category == topCategory && rate.percent > 0
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .fillMaxWidth()
                        .background(
                            color = if (isTop) Color(0xFF32E0A1) else Color(0xFF7BECC3),
                            shape = RoundedCornerShape(12.dp),
                        ),
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
    disposalRatePercent: Int?,
    modifier: Modifier = Modifier,
) {
    val bannerBg = Color(0xFFEAF7F2)
    val text = buildAnnotatedString {
        if (disposalRatePercent != null) {
            append("전체 폐기율은 ")
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
                append("${disposalRatePercent}%")
            }
            append("입니다.\nconsume 가 아닌 delete 처리된 식재료만 집계돼요.")
        } else {
            append("아직 분석할 식재료가 충분하지 않아요.\n식재료를 추가하면 폐기율이 표시됩니다.")
        }
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

private fun ExpiringChipUi.toChipLabel(): String = "$name (${formatDdayLabel(dday)})"

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
