package com.example.myfrigelocal.ui.screens.chat

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myfrigelocal.ui.theme.BottomNavSelected

/** Shared visual tokens for AI chat surfaces (UI only). */
object ChatDesign {
    /** Primary mint — matches the sidebar "새 채팅" button (#32E0A1). */
    val ChatPrimary = BottomNavSelected

    val ScreenBg = Color(0xFFF6F8F7)
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

    val TopBarShape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
    val CardShape = RoundedCornerShape(20.dp)
    val InputBarShape = RoundedCornerShape(28.dp)
    val BubbleAiShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 18.dp,
        bottomStart = 6.dp,
    )
    val BubbleUserShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 6.dp,
        bottomStart = 18.dp,
    )
    val BubbleMaxWidthFraction = 0.82f
}
