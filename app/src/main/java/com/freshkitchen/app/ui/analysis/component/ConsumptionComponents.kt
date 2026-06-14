package com.freshkitchen.app.ui.analysis.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshkitchen.app.ui.analysis.AnalysisColors
import com.freshkitchen.app.viewmodel.ConsumptionDdayTone
import com.freshkitchen.app.viewmodel.ConsumptionItemUi
import com.freshkitchen.app.viewmodel.ConsumptionStorageFilter

@Composable
internal fun ConsumptionSummaryCard(
    count: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalysisColors.SummaryBackground),
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
                text = if (count > 0) {
                    "총 ${count}개의 품목의 유통기한이\n7일 이내입니다."
                } else {
                    "유통기한이 7일 이내로 남은\n식재료가 없습니다."
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = AnalysisColors.TextPrimary,
            )
        }
    }
}

@Composable
internal fun ConsumptionStorageFilterRow(
    selected: ConsumptionStorageFilter,
    onSelect: (ConsumptionStorageFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(ConsumptionStorageFilter.entries, key = { it.name }) { filter ->
            ConsumptionFilterChip(
                text = filter.label,
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun ConsumptionFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) {
        AnalysisColors.FilterSelectedBackground
    } else {
        AnalysisColors.FilterUnselectedBackground
    }
    val foreground = if (selected) Color.White else AnalysisColors.FilterUnselectedText

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
    }
}

@Composable
internal fun ConsumptionItemCard(
    item: ConsumptionItemUi,
    accent: Color,
    onConsumeComplete: () -> Unit,
    modifier: Modifier = Modifier,
    isConsuming: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalysisColors.CardBackground),
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
                ItemEmojiBox(emoji = item.emoji)
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AnalysisColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.storageLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnalysisColors.TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (!item.expiryDate.isNullOrBlank()) {
                            "유통기한: ${item.expiryDate}"
                        } else {
                            "유통기한: -"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AnalysisColors.TextSecondary,
                    )
                }

                ConsumptionDdayBadge(
                    text = item.ddayLabel,
                    tone = item.ddayTone,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ConsumptionCompleteButton(
                text = if (isConsuming) "처리 중..." else "소비 완료",
                accent = accent,
                isLoading = isConsuming,
                onClick = onConsumeComplete,
            )
        }
    }
}

@Composable
private fun ItemEmojiBox(
    emoji: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AnalysisColors.EmojiBoxBackground)
            .border(1.dp, AnalysisColors.EmojiBoxBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (!emoji.isNullOrBlank()) {
            Text(
                text = emoji,
                fontSize = 26.sp,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = Color(0xFF9AA5AE),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ConsumptionDdayBadge(
    text: String,
    tone: ConsumptionDdayTone,
    modifier: Modifier = Modifier,
) {
    val foreground = when (tone) {
        ConsumptionDdayTone.Critical -> Color(0xFFFF3B30)
        ConsumptionDdayTone.Warning -> Color(0xFFFF6A00)
        ConsumptionDdayTone.Info -> Color(0xFF22A87E)
    }
    val background = foreground.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
    }
}

@Composable
private fun ConsumptionCompleteButton(
    text: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val backgroundColor = if (isLoading) accent.copy(alpha = 0.6f) else accent
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

@Composable
internal fun ConsumptionEmptyCard(
    filter: ConsumptionStorageFilter,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalysisColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = if (filter == ConsumptionStorageFilter.All) {
                "유통기한이 7일 이내로 남은 식재료가 없습니다."
            } else {
                "${filter.label}에 임박한 식재료가 없습니다."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9AA4AE),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun AnalysisCenterLoadingIndicator(
    accent: Color,
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(
        modifier = modifier.size(36.dp),
        color = accent,
    )
}
