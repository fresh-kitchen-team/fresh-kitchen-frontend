package com.freshkitchen.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.freshkitchen.app.ui.screens.chat.ChatScreen
import com.freshkitchen.app.viewmodel.AiChatViewModel

@Composable
fun AiChatScreen(
    backStackEntry: NavBackStackEntry,
) {
    val reselectToken = backStackEntry.savedStateHandle
        .getStateFlow("ai_chat_reselect", 0L)
        .collectAsStateWithLifecycle()

    // Activity-scoped: survives bottom-tab switches (Nav destination VM can be cleared on leave).
    val activity = LocalContext.current as ComponentActivity
    val viewModel: AiChatViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            activity.application,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(reselectToken.value) {
        viewModel.onAiChatScreenVisible()
        onPauseOrDispose { }
    }

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
        onDeleteRoom = { threadId ->
            threadId.toLongOrNull()?.let { id ->
                viewModel.deleteRoom(id)
            }
        },
        isSubmittingSupport = state.isSubmittingSupport,
        supportError = state.supportError,
        supportSuccessMessage = state.supportSuccessMessage,
        supportSubmitSuccessToken = state.supportSubmitSuccessToken,
        onDismissSupportError = viewModel::dismissSupportError,
        onSubmitInquiry = { category, content, imageUri ->
            viewModel.submitInquiry(category, content, imageUri)
        },
        onSubmitReport = { category, content, imageUri ->
            viewModel.submitReport(category, content, imageUri)
        },
        aiSetting = state.aiSetting,
        isLoadingAiSetting = state.isLoadingAiSetting,
        isSavingAiSetting = state.isSavingAiSetting,
        onLoadAiSettings = viewModel::loadAiSettings,
        onSaveAiSettings = viewModel::updateAiSetting,
        quickReplies = state.quickReplies,
        onEnrichRecipeMatchedItems = viewModel::enrichRecipeMatchedItems,
        onConsumeRecipeMatchedItems = viewModel::consumeRecipeMatchedItems,
    )
}
