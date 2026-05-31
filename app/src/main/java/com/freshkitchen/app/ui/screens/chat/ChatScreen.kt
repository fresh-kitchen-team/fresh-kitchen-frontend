package com.freshkitchen.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshkitchen.app.R
import com.freshkitchen.app.data.remote.dto.AiSettingDto
import com.freshkitchen.app.ui.theme.BottomNavUnselected
import com.freshkitchen.app.ui.theme.MyFrigeLocalTheme
import java.util.UUID
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.freshkitchen.app.ui.screens.help.HelpFeedbackScreen
import com.freshkitchen.app.ui.screens.help.ContactSupportScreen
import com.freshkitchen.app.ui.screens.help.InquiryDetailScreen
import com.freshkitchen.app.ui.screens.help.InquiryListScreen
import com.freshkitchen.app.ui.screens.help.ReportIssueScreen

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    /** Mirrors backend `uiType`: [AI_RESPONSE_TYPE_TEXT] (GENERAL) or [AI_RESPONSE_TYPE_RECIPE] (RECIPE). */
    val responseType: String = AI_RESPONSE_TYPE_TEXT,
    val recipe: RecipeUiModel? = null,
    /** Local-only placeholder while waiting for AI response. */
    val isLoading: Boolean = false,
)

enum class Sender {
    Ai,
    User,
}

private fun dummyRecipe(): RecipeUiModel = RecipeUiModel(
    title = "토마토 계란 볶음",
    cookTime = "10분",
    ingredients = listOf("계란", "토마토", "소금", "식용유"),
    steps = listOf(
        "계란을 풀어 준비합니다",
        "토마토를 먹기 좋게 자릅니다",
        "팬에 기름을 두르고 토마토를 볶습니다",
        "계란을 넣고 함께 볶습니다",
    ),
    tip = "토마토는 너무 오래 볶지 않는 것이 좋아요.",
    missingIngredients = listOf("소금", "식용유"),
    imageUrl = "",
)

