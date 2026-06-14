package com.freshkitchen.app.ui.analysis.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.freshkitchen.app.navigation.BottomNavRoute
import com.freshkitchen.app.navigation.ScanNav
import com.freshkitchen.app.ui.analysis.AnalysisColors
import com.freshkitchen.app.ui.analysis.component.AnalysisBackTopBar
import com.freshkitchen.app.ui.analysis.component.AnalysisCenterLoadingIndicator
import com.freshkitchen.app.ui.analysis.component.AnalysisErrorCard
import com.freshkitchen.app.ui.analysis.component.ConsumptionEmptyCard
import com.freshkitchen.app.ui.analysis.component.ConsumptionItemCard
import com.freshkitchen.app.ui.analysis.component.ConsumptionStorageFilterRow
import com.freshkitchen.app.ui.analysis.component.ConsumptionSummaryCard
import com.freshkitchen.app.ui.theme.BottomNavSelected
import com.freshkitchen.app.viewmodel.ConsumptionStorageFilter
import com.freshkitchen.app.viewmodel.ConsumptionViewModel

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ConsumptionDetailScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ConsumptionViewModel = viewModel(),
) {
    val accent = BottomNavSelected
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedFilter by rememberSaveable { mutableStateOf(ConsumptionStorageFilter.All) }

    val filteredItems = if (selectedFilter == ConsumptionStorageFilter.All) {
        uiState.items
    } else {
        uiState.items.filter { it.storageType.equals(selectedFilter.apiValue, ignoreCase = true) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        val message = uiState.error
        if (!message.isNullOrBlank() && uiState.items.isNotEmpty()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnalysisBackTopBar(
                title = "소비 권장 알림",
                onBackClick = navController::popBackStack,
                containerColor = AnalysisColors.TopBarBackground,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "summary") {
                    ConsumptionSummaryCard(
                        count = uiState.items.size,
                        accent = accent,
                    )
                }

                item(key = "filters") {
                    ConsumptionStorageFilterRow(
                        selected = selectedFilter,
                        onSelect = { selectedFilter = it },
                    )
                }

                when {
                    uiState.error != null && uiState.items.isEmpty() -> {
                        item(key = "error") {
                            AnalysisErrorCard(
                                message = uiState.error.orEmpty(),
                                onRetry = viewModel::loadExpiringItems,
                            )
                        }
                    }
                    !uiState.isLoading && filteredItems.isEmpty() -> {
                        item(key = "empty") {
                            ConsumptionEmptyCard(filter = selectedFilter)
                        }
                    }
                    else -> {
                        items(
                            items = filteredItems,
                            key = { it.id },
                        ) { item ->
                            ConsumptionItemCard(
                                modifier = Modifier.animateItemPlacement(),
                                item = item,
                                accent = accent,
                                isConsuming = item.id in uiState.consumingIds,
                                onConsumeComplete = {
                                    viewModel.consumeItem(item.id) {
                                        navController.getBackStackEntry(BottomNavRoute.Home.route)
                                            .savedStateHandle
                                            .set(ScanNav.keyRefreshHome, System.currentTimeMillis())
                                    }
                                },
                            )
                        }
                    }
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
