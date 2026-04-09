package com.example.myfrigelocal.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.example.myfrigelocal.ui.screens.chat.ChatScreen

@Composable
fun AiChatScreen(
    backStackEntry: NavBackStackEntry,
) {
    val reselectToken = backStackEntry.savedStateHandle
        .getStateFlow("ai_chat_reselect", 0L)
        .collectAsStateWithLifecycle()

    ChatScreen(reselectToken = reselectToken.value)
}