@Composable
fun ChatScreen(
    reselectToken: Long = 0L,
    modifier: Modifier = Modifier,
    topBarTitle: String = "AI 주방 비서",
    sideMenuItems: List<SideMenuItem>,
    messages: List<ChatMessage>,
    currentThreadId: String,
    isLoadingRooms: Boolean = false,
    isLoadingMessages: Boolean = false,
    isSending: Boolean = false,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    onSelectThread: (String) -> Unit = {},
    onNewChat: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onRenameRoomLocal: (threadId: String, newTitle: String) -> Unit = { _, _ -> },
    onDeleteRoom: (threadId: String) -> Unit = {},
    isSubmittingSupport: Boolean = false,
    supportError: String? = null,
    supportSuccessMessage: String? = null,
    supportSubmitSuccessToken: Long = 0L,
    onDismissSupportError: () -> Unit = {},
    onSubmitInquiry: (categoryLabel: String, content: String, imageUri: String?) -> Unit = { _, _, _ -> },
    onSubmitReport: (categoryLabel: String, content: String, imageUri: String?) -> Unit = { _, _, _ -> },
    onSaveAiSettings: (AiSettingDto) -> Unit = {},
    onEnrichRecipeMatchedItems: suspend (List<RecipeMatchedItemUi>) -> List<RecipeMatchedItemUi> = { it },
    onConsumeRecipeMatchedItems: suspend (List<RecipeMatchedItemUi>) -> Result<Int> = {
        Result.failure(UnsupportedOperationException())
    },
) {
    var input by rememberSaveable { mutableStateOf("") }
    var isSideMenuOpen by rememberSaveable { mutableStateOf(false) }
    var isAiSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var isHelpFeedbackOpen by rememberSaveable { mutableStateOf(false) }
    var isInquiryListOpen by rememberSaveable { mutableStateOf(false) }
    var selectedInquiryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isContactSupportOpen by rememberSaveable { mutableStateOf(false) }
    var isReportIssueOpen by rememberSaveable { mutableStateOf(false) }
    var openedMenuThreadId by remember { mutableStateOf<String?>(null) }
    var editingThreadId by remember { mutableStateOf<String?>(null) }
    var editingTitleSeed by remember { mutableStateOf("") }
    var deletingThreadId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(currentThreadId, messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LaunchedEffect(supportSubmitSuccessToken) {
        if (supportSubmitSuccessToken > 0L) {
            isContactSupportOpen = false
            isReportIssueOpen = false
            isInquiryListOpen = false
            selectedInquiryId = null
            isHelpFeedbackOpen = false
        }
    }

    // Bottom-nav reselect: return to base chat state without clearing history.
    LaunchedEffect(reselectToken) {
        if (reselectToken == 0L) return@LaunchedEffect
        isSideMenuOpen = false
        isAiSettingsOpen = false
        isHelpFeedbackOpen = false
        isInquiryListOpen = false
        selectedInquiryId = null
        isContactSupportOpen = false
        isReportIssueOpen = false
        openedMenuThreadId = null
        editingThreadId = null
        deletingThreadId = null
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatDesign.ScreenBg)
                .zIndex(0f),
        ) {
            ChatTopBar(
                title = topBarTitle,
                onMenuClick = { isSideMenuOpen = true },
            )

            if (isLoadingRooms || isLoadingMessages || isSending) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = ChatDesign.ChatPrimary,
                )
            }

            errorMessage?.let { err ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = ChatDesign.ErrorBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.ErrorBorder),
                ) {
                    Row(
                        modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = ChatDesign.ErrorText,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = err,
                            modifier = Modifier.weight(1f),
                            color = ChatDesign.ErrorText,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        )
                        IconButton(onClick = onDismissError) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "닫기",
                                tint = ChatDesign.ErrorText,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onEnrichRecipeMatchedItems = onEnrichRecipeMatchedItems,
                        onConsumeRecipeMatchedItems = onConsumeRecipeMatchedItems,
                    )
                }
            }

            ChatQuickRepliesRow(
                enabled = !isSending,
                onQuickReplyClick = { message ->
                    keyboardController?.hide()
                    onSendMessage(message)
                    input = ""
                },
            )

            ChatInputBar(
                inputValue = input,
                onInputChange = { input = it },
                // No room yet (e.g. after delete): ViewModel creates a room then sends the message.
                sendEnabled = !isSending,
                onSend = { text ->
                    val trimmed = text.trim()
                    if (trimmed.isEmpty()) return@ChatInputBar
                    onSendMessage(trimmed)
                    input = ""
                },
            )
        }

        SideMenuDrawer(
            isOpen = isSideMenuOpen,
            onClose = {
                openedMenuThreadId = null
                isSideMenuOpen = false
            },
            items = sideMenuItems,
            onSelectThread = { threadId ->
                onSelectThread(threadId)
                isSideMenuOpen = false
                openedMenuThreadId = null
            },
            onNewChat = {
                onNewChat()
                isSideMenuOpen = false
                openedMenuThreadId = null
            },
            onSettingsClick = {
                isAiSettingsOpen = true
            },
            onHelpClick = {
                isHelpFeedbackOpen = true
            },
            openedMenuThreadId = openedMenuThreadId,
            onToggleChatMenu = { threadId ->
                openedMenuThreadId = if (openedMenuThreadId == threadId) null else threadId
            },
            onDismissChatMenu = { openedMenuThreadId = null },
            onEditChatTitleFromMenu = { threadId, currentTitle ->
                openedMenuThreadId = null
                editingThreadId = threadId
                editingTitleSeed = currentTitle
            },
            onDeleteChatFromMenu = { threadId ->
                openedMenuThreadId = null
                deletingThreadId = threadId
            },
            modifier = Modifier.zIndex(1f),
        )

        deletingThreadId?.let { tid ->
            Box(Modifier.fillMaxSize().zIndex(6f)) {
                DeleteChatConfirmDialog(
                    onDismiss = { deletingThreadId = null },
                    onConfirmDelete = {
                        onDeleteRoom(tid)
                        deletingThreadId = null
                        isSideMenuOpen = false
                    },
                )
            }
        }

        editingThreadId?.let { tid ->
            Box(Modifier.fillMaxSize().zIndex(5f)) {
                EditChatTitleDialog(
                    initialTitle = editingTitleSeed,
                    onDismiss = { editingThreadId = null },
                    onSave = { trimmed ->
                        onRenameRoomLocal(tid, trimmed)
                        editingThreadId = null
                    },
                )
            }
        }

        if (isAiSettingsOpen) {
            BackHandler { isAiSettingsOpen = false }
            AiSettingsScreen(
                onClose = { isAiSettingsOpen = false },
                onSave = { settings ->
                    onSaveAiSettings(settings)
                    isAiSettingsOpen = false
                },
                modifier = Modifier.zIndex(2f),
            )
        }

        if (isHelpFeedbackOpen) {
            BackHandler {
                when {
                    selectedInquiryId != null -> selectedInquiryId = null
                    isInquiryListOpen -> isInquiryListOpen = false
                    else -> isHelpFeedbackOpen = false
                }
            }
            HelpFeedbackScreen(
                onClose = { isHelpFeedbackOpen = false },
                modifier = Modifier.zIndex(3f),
                onInquiryListClick = { isInquiryListOpen = true },
                onContactSupportClick = {
                    isContactSupportOpen = true
                },
                onReportIssueClick = {
                    isReportIssueOpen = true
                },
            )
        }

        if (isInquiryListOpen) {
            InquiryListScreen(
                onClose = {
                    isInquiryListOpen = false
                    selectedInquiryId = null
                },
                onAnsweredItemClick = { selectedInquiryId = it },
                modifier = Modifier.zIndex(4f),
            )
        }

        selectedInquiryId?.let { inquiryId ->
            InquiryDetailScreen(
                inquiryId = inquiryId,
                onClose = { selectedInquiryId = null },
                modifier = Modifier.zIndex(5f),
            )
        }

        if (isContactSupportOpen) {
            BackHandler { isContactSupportOpen = false }
            ContactSupportScreen(
                onClose = { isContactSupportOpen = false },
                modifier = Modifier.zIndex(6f),
                isSubmitting = isSubmittingSupport,
                errorMessage = supportError,
                successMessage = supportSuccessMessage,
                onDismissError = onDismissSupportError,
                onSubmit = onSubmitInquiry,
            )
        }

        if (isReportIssueOpen) {
            BackHandler { isReportIssueOpen = false }
            ReportIssueScreen(
                onClose = { isReportIssueOpen = false },
                modifier = Modifier.zIndex(6f),
                isSubmitting = isSubmittingSupport,
                errorMessage = supportError,
                successMessage = supportSuccessMessage,
                onDismissError = onDismissSupportError,
                onSubmit = onSubmitReport,
            )
        }
    }
}

