package com.freshkitchen.app.ui.analysis.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.freshkitchen.app.ui.analysis.AnalysisColors

@Composable
internal fun ManagementTipCard(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    footer: @Composable (() -> Unit)? = null,
) {
    AnalysisCard(modifier = modifier) {
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
                    color = AnalysisColors.TextPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnalysisColors.TextSecondary,
                )
                footer?.invoke()
            }
        }
    }
}

@Composable
internal fun ExpiringChipRow(
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
        chips.forEach { label ->
            ExpiringChip(text = label)
        }
    }
}

@Composable
private fun ExpiringChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(AnalysisColors.ChipBackground, RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.bodySmall,
        color = AnalysisColors.TextChip,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun TipLinkRow(
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
internal fun DisposalInfoBanner(
    accent: Color,
    disposalRatePercent: Int?,
    urgentItemCount: Int,
    modifier: Modifier = Modifier,
) {
    val bannerText = buildAnnotatedString {
        if (disposalRatePercent != null) {
            append("전체 폐기율은 ")
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
                append("${disposalRatePercent}%")
            }
            append("입니다.\n유통기한이 3일 이내인 식재료는 ")
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
                append("${urgentItemCount}개")
            }
            append(" 있습니다.")
        } else {
            append("아직 분석할 식재료가 충분하지 않아요.\n식재료를 추가하면 폐기율이 표시됩니다.")
        }
    }

    Row(
        modifier = modifier
            .background(AnalysisColors.BannerBackground, RoundedCornerShape(14.dp))
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
            text = bannerText,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2A343B),
        )
    }
}
