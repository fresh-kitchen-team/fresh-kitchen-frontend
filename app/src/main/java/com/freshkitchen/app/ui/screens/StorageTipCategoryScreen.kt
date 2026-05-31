package com.freshkitchen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.freshkitchen.app.viewmodel.StorageTipCategoryType
import com.freshkitchen.app.viewmodel.StorageTipUi
import com.freshkitchen.app.viewmodel.StorageTipsViewModel

@Composable
fun StorageTipCategoryScreen(
    navController: NavHostController,
    type: String,
    modifier: Modifier = Modifier,
    viewModel: StorageTipsViewModel = viewModel(),
) {
    val background = Color(0xFFF6F8F7)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val categoryType = StorageTipCategoryType.fromApi(type)
    val tips = if (categoryType != null) {
        uiState.tipsByCategory[categoryType].orEmpty()
    } else {
        emptyList()
    }
    val iconBg = categoryType?.iconBgColor() ?: Color(0xFFF1F3F5)
    val title = categoryType?.displayName ?: "보관 팁"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CategoryTopBar(
                title = title,
                onBackClick = { navController.popBackStack() },
                containerColor = Color(0xFFF1F6F6),
            )
        },
        containerColor = background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading && tips.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp),
                        color = Color(0xFF32E0A1),
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
                            color = Color(0xFF6B7680),
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF32E0A1))
                                .clickable { viewModel.loadStorageTips() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                tips.isEmpty() -> {
                    Text(
                        text = "표시할 팁이 아직 없어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9AA4AE),
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
                        items(tips, key = { it.id }) { tip ->
                            TipCard(
                                tip = tip,
                                iconBg = iconBg,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun StorageTipCategoryType.iconBgColor(): Color = when (this) {
    StorageTipCategoryType.VEGETABLE_FRUIT -> Color(0xFFEAF7F2)
    StorageTipCategoryType.DAIRY_DRINK -> Color(0xFFFFF8E1)
    StorageTipCategoryType.MEAT_SEAFOOD -> Color(0xFFFFE9EA)
    StorageTipCategoryType.ETC -> Color(0xFFEAF2FF)
}

@Composable
private fun CategoryTopBar(
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
private fun TipCard(
    tip: StorageTipUi,
    iconBg: Color,
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
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tip.emoji.ifBlank { "💡" },
                    fontSize = 22.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101418),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tip.tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7680),
                )
            }
        }
    }
}
