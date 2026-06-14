package com.freshkitchen.app.ui.analysis.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freshkitchen.app.ui.analysis.AnalysisColors
import com.freshkitchen.app.viewmodel.CategoryRateUi

@Composable
internal fun DisposalRateCard(
    title: String,
    rates: List<CategoryRateUi>,
    disposalRatePercent: Int?,
    urgentItemCount: Int,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    AnalysisCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AnalysisColors.TextPrimary,
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
                        color = AnalysisColors.TextSecondary,
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
                CategoryDiscardRateBarChart(
                    rates = rates,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DisposalInfoBanner(
            accent = accent,
            disposalRatePercent = disposalRatePercent,
            urgentItemCount = urgentItemCount,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun CategoryDiscardRateBarChart(
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
                            color = if (isTop) AnalysisColors.BarPrimary else AnalysisColors.BarSecondary,
                            shape = RoundedCornerShape(12.dp),
                        ),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = rate.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnalysisColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${rate.percent}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AnalysisColors.TextPrimary,
                )
            }
        }
    }
}
