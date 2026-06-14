package com.freshkitchen.app.ui.analysis.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.freshkitchen.app.ui.analysis.AnalysisColors
import com.freshkitchen.app.ui.analysis.component.AnalysisInfoTopBar
import com.freshkitchen.app.ui.analysis.component.AnalysisSectionHeader
import com.freshkitchen.app.ui.analysis.component.DisposalRateCard
import com.freshkitchen.app.ui.analysis.component.ExpiringChipRow
import com.freshkitchen.app.ui.analysis.component.ManagementTipCard
import com.freshkitchen.app.ui.analysis.component.TipLinkRow
import com.freshkitchen.app.ui.analysis.util.toChipLabel
import com.freshkitchen.app.ui.theme.BottomNavSelected
import com.freshkitchen.app.ui.theme.MyFrigeLocalTheme
import com.freshkitchen.app.viewmodel.AnalyticsViewModel
import com.freshkitchen.app.viewmodel.CategoryRateUi
import com.freshkitchen.app.viewmodel.ExpiringChipUi
import com.freshkitchen.app.viewmodel.StorageTipCategoryType

@Composable
fun AnalyticsTipsScreen(
    navController: NavHostController,
    viewModel: AnalyticsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsTipsContent(
        categoryRates = uiState.categoryRates,
        disposalRatePercent = uiState.disposalRatePercent,
        urgentItemCount = uiState.urgentItemCount,
        expiringChips = uiState.expiringChips,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRetry = viewModel::loadAnalytics,
        onConsumptionCardClick = { navController.navigate("consumption_detail") },
        onStorageTipCardClick = { navController.navigate("storage_tip_detail") },
        onDisposalGuideCardClick = { navController.navigate("disposal_guide") },
    )
}

@Composable
private fun AnalyticsTipsContent(
    categoryRates: List<CategoryRateUi>,
    disposalRatePercent: Int?,
    urgentItemCount: Int,
    expiringChips: List<ExpiringChipUi>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onConsumptionCardClick: () -> Unit,
    onStorageTipCardClick: () -> Unit,
    onDisposalGuideCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = BottomNavSelected

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnalysisInfoTopBar(
                title = "소비 분석 & 팁",
                accent = accent,
                containerColor = AnalysisColors.TopBarBackground,
            )
        },
        containerColor = AnalysisColors.ScreenBackground,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "disposal_rate_card") {
                DisposalRateCard(
                    title = "카테고리 별 폐기율",
                    rates = categoryRates,
                    disposalRatePercent = disposalRatePercent,
                    urgentItemCount = urgentItemCount,
                    isLoading = isLoading,
                    error = error,
                    onRetry = onRetry,
                    accent = accent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item(key = "management_tips_header") {
                AnalysisSectionHeader(title = "냉장고 관리 팁")
            }

            item(key = "consumption_tip_card") {
                ManagementTipCard(
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
                    footer = {
                        if (expiringChips.isNotEmpty()) {
                            ExpiringChipRow(
                                chips = expiringChips.map { it.toChipLabel() },
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }
                    },
                )
            }

            item(key = "storage_tip_card") {
                ManagementTipCard(
                    icon = Icons.Outlined.AcUnit,
                    iconBackground = Color(0xFFEAF2FF),
                    iconTint = Color(0xFF2F6BFF),
                    title = "올바른 보관 팁",
                    body = "오래 보관해야 하는 식재료는 냉동실로 옮겨보세요.\n신선도를 더 오래 유지할 수 있습니다.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onStorageTipCardClick),
                    footer = {
                        TipLinkRow(
                            text = "냉동 보관 가이드 보기",
                            accent = accent,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    },
                )
            }

            item(key = "disposal_guide_card") {
                ManagementTipCard(
                    icon = Icons.Outlined.Delete,
                    iconBackground = Color(0xFFFFE9EA),
                    iconTint = Color(0xFFFF3B30),
                    title = "폐기 가이드",
                    body = "만료된 식재료는 음식물 쓰레기 분류 기준에 맞춰 배출해주세요.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDisposalGuideCardClick),
                    footer = {
                        Text(
                            text = "• 채소, 껍질, 씨앗 등은 일반 쓰레기입니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8A939B),
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsTipsContentPreview() {
    MyFrigeLocalTheme {
        AnalyticsTipsContent(
            categoryRates = StorageTipCategoryType.entries.map { type ->
                CategoryRateUi(
                    category = type,
                    label = type.displayName,
                    itemCount = 5,
                    percent = 25,
                )
            },
            disposalRatePercent = 12,
            urgentItemCount = 3,
            expiringChips = listOf(
                ExpiringChipUi(id = 1, name = "우유", dday = 2),
                ExpiringChipUi(id = 2, name = "양파", dday = 5),
            ),
            isLoading = false,
            error = null,
            onRetry = {},
            onConsumptionCardClick = {},
            onStorageTipCardClick = {},
            onDisposalGuideCardClick = {},
        )
    }
}
