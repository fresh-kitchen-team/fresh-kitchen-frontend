package com.freshkitchen.app.ui.screens.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class SideMenuItem(
    val threadId: String,
    val title: String,
    val section: String,
    val selected: Boolean = false,
)

@Composable
fun SideMenuDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    items: List<SideMenuItem>,
    onSelectThread: (String) -> Unit,
    onNewChat: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    openedMenuThreadId: String? = null,
    onToggleChatMenu: (threadId: String) -> Unit = {},
    onDismissChatMenu: () -> Unit = {},
    onEditChatTitleFromMenu: (threadId: String, currentTitle: String) -> Unit = { _, _ -> },
    onDeleteChatFromMenu: (threadId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val drawerWidth = (screenWidth * 0.84f).coerceAtMost(360.dp)
    val density = LocalDensity.current
    val drawerWidthPx = remember(drawerWidth, density) { with(density) { drawerWidth.toPx() } }

    // Avoid 1-frame flash when composing open: snap to offscreen then animate in.
    val translationX = remember(drawerWidthPx) { Animatable(-drawerWidthPx) }
    // Scrim should appear only after drawer is fully open.
    val scrimAlpha = remember { Animatable(0f) }

    // If fully closed, don't keep an invisible layer on top (it would block hamburger taps).
    val fullyClosed = !isOpen && translationX.value <= -drawerWidthPx + 0.5f
    if (fullyClosed) return

    LaunchedEffect(isOpen, drawerWidth) {
        if (isOpen) {
            // Ensure we start from fully hidden (no flash), then animate in.
            translationX.snapTo(-drawerWidthPx)
            scrimAlpha.snapTo(0f)
            translationX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220),
            )
            // After drawer fully open, fade in scrim.
            scrimAlpha.animateTo(
                targetValue = 0.22f,
                animationSpec = tween(durationMillis = 140),
            )
        } else {
            // Fade out scrim first, then slide drawer away.
            scrimAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 120),
            )
            translationX.animateTo(
                targetValue = -drawerWidthPx,
                animationSpec = tween(durationMillis = 200),
            )
        }
    }

    // Only the right strip is clickable to close.
    val rightOverlayFraction = 1f - (drawerWidth / screenWidth)

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(rightOverlayFraction.coerceIn(0.12f, 0.25f))
                .background(Color.Black.copy(alpha = scrimAlpha.value))
                .then(
                    if (scrimAlpha.value > 0f) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose,
                        )
                    } else {
                        Modifier
                    },
                ),
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(drawerWidth)
                .graphicsLayer {
                    // Pixel-based translation avoids 1-frame layout-direction/measurement flashes.
                    this.translationX = translationX.value
                },
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
        ) {
            SideMenuContent(
                modifier = Modifier.fillMaxSize(),
                items = items,
                onSelectThread = onSelectThread,
                onNewChat = onNewChat,
                onSettingsClick = onSettingsClick,
                onHelpClick = onHelpClick,
                openedMenuThreadId = openedMenuThreadId,
                onToggleChatMenu = onToggleChatMenu,
                onDismissChatMenu = onDismissChatMenu,
                onEditChatTitleFromMenu = onEditChatTitleFromMenu,
                onDeleteChatFromMenu = onDeleteChatFromMenu,
            )
        }
    }
}

@Composable
private fun SideMenuContent(
    modifier: Modifier = Modifier,
    items: List<SideMenuItem>,
    onSelectThread: (String) -> Unit,
    onNewChat: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    openedMenuThreadId: String?,
    onToggleChatMenu: (threadId: String) -> Unit,
    onDismissChatMenu: () -> Unit,
    onEditChatTitleFromMenu: (threadId: String, currentTitle: String) -> Unit,
    onDeleteChatFromMenu: (threadId: String) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredItems by remember(searchQuery, items) {
        derivedStateOf {
            val q = searchQuery.trim()
            if (q.isEmpty()) items
            else items.filter { it.title.contains(q, ignoreCase = true) }
        }
    }

    Column(modifier = modifier.padding(top = 8.dp)) {
        // Search
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF6F8F7),
            border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "검색",
                    tint = ChatDesign.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = ChatDesign.TextPrimary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "채팅 검색",
                                color = ChatDesign.TextMuted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        inner()
                    },
                )
            }
        }

        // List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            var lastSection: String? = null
            if (filteredItems.isEmpty()) {
                item {
                    Text(
                        text = "검색 결과가 없습니다.",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    )
                }
            }

            items(filteredItems, key = { it.threadId }) { item ->
                if (lastSection != item.section) {
                    lastSection = item.section
                    Text(
                        text = item.section,
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp),
                        color = ChatDesign.TextMuted,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        ),
                    )
                }

                val bg = if (item.selected) ChatDesign.DrawerSelectedBg else Color.Transparent
                val iconTint = if (item.selected) ChatDesign.DrawerSelectedAccent else ChatDesign.TextMuted
                val titleColor = if (item.selected) ChatDesign.TextPrimary else ChatDesign.TextSecondary

                ChatRoomListItemRow(
                    item = item,
                    rowBackground = bg,
                    iconTint = iconTint,
                    titleColor = titleColor,
                    selected = item.selected,
                    menuExpanded = openedMenuThreadId == item.threadId,
                    onSelectRow = { onSelectThread(item.threadId) },
                    onToggleMenu = { onToggleChatMenu(item.threadId) },
                    onDismissMenu = onDismissChatMenu,
                    onEditChatTitle = { onEditChatTitleFromMenu(item.threadId, item.title) },
                    onDeleteChatRoom = { onDeleteChatFromMenu(item.threadId) },
                )
            }
        }

        // New chat button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .clickable { onNewChat() },
            color = ChatDesign.ChatPrimary,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "새 채팅",
                    tint = Color(0xFF065F46),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "새 채팅",
                    color = Color(0xFF065F46),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    ),
                )
            }
        }

        DrawerFooterRow(
            icon = Icons.Outlined.Settings,
            text = "AI 설정",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            onClick = onSettingsClick,
        )
        DrawerFooterRow(
            icon = Icons.Outlined.HelpOutline,
            text = "도움말 및 피드백",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            onClick = onHelpClick,
        )

        Spacer(modifier = Modifier.size(10.dp))
    }
}

@Composable
private fun ChatRoomListItemRow(
    item: SideMenuItem,
    rowBackground: Color,
    iconTint: Color,
    titleColor: Color,
    selected: Boolean,
    menuExpanded: Boolean,
    onSelectRow: () -> Unit,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditChatTitle: () -> Unit,
    onDeleteChatRoom: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowBackground)
            .padding(start = if (selected) 0.dp else 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .background(ChatDesign.DrawerSelectedAccent, RoundedCornerShape(2.dp)),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSelectRow() }
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "채팅",
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = item.title,
                color = titleColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold
                    else androidx.compose.ui.text.font.FontWeight.Normal,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        ChatRoomMoreMenu(
            expanded = menuExpanded,
            onExpandRequest = onToggleMenu,
            onDismissRequest = onDismissMenu,
            onEditChatTitle = onEditChatTitle,
            onDeleteChatRoom = onDeleteChatRoom,
        )
    }
}

@Composable
private fun DrawerFooterRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = ChatDesign.TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = text,
            color = ChatDesign.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

