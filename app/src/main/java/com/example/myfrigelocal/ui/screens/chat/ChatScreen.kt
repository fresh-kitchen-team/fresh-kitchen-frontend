package com.example.myfrigelocal.ui.screens.chat

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.myfrigelocal.R
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.BottomNavUnselected
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme
import androidx.compose.ui.platform.LocalContext
import java.util.UUID
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.myfrigelocal.ui.screens.help.HelpFeedbackScreen
import com.example.myfrigelocal.ui.screens.help.ContactSupportScreen
import com.example.myfrigelocal.ui.screens.help.ReportIssueScreen

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    /** Mirrors backend `type`: [AI_RESPONSE_TYPE_TEXT] or [AI_RESPONSE_TYPE_RECIPE]. */
    val responseType: String = AI_RESPONSE_TYPE_TEXT,
    val recipe: RecipeUiModel? = null,
    /**
     * Raw Swagger `aiPayload` string (if any). Do not parse until backend documents JSON shape.
     */
    val aiPayloadRaw: String? = null,
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
) {
    var input by rememberSaveable { mutableStateOf("") }
    var attachedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var isSideMenuOpen by rememberSaveable { mutableStateOf(false) }
    var isAiSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var isHelpFeedbackOpen by rememberSaveable { mutableStateOf(false) }
    var isContactSupportOpen by rememberSaveable { mutableStateOf(false) }
    var isReportIssueOpen by rememberSaveable { mutableStateOf(false) }
    var openedMenuThreadId by remember { mutableStateOf<String?>(null) }
    var editingThreadId by remember { mutableStateOf<String?>(null) }
    var editingTitleSeed by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val cameraActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val uriString = result.data?.getStringExtra(CameraCaptureActivity.RESULT_IMAGE_URI)
            if (!uriString.isNullOrBlank()) {
                attachedImageUri = uriString
            }
        },
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                cameraActivityLauncher.launch(Intent(context, CameraCaptureActivity::class.java))
            }
        },
    )

    LaunchedEffect(currentThreadId, messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Bottom-nav reselect: return to base chat state without clearing history.
    LaunchedEffect(reselectToken) {
        if (reselectToken == 0L) return@LaunchedEffect
        isSideMenuOpen = false
        isAiSettingsOpen = false
        isHelpFeedbackOpen = false
        isContactSupportOpen = false
        isReportIssueOpen = false
        openedMenuThreadId = null
        editingThreadId = null
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
                .background(Color(0xFFF6F8F7))
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
                    color = BottomNavSelected,
                )
            }

            errorMessage?.let { err ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color(0xFFFFF1F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = err,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF991B1B),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "닫기",
                            color = Color(0xFF991B1B),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onDismissError() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(message = message)
                }
            }

            ChatInputBar(
                inputValue = input,
                onInputChange = { input = it },
                sendEnabled = !isSending && currentThreadId.isNotBlank(),
                onPlusClick = {
                    // 디자인 전용 더미 동작
                },
                attachedImageUri = attachedImageUri?.let(Uri::parse),
                onRemoveAttachedImage = { attachedImageUri = null },
                onCameraClick = {
                    val perm = android.Manifest.permission.CAMERA
                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        perm,
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        cameraActivityLauncher.launch(Intent(context, CameraCaptureActivity::class.java))
                    } else {
                        cameraPermissionLauncher.launch(perm)
                    }
                },
                onSend = { text ->
                    val trimmed = text.trim()
                    if (trimmed.isEmpty() || currentThreadId.isBlank()) return@ChatInputBar
                    onSendMessage(trimmed)
                    input = ""
                    attachedImageUri = null
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
            modifier = Modifier.zIndex(1f),
        )

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
                modifier = Modifier.zIndex(2f),
            )
        }

        if (isHelpFeedbackOpen) {
            BackHandler { isHelpFeedbackOpen = false }
            HelpFeedbackScreen(
                onClose = { isHelpFeedbackOpen = false },
                modifier = Modifier.zIndex(3f),
                onContactSupportClick = {
                    isContactSupportOpen = true
                },
                onReportIssueClick = {
                    isReportIssueOpen = true
                },
            )
        }

        if (isContactSupportOpen) {
            BackHandler { isContactSupportOpen = false }
            ContactSupportScreen(
                onClose = { isContactSupportOpen = false },
                modifier = Modifier.zIndex(4f),
            )
        }

        if (isReportIssueOpen) {
            BackHandler { isReportIssueOpen = false }
            ReportIssueScreen(
                onClose = { isReportIssueOpen = false },
                modifier = Modifier.zIndex(4f),
            )
        }
    }
}

