package com.freshkitchen.app.ui.analysis.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freshkitchen.app.ui.analysis.AnalysisColors
import com.freshkitchen.app.ui.analysis.component.AnalysisBackTopBar
import com.freshkitchen.app.ui.analysis.component.AnalysisCenterLoadingIndicator
import com.freshkitchen.app.ui.analysis.component.AnalysisRetryButton
import com.freshkitchen.app.ui.analysis.component.StorageTipCard
import com.freshkitchen.app.ui.analysis.util.iconBackgroundColor
import com.freshkitchen.app.viewmodel.StorageTipCategoryType
import com.freshkitchen.app.viewmodel.StorageTipsViewModel

@Composable
fun StorageTipCategoryScreen(
    navController: NavHostController,
    type: String,
    modifier: Modifier = Modifier,
    viewModel: StorageTipsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val categoryType = StorageTipCategoryType.fromApi(type)
    val tips = if (categoryType != null) {
        uiState.tipsByCategory[categoryType].orEmpty()
    } else {
        emptyList()
    }
    val iconBackground = categoryType?.iconBackgroundColor() ?: AnalysisColors.EmojiBoxBackground
    val title = categoryType?.displayName ?: "보관 팁"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnalysisBackTopBar(
                title = title,
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
            when {
                uiState.isLoading && tips.isEmpty() -> {
                    AnalysisCenterLoadingIndicator(
                        accent = AnalysisColors.StorageAccent,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                uiState.error != null && tips.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = uiState.error.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AnalysisColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                        AnalysisRetryButton(onClick = viewModel::loadStorageTips)
                    }
                }
                tips.isEmpty() -> {
                    Text(
                        text = "표시할 팁이 아직 없어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnalysisColors.TextMuted,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 24.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(
                            items = tips,
                            key = { it.id },
                        ) { tip ->
                            StorageTipCard(
                                tip = tip,
                                iconBackground = iconBackground,
                            )
                        }
                    }
                }
            }
        }
    }
}
