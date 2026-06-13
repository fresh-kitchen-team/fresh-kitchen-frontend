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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
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
    aiSetting: AiSettingDto? = null,
    isLoadingAiSetting: Boolean = false,
    isSavingAiSetting: Boolean = false,
    onLoadAiSettings: () -> Unit = {},
    onSaveAiSettings: (AiSettingDto, (Boolean) -> Unit) -> Unit = { _, _ -> },
    quickReplies: List<ChatQuickReply> = fixedChatQuickReplies,
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
                .background(ChatDesign.ConversationBg)
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

            if (messages.isEmpty() && !isLoadingMessages && !isLoadingRooms) {
                ChatWelcomeState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 20.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            onEnrichRecipeMatchedItems = onEnrichRecipeMatchedItems,
                            onConsumeRecipeMatchedItems = onConsumeRecipeMatchedItems,
                        )
                    }
                }
            }

            ChatQuickRepliesRow(
                enabled = !isSending,
                quickReplies = quickReplies,
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
                onLoadAiSettings()
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
            BackHandler(enabled = !isSavingAiSetting) { isAiSettingsOpen = false }
            AiSettingsScreen(
                settings = aiSetting,
                isLoading = isLoadingAiSetting,
                isSaving = isSavingAiSetting,
                onClose = {
                    if (!isSavingAiSetting) isAiSettingsOpen = false
                },
                onSave = { settings ->
                    onSaveAiSettings(settings) { success ->
                        if (success) isAiSettingsOpen = false
                    }
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
    settings: AiSettingDto?,
    isLoading: Boolean,
    isSaving: Boolean,
    onClose: () -> Unit,
    onSave: (AiSettingDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    // NOTE: Don't show arbitrary defaults before GET succeeds.
    if (settings == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ChatDesign.ScreenBg),
        ) {
            AiSettingsHeader(onClose = onClose, enabled = !isSaving)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = ChatDesign.ChatPrimary)
                } else {
                    Text(
                        text = "설정을 불러오지 못했습니다.",
                        color = ChatDesign.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        return
    }

    var extraInfo by rememberSaveable(settings) { mutableStateOf(settings.provideExtraInfo) }
    var recommendExpiryFirst by rememberSaveable(settings) { mutableStateOf(settings.priorityExpiration) }
    var recommendNutritionBalanced by rememberSaveable(settings) { mutableStateOf(settings.priorityNutrition) }
    var recommendFavoriteIngredients by rememberSaveable(settings) { mutableStateOf(settings.priorityFrequent) }
    var responseStyle by rememberSaveable(settings) {
        mutableStateOf(
            if (settings.responseStyle) AiResponseStyle.Friendly else AiResponseStyle.Simple,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChatDesign.ScreenBg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AiSettingsHeader(onClose = onClose, enabled = !isSaving)

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(modifier = Modifier.size(2.dp))

                SettingsHeroCard()

                SettingsSection(
                    icon = Icons.Outlined.AutoAwesome,
                    iconBg = ChatDesign.MintSoft,
                    iconTint = ChatDesign.BrandGreenDeep,
                    title = "AI 기능",
                    subtitle = "답변에 포함할 내용을 선택하세요",
                ) {
                    SettingsToggleRow(
                        label = "추가 정보 제공",
                        description = "영양 정보, 조리 팁 등을 함께 안내",
                        checked = extraInfo,
                        onCheckedChange = { extraInfo = it },
                    )
                }

                SettingsSection(
                    icon = Icons.Outlined.Tune,
                    iconBg = Color(0xFFEAF2FF),
                    iconTint = Color(0xFF3B82F6),
                    title = "추천 기준",
                    subtitle = "여러 개를 함께 선택할 수 있어요",
                ) {
                    SettingsToggleRow(
                        label = "유통기한 우선 추천",
                        description = "소비 임박 재료를 먼저 활용해요",
                        checked = recommendExpiryFirst,
                        onCheckedChange = { recommendExpiryFirst = it },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        label = "영양 균형 기반 추천",
                        description = "영양이 고르게 갖춰진 레시피 위주",
                        checked = recommendNutritionBalanced,
                        onCheckedChange = { recommendNutritionBalanced = it },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        label = "자주 사용하는 재료 우선",
                        description = "평소 즐겨 쓰는 재료를 반영해요",
                        checked = recommendFavoriteIngredients,
                        onCheckedChange = { recommendFavoriteIngredients = it },
                    )
                }

                SettingsSection(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    iconBg = Color(0xFFFFF1E6),
                    iconTint = Color(0xFFF97316),
                    title = "AI 응답 설정",
                    subtitle = "답변 말투를 선택하세요",
                ) {
                    ResponseStyleSegment(
                        selected = responseStyle,
                        onSelect = { responseStyle = it },
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))
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
                isSaving = isSaving,
                enabled = !isLoading && !isSaving,
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChatDesign.ChatPrimary)
            }
        }
    }
}