private enum class AiResponseStyle { Friendly, Simple }

@Composable
private fun AiSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Defaults match the reference image (green = on, gray = off)
    var extraInfo by rememberSaveable { mutableStateOf(true) }
    var recommendExpiryFirst by rememberSaveable { mutableStateOf(true) }
    var recommendNutritionBalanced by rememberSaveable { mutableStateOf(false) }
    var recommendFavoriteIngredients by rememberSaveable { mutableStateOf(true) }
    var notifyRecipeDone by rememberSaveable { mutableStateOf(true) }
    var notifyAiRecommend by rememberSaveable { mutableStateOf(false) }
    var responseStyle by rememberSaveable { mutableStateOf(AiResponseStyle.Friendly) }
    var includeImages by rememberSaveable { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)),
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        contentDescription = "Close",
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
                SettingsSectionTitle("알림 설정")
                SettingsToggleRow(
                    label = "레시피 생성 완료 알림",
                    checked = notifyRecipeDone,
                    onCheckedChange = { notifyRecipeDone = it },
                )
                SettingsToggleRow(
                    label = "AI 추천 알림",
                    checked = notifyAiRecommend,
                    onCheckedChange = { notifyAiRecommend = it },
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

                Spacer(modifier = Modifier.size(14.dp))

                SettingsToggleRow(
                    label = "이미지 포함 응답",
                    checked = includeImages,
                    onCheckedChange = { includeImages = it },
                )
            }

            Spacer(modifier = Modifier.size(22.dp))
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
                checkedTrackColor = Color(0xFF2EEA92),
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
    val bg = if (selected) BottomNavSelected else Color.Transparent
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_ai_chat_app),
                contentDescription = "App icon",
                modifier = Modifier.size(32.dp),
            )
        }

        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val isAi = message.sender == Sender.Ai

    if (isAi) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFDFF7ED),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = "AI",
                        tint = Color(0xFF37C18A),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Assistant",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.size(6.dp))

                val recipePayload = message.recipe
                if (message.responseType == AI_RESPONSE_TYPE_RECIPE && recipePayload != null) {
                    RecipeResponseCard(recipe = recipePayload)
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            color = Color(0xFF111827),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
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
                modifier = Modifier.widthIn(max = 280.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "나",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.size(6.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BottomNavSelected,
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    )
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = Color(0xFFE5E7EB),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "User avatar",
                        tint = Color(0xFF6B7280),
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
    onPlusClick: () -> Unit,
    onCameraClick: () -> Unit,
    attachedImageUri: Uri?,
    onRemoveAttachedImage: () -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
    ) {
        Column {
            if (attachedImageUri != null) {
                AttachedImagePreview(
                    uri = attachedImageUri,
                    onRemove = onRemoveAttachedImage,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                    .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // + 버튼: 터치 영역은 유지하고(40dp), 보이는 원은 작게(22dp)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPlusClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                IconButton(
                    onClick = onCameraClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp),
                    )
                }

                TextField(
                    value = inputValue,
                    onValueChange = { if (sendEnabled) onInputChange(it) },
                    enabled = sendEnabled,
                    placeholder = {
                        Text(
                            text = "메시지를 입력하세요...",
                            color = Color(0xFF9CA3AF),
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (state.isFocused) keyboardController?.show()
                        },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSend(inputValue) },
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )

                IconButton(
                    onClick = { if (sendEnabled) onSend(inputValue) },
                    enabled = sendEnabled,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BottomNavSelected, CircleShape),
                ) {
                    Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachedImagePreview(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var imageBitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uri) {
        imageBitmap = try {
            if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (_: Throwable) {
            null
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.size(64.dp),
        ) {
            val bmp = imageBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Attached image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("이미지", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        IconButton(onClick = onRemove) {
            Text("X", color = Color(0xFF6B7280))
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

