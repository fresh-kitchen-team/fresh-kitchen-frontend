package com.freshkitchen.app.ui.analysis.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freshkitchen.app.ui.analysis.AnalysisColors
import com.freshkitchen.app.ui.analysis.component.AnalysisBackTopBar
import com.freshkitchen.app.ui.analysis.component.AnalysisCenterLoadingIndicator
import com.freshkitchen.app.ui.analysis.component.AnalysisErrorCard
import com.freshkitchen.app.ui.analysis.component.StorageTipCategorySection
import com.freshkitchen.app.ui.analysis.model.StorageTipCategorySectionUi
import com.freshkitchen.app.ui.analysis.util.headerBackgroundColor
import com.freshkitchen.app.ui.analysis.util.headerEmoji
import com.freshkitchen.app.viewmodel.StorageTipCategoryType
import com.freshkitchen.app.viewmodel.StorageTipsViewModel

@Composable
fun StorageTipDetailScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: StorageTipsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val categories = StorageTipCategoryType.entries.map { type ->
        StorageTipCategorySectionUi(
            type = type,
            title = type.displayName,
            headerEmoji = type.headerEmoji(),
            headerBackground = type.headerBackgroundColor(),
            previewTip = uiState.tipsByCategory[type]?.firstOrNull(),
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnalysisBackTopBar(
                title = "보관 팁",
                onBackClick = navController::popBackStack,
                containerColor = AnalysisColors.TopBarBackground,
            )
        },
        containerColor = AnalysisColors.ScreenBackground,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item(key = "page_title") {
                    Text(
                        text = "올바른 보관 팁",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AnalysisColors.TextPrimary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                    )
                }

                if (uiState.error != null && !uiState.isLoading) {
                    item(key = "error") {
                        AnalysisErrorCard(
                            message = uiState.error.orEmpty(),
                            onRetry = viewModel::loadStorageTips,
                        )
                    }
                }

                items(
                    items = categories,
                    key = { it.type.apiValue },
                ) { category ->
                    StorageTipCategorySection(
                        category = category,
                        onMoreClick = {
                            navController.navigate("storage_tip_category/${category.type.apiValue}")
                        },
                    )
                }
            }

            if (uiState.isLoading) {
                AnalysisCenterLoadingIndicator(
                    accent = AnalysisColors.StorageAccent,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}
