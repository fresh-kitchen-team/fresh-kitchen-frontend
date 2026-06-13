package com.freshkitchen.app.ui.screens.help

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshkitchen.app.ui.screens.chat.ChatDesign
import com.freshkitchen.app.ui.theme.BottomNavSelected
import com.freshkitchen.app.ui.theme.MyFrigeLocalTheme
import com.freshkitchen.app.viewmodel.InquiryDetailViewModel

@Composable
fun InquiryDetailScreen(
    inquiryId: Long,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InquiryDetailViewModel = viewModel(),
) {
    BackHandler { onClose() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(inquiryId) {
        viewModel.load(inquiryId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChatDesign.ScreenBg),
    ) {
        InquiryDetailTopBar(onBack = onClose)

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = ChatDesign.ChatPrimary,
                        strokeWidth = 2.5.dp,
                    )
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ChatDesign.ErrorText,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.load(inquiryId) }) {
                        Text(
                            text = "다시 시도",
                            color = ChatDesign.ChatPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            uiState.detail != null -> {
                InquiryDetailContent(detail = uiState.detail!!)
            }
        }
    }
}

@Composable
private fun InquiryDetailTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChatDesign.SurfaceWhite,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "뒤로",
                        tint = ChatDesign.TextPrimary,
                    )
                }
                Text(
                    text = "문의 상세",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChatDesign.TextPrimary,
                )
            }
            androidx.compose.material3.HorizontalDivider(thickness = 1.dp, color = ChatDesign.BorderSoft)
        }
    }
}

@Composable
private fun InquiryDetailContent(
    detail: InquiryDetailUi,
    modifier: Modifier = Modifier,
) {
    val typeBg = if (detail.isReport) Color(0xFFFDECEC) else Color(0xFFDFF7ED)
    val typeTint = if (detail.isReport) Color(0xFFE24A4A) else BottomNavSelected
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "status_banner") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8FAF2)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(22.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.statusLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF059669),
                        )
                        detail.answeredAtLabel?.let { answeredAt ->
                            Text(
                                text = "답변일 · $answeredAt",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF047857),
                            )
                        }
                    }
                }
            }
        }

        item(key = "my_inquiry") {
            DetailSectionCard(
                icon = Icons.Outlined.Person,
                iconBg = Color(0xFFF3F4F6),
                iconTint = ChatDesign.TextSecondary,
                title = "내 문의",
                subtitle = detail.createdAtLabel,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InquiryDetailChip(text = detail.typeLabel, background = typeBg, textColor = typeTint)
                    InquiryDetailChip(
                        text = detail.categoryLabel,
                        background = Color(0xFFF6F8F7),
                        textColor = ChatDesign.TextSecondary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = detail.content.ifBlank { "(내용 없음)" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f,
                    ),
                    color = ChatDesign.TextPrimary,
                )
                detail.imageUrl?.let { url ->
                    Spacer(modifier = Modifier.height(14.dp))
                    InquiryAttachmentImage(
                        imageUrl = url,
                        onClick = { fullscreenImageUrl = url },
                    )
                }
            }
        }

        item(key = "connector") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(ChatDesign.BorderSoft),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "관리자 답변",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = ChatDesign.TextMuted,
                )
            }
        }

        item(key = "admin_reply") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ChatDesign.SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFBBF7D0)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(ChatDesign.ChatPrimary),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFDFF7ED), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SupportAgent,
                                    contentDescription = null,
                                    tint = BottomNavSelected,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = "Fresh Kitchen 상담",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = ChatDesign.TextPrimary,
                                )
                                detail.answeredAtLabel?.let { at ->
                                    Text(
                                        text = at,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ChatDesign.TextMuted,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = ChatDesign.BorderSoft)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = detail.adminReply.ifBlank {
                                "답변 내용을 불러올 수 없습니다."
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f,
                            ),
                            color = ChatDesign.TextPrimary,
                        )
                    }
                }
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    fullscreenImageUrl?.let { url ->
        InquiryFullscreenImageDialog(
            imageUrl = url,
            onDismiss = { fullscreenImageUrl = null },
        )
    }
}

@Composable
private fun DetailSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ChatDesign.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ChatDesign.TextPrimary,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = ChatDesign.TextMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ChatDesign.BorderSoft)
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun InquiryDetailChip(
    text: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = textColor,
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun InquiryDetailContentPreview() {
    MyFrigeLocalTheme {
        InquiryDetailContent(
            detail = InquiryDetailUi(
                id = 1L,
                typeLabel = "문의",
                categoryLabel = "레시피",
                content = "레시피 추천이 잘못된 것 같습니다.3333",
                imageUrl = null,
                statusLabel = "답변 완료",
                isAnswered = true,
                isReport = false,
                adminReply = "알겠습니다.",
                createdAtLabel = "5월 25일",
                answeredAtLabel = "5월 25일 20:26",
            ),
        )
    }
}
