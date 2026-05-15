package com.example.myfrigelocal.ui.screens

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.myfrigelocal.ui.screens.chat.ChatScreen
import com.example.myfrigelocal.viewmodel.AiChatViewModel

@Composable
fun AiChatScreen(
    backStackEntry: NavBackStackEntry,
) {
    val reselectToken = backStackEntry.savedStateHandle
        .getStateFlow("ai_chat_reselect", 0L)
        .collectAsStateWithLifecycle()

    val viewModel: AiChatViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        reselectToken = reselectToken.value,
        topBarTitle = state.topBarTitle,
        sideMenuItems = state.sideMenuItems,
        messages = state.messages,
        currentThreadId = state.currentRoomId?.toString().orEmpty(),
        isLoadingRooms = state.isLoadingRooms,
        isLoadingMessages = state.isLoadingMessages,
        isSending = state.isSending,
        errorMessage = state.error,
        onDismissError = viewModel::dismissError,
        onSelectThread = { id -> viewModel.selectRoom(id.toLong()) },
        onNewChat = viewModel::createRoom,
        onSendMessage = viewModel::sendMessage,
        onRenameRoomLocal = { threadId, title ->
            threadId.toLongOrNull()?.let { id ->
                viewModel.updateRoomTitle(id, title)
            }
        },
    )
}
