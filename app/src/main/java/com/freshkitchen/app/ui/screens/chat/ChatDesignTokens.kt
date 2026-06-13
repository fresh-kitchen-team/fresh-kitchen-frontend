package com.freshkitchen.app.ui.screens.chat

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freshkitchen.app.ui.theme.BottomNavSelected

/** Shared visual tokens for AI chat surfaces (UI only). */
object ChatDesign {
    /** Primary mint — matches the sidebar "새 채팅" button (#32E0A1). */
    val ChatPrimary = BottomNavSelected

    /** Brand greens (aligned with the home hero gradient). */
    val BrandGreen = Color(0xFF4ADE80)
    val BrandGreenDark = Color(0xFF22C55E)
    val BrandGreenDeep = Color(0xFF059669)

    val ScreenBg = Color(0xFFF4F8F6)
    val SurfaceWhite = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)
    val BorderSoft = Color(0xFFE8ECE9)
    val AiAvatarBg = Color(0xFFDFF7ED)
    val AiAvatarTint = ChatPrimary
    val UserBubble = ChatPrimary
    val UserBubbleText = Color.White
    val ErrorBg = Color(0xFFFFF5F5)
    val ErrorBorder = Color(0xFFFECDD3)
    val ErrorText = Color(0xFFB91C1C)
    val DrawerSelectedBg = Color(0xFFE8FAF2)
    val DrawerSelectedAccent = ChatPrimary

    /** Soft tinted backgrounds for chips / pills. */
    val MintSoft = Color(0xFFE8FAF2)
    val MintSoftBorder = Color(0xFFBBF7D0)
    val MintDeepText = Color(0xFF047857)

    /** Brand gradient used for AI avatar, send button, primary CTAs. */
    val BrandGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF34E3A4), BrandGreenDark),
    )

    /** Gradient for the outgoing (user) message bubble. */
    val UserBubbleGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3DE3A6), Color(0xFF24C98A)),
    )

    /** Subtle background wash behind the conversation. */
    val ConversationBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF1F8F4), Color(0xFFF6F8F7)),
    )

    val TopBarShape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
    val CardShape = RoundedCornerShape(20.dp)
    val InputBarShape = RoundedCornerShape(28.dp)
    val BubbleAiShape = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 20.dp,
        bottomEnd = 20.dp,
        bottomStart = 20.dp,
    )
    val BubbleUserShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomEnd = 6.dp,
        bottomStart = 20.dp,
    )
    val BubbleMaxWidthFraction = 0.82f
}
