package com.freshkitchen.app.ui.analysis.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.freshkitchen.app.ui.analysis.component.AnalysisEmptyCard
import com.freshkitchen.app.ui.analysis.component.AnalysisErrorCard
import com.freshkitchen.app.ui.analysis.component.DisposalGuideHeaderCard
import com.freshkitchen.app.ui.analysis.component.DisposalGuideItemCard
import com.freshkitchen.app.ui.theme.BottomNavSelected
import com.freshkitchen.app.viewmodel.DisposalGuideViewModel

@Composable
fun DisposalGuideScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: DisposalGuideViewModel = hiltViewModel(),
) {
    val accent = BottomNavSelected
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnalysisBackTopBar(
                title = "분리배출 가이드",
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "guide_header") {
                    DisposalGuideHeaderCard(
                        accent = accent,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item(key = "section_title") {
                    Text(
                        text = "자주 헷갈리는 품목",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AnalysisColors.TextPrimary,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                    )
                }

                if (uiState.error != null && uiState.items.isEmpty()) {
                    item(key = "error") {
                        AnalysisErrorCard(
                            message = uiState.error.orEmpty(),
                            onRetry = viewModel::loadRecyclingTips,
                        )
                    }
                } else if (!uiState.isLoading && uiState.items.isEmpty()) {
                    item(key = "empty") {
                        AnalysisEmptyCard(message = "표시할 품목이 아직 없어요.")
                    }
                }

                items(
                    items = uiState.items,
                    key = { it.id },
                ) { item ->
                    DisposalGuideItemCard(item = item)
                }
            }

            if (uiState.isLoading && uiState.items.isEmpty()) {
                AnalysisCenterLoadingIndicator(
                    accent = accent,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}
