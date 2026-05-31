package com.freshkitchen.app.ui.screens.help

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.freshkitchen.app.ui.screens.chat.ChatDesign
import com.freshkitchen.app.ui.theme.BottomNavSelected
import com.freshkitchen.app.ui.theme.MyFrigeLocalTheme

private enum class SupportType { Recipe, Ai, Other }

@Composable
fun ContactSupportScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onDismissError: () -> Unit = {},
    onSubmit: (type: String, message: String, imageUri: String?) -> Unit = { _, _, _ -> },
) {
    SupportFormScreen(
        title = "문의 보내기",
        ctaText = "문의 보내기",
        ctaColor = BottomNavSelected,
        placeholder = "예: 영양 균형 기반 추천은 어떤 식으로 추천하는건가요?",
        bodyLabel = "문의 내용",
        onClose = onClose,
        modifier = modifier,
        isSubmitting = isSubmitting,
        errorMessage = errorMessage,
        successMessage = successMessage,
        onDismissError = onDismissError,
        onSubmit = onSubmit,
    )
}

@Composable
fun ReportIssueScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onDismissError: () -> Unit = {},
    onSubmit: (type: String, message: String, imageUri: String?) -> Unit = { _, _, _ -> },
) {
    SupportFormScreen(
        title = "문제 신고하기",
        ctaText = "신고 보내기",
        ctaColor = Color(0xFFF79A86),
        placeholder = "예: 레시피가 잘못 추천됩니다. 토마토가\n없는데 포함돼요.",
        bodyLabel = "신고 내용",
        onClose = onClose,
        modifier = modifier,
        isSubmitting = isSubmitting,
        errorMessage = errorMessage,
        successMessage = successMessage,
        onDismissError = onDismissError,
        onSubmit = onSubmit,
    )
}

@Composable
private fun SupportFormScreen(
    title: String,
    ctaText: String,
    ctaColor: Color,
    placeholder: String,
    bodyLabel: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onDismissError: () -> Unit = {},
    onSubmit: (type: String, message: String, imageUri: String?) -> Unit,
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    var type by rememberSaveable { mutableStateOf(SupportType.Recipe) }
    var message by rememberSaveable { mutableStateOf("") }
    var attachedImageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        attachedImageUri = uri?.toString()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ChatDesign.ScreenBg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = ChatDesign.TextPrimary,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "닫기",
                        tint = ChatDesign.TextSecondary,
                    )
                }
            }
        }

        item {
            FormCard {
                Text(
                    text = "문의 유형",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827),
                )
                Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TypeChip(
                        text = "레시피 관련",
                        selected = type == SupportType.Recipe,
                        onClick = { type = SupportType.Recipe },
                        selectedColor = BottomNavSelected,
                    )
                    TypeChip(
                        text = "AI 관련",
                        selected = type == SupportType.Ai,
                        onClick = { type = SupportType.Ai },
                        selectedColor = BottomNavSelected,
                    )
                    TypeChip(
                        text = "기타",
                        selected = type == SupportType.Other,
                        onClick = { type = SupportType.Other },
                        selectedColor = BottomNavSelected,
                    )
                }
            }
        }

        item {
            FormCard {
                Text(
                    text = bodyLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827),
                )
                Spacer(modifier = Modifier.size(12.dp))
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }

        item {
            FormCard {
                val previewUri = attachedImageUri?.let { Uri.parse(it) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = Color(0xFFD1D5DB),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clickable(
                            enabled = !isSubmitting && successMessage == null,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (previewUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(previewUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "첨부 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        IconButton(
                            onClick = { attachedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "이미지 제거",
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Add image",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(30.dp),
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "이미지 추가",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "문제 상황을 더 정확히 전달할 수 있습니다. (선택)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }

        successMessage?.let { ok ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8FAF2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB8F0D4)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = ok,
                            color = Color(0xFF059669),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }
        }

        errorMessage?.let { err ->
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismissError() },
                    shape = RoundedCornerShape(16.dp),
                    color = ChatDesign.ErrorBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.ErrorBorder),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
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
                            color = ChatDesign.ErrorText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.size(6.dp))
            val canSubmit = !isSubmitting && successMessage == null && message.trim().isNotEmpty()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(
                        enabled = canSubmit,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (!canSubmit) return@clickable
                        onSubmit(
                            when (type) {
                                SupportType.Recipe -> "레시피 관련"
                                SupportType.Ai -> "AI 관련"
                                SupportType.Other -> "기타"
                            },
                            message,
                            attachedImageUri,
                        )
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ctaColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isSubmitting) "전송 중..." else ctaText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827).copy(alpha = if (canSubmit) 1f else 0.45f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ChatDesign.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            content()
        }
    }
}

@Composable
private fun TypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) selectedColor else Color(0xFFE5E7EB)
    val fg = if (selected) Color(0xFF111827) else Color(0xFF6B7280)

    Box(
        modifier = modifier
            .height(38.dp)
            .widthIn(min = 72.dp)
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ContactSupportPreview() {
    MyFrigeLocalTheme {
        ContactSupportScreen(onClose = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ReportIssuePreview() {
    MyFrigeLocalTheme {
        ReportIssueScreen(onClose = {})
    }
}

