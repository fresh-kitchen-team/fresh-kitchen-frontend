package com.example.myfrigelocal.ui.screens.help

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme

private enum class SupportType { Recipe, Ai, Other }

@Composable
fun ContactSupportScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onSubmit: (type: String, message: String) -> Unit = { _, _ -> },
) {
    SupportFormScreen(
        title = "문의 보내기",
        ctaText = "문의 보내기",
        ctaColor = BottomNavSelected,
        placeholder = "예: 영양 균형 기반 추천은 어떤 식으로 추천하는건가요?",
        bodyLabel = "문의 내용",
        onClose = onClose,
        modifier = modifier,
        onSubmit = onSubmit,
    )
}

@Composable
fun ReportIssueScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onSubmit: (type: String, message: String) -> Unit = { _, _ -> },
) {
    SupportFormScreen(
        title = "문제 신고하기",
        ctaText = "신고 보내기",
        ctaColor = Color(0xFFF79A86),
        placeholder = "예: 레시피가 잘못 추천됩니다. 토마토가\n없는데 포함돼요.",
        bodyLabel = "신고 내용",
        onClose = onClose,
        modifier = modifier,
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
    onSubmit: (type: String, message: String) -> Unit,
) {
    BackHandler { onClose() }

    var type by rememberSaveable { mutableStateOf(SupportType.Recipe) }
    var message by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFD1D5DB),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
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
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "문제 상황을 더 정확히 전달할 수 있습니다.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.size(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        onSubmit(
                            when (type) {
                                SupportType.Recipe -> "레시피 관련"
                                SupportType.Ai -> "AI 관련"
                                SupportType.Other -> "기타"
                            },
                            message,
                        )
                        onClose()
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ctaColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ctaText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827),
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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

