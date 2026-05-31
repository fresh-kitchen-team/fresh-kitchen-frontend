package com.freshkitchen.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatQuickReply(
    val label: String,
    val message: String,
)

val defaultChatQuickReplies: List<ChatQuickReply> = listOf(
    ChatQuickReply(
        label = "레시피 추천",
        message = "레시피 추천해줘",
    ),
    ChatQuickReply(
        label = "보유 재료 레시피",
        message = "보유 중인 식재료로 만들 수 있는 레시피 추천해줘",
    ),
    ChatQuickReply(
        label = "냉장고 재료",
        message = "냉장고에 있는 재료로 만들 수 있는 레시피 추천해줘",
    ),
    ChatQuickReply(
        label = "소비 임박",
        message = "소비 임박 재료로 만들 수 있는 레시피 추천해줘",
    ),
    ChatQuickReply(
        label = "양식 메뉴",
        message = "양식 메뉴 추천해줘",
    ),
)

@Composable
fun ChatQuickRepliesRow(
    onQuickReplyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    quickReplies: List<ChatQuickReply> = defaultChatQuickReplies,
) {
    val scrollState = rememberScrollState()

    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        quickReplies.forEach { reply ->
            ChatQuickReplyChip(
                label = reply.label,
                enabled = enabled,
                onClick = { onQuickReplyClick(reply.message) },
            )
        }
    }
}

@Composable
private fun ChatQuickReplyChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val bg = if (enabled) Color(0xFFE8FAF2) else Color(0xFFF3F4F6)
    val borderColor = if (enabled) Color(0xFFBBF7D0) else ChatDesign.BorderSoft
    val textColor = if (enabled) Color(0xFF047857) else ChatDesign.TextMuted

    Text(
        text = label,
        modifier = modifier
            .clip(shape)
            .background(bg, shape)
            .border(1.dp, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        ),
        color = textColor,
        maxLines = 1,
    )
}