@Composable
private fun AiSettingsHeader(
    onClose: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChatDesign.SurfaceWhite,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(start = 18.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "AI 설정",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChatDesign.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ChatDesign.ScreenBg)
                        .clickable(
                            enabled = enabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "닫기",
                        tint = ChatDesign.TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            HorizontalDivider(thickness = 1.dp, color = ChatDesign.BorderSoft)
        }
    }
}

@Composable
private fun SettingsHeroCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ChatDesign.BrandGradient)
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "나만의 AI 비서 맞춤 설정",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "추천 기준과 말투를 설정하면 더 잘 맞는\n레시피를 받을 수 있어요.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    SettingsCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ChatDesign.TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ChatDesign.TextMuted,
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 2.dp),
        thickness = 1.dp,
        color = ChatDesign.BorderSoft,
    )
}

@Composable
private fun AiSettingsBottomActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChatDesign.SurfaceWhite,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ChatDesign.TextPrimary,
                ),
            ) {
                Text(
                    text = "취소",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(ChatDesign.BrandGradient)
                    .alpha(if (enabled) 1f else 0.5f)
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSave,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "저장",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = ChatDesign.TextPrimary,
            )
            if (description != null) {
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ChatDesign.TextMuted,
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
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
        color = Color(0xFFF1F3F2),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SegmentOption(
                text = "친절하게",
                selected = selected == AiResponseStyle.Friendly,
                onClick = { onSelect(AiResponseStyle.Friendly) },
                modifier = Modifier.weight(1f),
            )
            SegmentOption(
                text = "간단하게",
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
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (selected) {
                    Modifier.background(ChatDesign.BrandGradient)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else ChatDesign.TextSecondary,
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
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(ChatDesign.ScreenBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onMenuClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "메뉴",
                        tint = ChatDesign.TextPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChatDesign.BrandGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp),
                    )
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        ),
                        color = ChatDesign.TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ChatDesign.BrandGreenDark),
                        )
                        Text(
                            text = "AI 주방 비서",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChatDesign.TextMuted,
                        )
                    }
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = ChatDesign.BorderSoft,
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
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChatDesign.BrandGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SmartToy,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp),
                )
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
                    modifier = Modifier.padding(start = 4.dp),
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
                        shadowElevation = 3.dp,
                        tonalElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                            color = ChatDesign.TextPrimary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
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
                    modifier = Modifier.padding(end = 4.dp),
                )

                Spacer(modifier = Modifier.size(6.dp))

                Box(
                    modifier = Modifier
                        .clip(ChatDesign.BubbleUserShape)
                        .background(ChatDesign.UserBubbleGradient),
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        color = ChatDesign.UserBubbleText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color(0xFFEDF1EF),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "User avatar",
                        tint = ChatDesign.TextSecondary,
                        modifier = Modifier.size(23.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatWelcomeState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ChatDesign.BrandGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        Text(
            text = "무엇을 요리해 볼까요?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ChatDesign.TextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = "냉장고 속 재료로 만들 수 있는 레시피를\n추천해 드릴게요. 아래 버튼으로 시작해 보세요.",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
            color = ChatDesign.TextSecondary,
            textAlign = TextAlign.Center,
        )
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

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ChatDesign.BrandGradient)
                    .alpha(sendButtonAlpha)
                    .clickable(
                        enabled = sendEnabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSend(inputValue) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "전송",
                    tint = Color.White,
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

