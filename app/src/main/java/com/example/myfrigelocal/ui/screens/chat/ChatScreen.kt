package com.example.myfrigelocal.ui.screens.chat

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfrigelocal.R
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.BottomNavUnselected
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.zIndex

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
)

enum class Sender {
    Ai,
    User,
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
) {
    val messages = remember {
        mutableStateListOf(
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
                text = "토마토 달걀 볶음 어떠세요? 맛있고 빠르게 만들 수 있어요.",
            ),
        )
    }

    var input by rememberSaveable { mutableStateOf("") }
    var attachedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var isSideMenuOpen by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
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

    LaunchedEffect(messages.size) {
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
                .zIndex(0f),
        ) {
            ChatTopBar(
                title = "AI 주방 비서",
                onMenuClick = { isSideMenuOpen = true },
            )

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
                    if (trimmed.isEmpty()) return@ChatInputBar

                    messages.add(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = Sender.User,
                            text = trimmed,
                        )
                    )
                    input = ""
                    attachedImageUri = null

                    // 간단한 더미 AI 응답(프레젠테이션용)
                    scope.launch {
                        delay(350)
                        messages.add(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                sender = Sender.Ai,
                                text = "좋아요. 바로 추천 이어갈게요!",
                            )
                        )
                    }
                },
            )
        }

        SideMenuDrawer(
            isOpen = isSideMenuOpen,
            onClose = { isSideMenuOpen = false },
            modifier = Modifier.zIndex(1f),
        )
    }
}

private data class SideMenuItem(
    val title: String,
    val section: String,
    val selected: Boolean = false,
)

@Composable
private fun SideMenuDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val drawerWidth = (screenWidth * 0.84f).coerceAtMost(360.dp)

    val offsetX by animateDpAsState(
        targetValue = if (isOpen) 0.dp else -drawerWidth,
        label = "drawerOffset",
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.22f else 0f,
        label = "scrimAlpha",
    )

    // If fully closed, don't keep an invisible layer on top (it would block hamburger taps).
    val fullyClosed = !isOpen && offsetX <= -drawerWidth + 0.5.dp
    if (fullyClosed) return

    // Box layering: draw right scrim first, then drawer on top of the left area.
    // Only the right ~16% strip is clickable to close (never the drawer / top-left).
    val rightOverlayFraction = 1f - (drawerWidth / screenWidth)

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Right-side dim + close target only (explicit width fraction, aligned to end)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(rightOverlayFraction.coerceIn(0.12f, 0.25f))
                .background(Color.Black.copy(alpha = scrimAlpha))
                .then(
                    if (scrimAlpha > 0f) {
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
                .offset(x = offsetX)
                .fillMaxHeight()
                .width(drawerWidth),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
        ) {
            SideMenuContent(
                modifier = Modifier.fillMaxSize(),
                onItemClick = { /* TODO: navigate to conversation */ },
                onNewChat = { /* TODO */ },
            )
        }
    }
}

@Composable
private fun SideMenuContent(
    modifier: Modifier = Modifier,
    onItemClick: (SideMenuItem) -> Unit,
    onNewChat: () -> Unit,
) {
    val items = remember {
        listOf(
            SideMenuItem("토마토 & 계란 레시피", section = "오늘", selected = true),
            SideMenuItem("냉장고 재고 확인", section = "어제"),
            SideMenuItem("유통기한이 지난 우유 아이디어", section = "어제"),
            SideMenuItem("42주차 식사 계획", section = "7일 이전"),
            SideMenuItem("식료품 목록", section = "7일 이전"),
            SideMenuItem("건강한 아침 식사", section = "7일 이전"),
        )
    }

    Column(modifier = modifier.padding(top = 8.dp)) {
        // Search
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF3F4F6),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "채팅 검색",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium,
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
            items(items) { item ->
                if (lastSection != item.section) {
                    lastSection = item.section
                    Text(
                        text = item.section,
                        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 6.dp),
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                val bg = if (item.selected) Color(0xFFDFF7ED) else Color.Transparent
                val iconTint = if (item.selected) BottomNavSelected else Color(0xFF94A3B8)
                val textColor = Color(0xFF111827)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chat",
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = item.title,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        // New chat button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .clickable { onNewChat() },
            color = BottomNavSelected,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "New chat",
                    tint = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "새 채팅",
                    color = Color.Black.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        // Footer links
        DrawerFooterRow(
            icon = Icons.Outlined.Settings,
            text = "Settings",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        DrawerFooterRow(
            icon = Icons.Outlined.HelpOutline,
            text = "Help & Feedback",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Spacer(modifier = Modifier.size(10.dp))
    }
}

@Composable
private fun DrawerFooterRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF111827),
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = text,
            color = Color(0xFF111827),
            style = MaterialTheme.typography.bodyLarge,
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
                    onValueChange = onInputChange,
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
                    onClick = { onSend(inputValue) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BottomNavSelected, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
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
    MyFrigeLocalTheme {
        ChatScreen()
    }
}