private enum class AiResponseStyle { Friendly, Simple }

@Composable
private fun AiSettingsScreen(
    onClose: () -> Unit,
    onSave: (AiSettingDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    var extraInfo by rememberSaveable { mutableStateOf(true) }
    var recommendExpiryFirst by rememberSaveable { mutableStateOf(true) }
    var recommendNutritionBalanced by rememberSaveable { mutableStateOf(false) }
    var recommendFavoriteIngredients by rememberSaveable { mutableStateOf(true) }
    var responseStyle by rememberSaveable { mutableStateOf(AiResponseStyle.Friendly) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp),
            ) {
                Spacer(modifier = Modifier.size(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "AI 설정",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF111827),
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                SettingsCard {
                    SettingsSectionTitle("AI 기능")
                    SettingsToggleRow(
                        label = "추가 정보 제공 (영양, 팁 등)",
                        checked = extraInfo,
                        onCheckedChange = { extraInfo = it },
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                SettingsCard {
                    SettingsSectionTitle("추천 기준")
                    Text(
                        text = "(여러 개 선택 가능)",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                    )
                    SettingsToggleRow(
                        label = "유통기한 우선 추천",
                        checked = recommendExpiryFirst,
                        onCheckedChange = { recommendExpiryFirst = it },
                    )
                    SettingsToggleRow(
                        label = "영양 균형 기반 추천",
                        checked = recommendNutritionBalanced,
                        onCheckedChange = { recommendNutritionBalanced = it },
                    )
                    SettingsToggleRow(
                        label = "자주 사용하는 재료 우선",
                        checked = recommendFavoriteIngredients,
                        onCheckedChange = { recommendFavoriteIngredients = it },
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                SettingsCard {
                    SettingsSectionTitle("AI 응답 설정")
                    Text(
                        text = "응답 스타일",
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
                    )

                    ResponseStyleSegment(
                        selected = responseStyle,
                        onSelect = { responseStyle = it },
                    )
                }

                Spacer(modifier = Modifier.size(22.dp))
            }

            AiSettingsBottomActions(
                onCancel = onClose,
                onSave = {
                    onSave(
                        AiSettingDto(
                            responseStyle = responseStyle == AiResponseStyle.Friendly,
                            priorityExpiration = recommendExpiryFirst,
                            priorityNutrition = recommendNutritionBalanced,
                            priorityFrequent = recommendFavoriteIngredients,
                            provideExtraInfo = extraInfo,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun AiSettingsBottomActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF3F4F6),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF111827),
                ),
            ) {
                Text(
                    text = "취소",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
            }
            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChatDesign.ChatPrimary,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "저장",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = Color(0xFF111827),
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF111827),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ChatDesign.ChatPrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD1D5DB),
            ),
        )
    }
}

@Composable
private fun ResponseStyleSegment(
    selected: AiResponseStyle,
    onSelect: (AiResponseStyle) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF3F4F6),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
        ) {
            SegmentOption(
                text = "친절",
                selected = selected == AiResponseStyle.Friendly,
                onClick = { onSelect(AiResponseStyle.Friendly) },
                modifier = Modifier.weight(1f),
            )
            SegmentOption(
                text = "간단",
                selected = selected == AiResponseStyle.Simple,
                onClick = { onSelect(AiResponseStyle.Simple) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) ChatDesign.ChatPrimary else Color.Transparent
    val fg = if (selected) Color(0xFF111827) else Color(0xFF6B7280)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun ChatTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChatDesign.SurfaceWhite,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "메뉴",
                        tint = ChatDesign.TextPrimary,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_fresh_kitchen),
                    contentDescription = "FreshKitchen",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                ),
                color = ChatDesign.TextPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    onEnrichRecipeMatchedItems: suspend (List<RecipeMatchedItemUi>) -> List<RecipeMatchedItemUi> = { it },
    onConsumeRecipeMatchedItems: suspend (List<RecipeMatchedItemUi>) -> Result<Int> = {
        Result.failure(UnsupportedOperationException())
    },
) {
    val isAi = message.sender == Sender.Ai

    if (isAi) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = ChatDesign.AiAvatarBg,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = "AI",
                        tint = ChatDesign.AiAvatarTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "AI 주방 비서",
                    color = ChatDesign.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )

                Spacer(modifier = Modifier.size(6.dp))

                if (message.isLoading) {
                    AiTypingBubble(text = message.text)
                } else if (message.responseType == AI_RESPONSE_TYPE_RECIPE && message.recipe != null) {
                    RecipeResponseCard(
                        recipe = message.recipe,
                        expandStateKey = message.id,
                    )
                    RecipeConsumeSection(
                        recipe = message.recipe,
                        stateKey = "${message.id}-consume",
                        onEnrichItems = onEnrichRecipeMatchedItems,
                        onConsume = onConsumeRecipeMatchedItems,
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(ChatDesign.BubbleMaxWidthFraction),
                        shape = ChatDesign.BubbleAiShape,
                        color = ChatDesign.SurfaceWhite,
                        shadowElevation = 2.dp,
                        tonalElevation = 0.dp,
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = ChatDesign.TextPrimary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                            ),
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(ChatDesign.BubbleMaxWidthFraction),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "나",
                    color = ChatDesign.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )

                Spacer(modifier = Modifier.size(6.dp))

                Surface(
                    shape = ChatDesign.BubbleUserShape,
                    color = ChatDesign.UserBubble,
                    shadowElevation = 1.dp,
                    tonalElevation = 0.dp,
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        color = ChatDesign.UserBubbleText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = Color(0xFFE8ECE9),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "User avatar",
                        tint = ChatDesign.TextSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    inputValue: String,
    onInputChange: (String) -> Unit,
    sendEnabled: Boolean = true,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val sendButtonAlpha = if (sendEnabled) 1f else 0.45f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = ChatDesign.InputBarShape,
        color = ChatDesign.SurfaceWhite,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = sendEnabled,
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = inputValue,
                onValueChange = { if (sendEnabled) onInputChange(it) },
                enabled = sendEnabled,
                placeholder = {
                    Text(
                        text = "메시지를 입력하세요...",
                        color = ChatDesign.TextMuted,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) keyboardController?.show()
                    },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ChatDesign.TextPrimary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (sendEnabled) onSend(inputValue) },
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = ChatDesign.TextSecondary,
                ),
            )

            IconButton(
                onClick = { if (sendEnabled) onSend(inputValue) },
                enabled = sendEnabled,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        ChatDesign.UserBubble.copy(alpha = sendButtonAlpha),
                        CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "전송",
                    tint = Color.White.copy(alpha = sendButtonAlpha.coerceAtLeast(0.7f)),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ChatScreenPreview() {
    val recipe = dummyRecipe()
    val previewMessages = listOf(
        ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.Ai,
            text = "냉장고 안에 있는 재료로 오늘의 요리를 추천해드릴게요.",
        ),
        ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.User,
            text = "냉장고에 토마토랑 달걀만 있어요.",
        ),
        ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = Sender.Ai,
            text = recipe.title,
            responseType = AI_RESPONSE_TYPE_RECIPE,
            recipe = recipe,
        ),
    )
    val previewMenu = listOf(
        SideMenuItem(
            threadId = "1",
            title = "토마토 & 계란 레시피",
            section = "오늘",
            selected = true,
        ),
        SideMenuItem(threadId = "2", title = "냉장고 재고 확인", section = "지난 7일", selected = false),
    )
    MyFrigeLocalTheme {
        ChatScreen(
            sideMenuItems = previewMenu,
            messages = previewMessages,
            currentThreadId = "1",
        )
    }
}

