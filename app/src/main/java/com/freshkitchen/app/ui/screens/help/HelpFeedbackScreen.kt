package com.freshkitchen.app.ui.screens.help

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshkitchen.app.ui.screens.chat.ChatDesign
import com.freshkitchen.app.ui.theme.BottomNavSelected
import com.freshkitchen.app.ui.theme.MyFrigeLocalTheme

private data class FaqEntry(
    val question: String,
    val answer: String,
)

private val HelpMintLight = Color(0xFFE8FAF2)
private val HelpMintMid = Color(0xFFDFF7ED)
@Composable
fun HelpFeedbackScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onInquiryListClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {},
    onReportIssueClick: () -> Unit = {},
) {
    BackHandler { onClose() }

    val faqEntries = remember {
        listOf(
            FaqEntry(
                question = "레시피 생성은 왜 시간이 걸리나요?",
                answer = "AI가 냉장고 재료와 선호 설정을 분석해 맞춤 레시피를 만들기 때문에 1~2분 정도 걸릴 수 있어요. 잠시만 기다려 주세요.",
            ),
            FaqEntry(
                question = "생성이 완료되면 어떻게 알 수 있나요?",
                answer = "완료되면 채팅 화면에 레시피 카드가 바로 표시돼요. 다른 화면에 있었다면 AI 채팅 탭으로 돌아와 확인해 주세요.",
            ),
            FaqEntry(
                question = "어떤 질문을 해야 정확한 추천을 받을 수 있나요?",
                answer = "가지고 있는 재료, 원하는 요리 종류, 인원 수, 알레르기 등을 함께 적어 주시면 더 정확한 추천을 받을 수 있어요.",
            ),
            FaqEntry(
                question = "이미지가 포함된 답변은 어떻게 제공되나요?",
                answer = "AI 설정에서 이미지 응답을 켜 두면 레시피·재료 관련 답변에 참고 이미지가 함께 제공될 수 있어요.",
            ),
        )
    }
    val expandedStates = remember {
        mutableStateListOf(false, false, false, false)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ChatDesign.ScreenBg),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            HelpTopBar(onClose = onClose)
        }

        item {
            HelpHeroBanner(
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            HelpSectionHeader(
                title = "자주 묻는 질문",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item {
            SettingCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                faqEntries.forEachIndexed { index, entry ->
                    FaqItem(
                        index = index + 1,
                        question = entry.question,
                        answer = entry.answer,
                        expanded = expandedStates[index],
                        onToggle = { expandedStates[index] = !expandedStates[index] },
                    )
                    if (index != faqEntries.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            color = ChatDesign.BorderSoft,
                        )
                    }
                }
            }
        }

        item {
            HelpSectionHeader(
                title = "고객 지원",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item {
            SupportHubCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                actions = listOf(
                    SupportAction(
                        icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                        iconBg = Color(0xFFE8F4FF),
                        iconTint = Color(0xFF3B82F6),
                        title = "내 문의 내역",
                        description = "접수한 문의·신고를 최신순으로 확인",
                        onClick = onInquiryListClick,
                    ),
                    SupportAction(
                        icon = Icons.Outlined.ChatBubble,
                        iconBg = HelpMintMid,
                        iconTint = BottomNavSelected,
                        title = "문의 보내기",
                        description = "궁금한 점을 남기면 답변해 드려요",
                        onClick = onContactSupportClick,
                    ),
                    SupportAction(
                        icon = Icons.Outlined.ReportProblem,
                        iconBg = Color(0xFFFDECEC),
                        iconTint = Color(0xFFE24A4A),
                        title = "문제 신고하기",
                        description = "오류·버그를 제보해 주세요",
                        onClick = onReportIssueClick,
                    ),
                ),
            )
        }
    }
}

@Composable
private fun HelpTopBar(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "도움말 및 피드백",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ChatDesign.TextPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        Surface(
            onClick = onClose,
            shape = CircleShape,
            color = ChatDesign.SurfaceWhite,
            border = BorderStroke(1.dp, ChatDesign.BorderSoft),
            shadowElevation = 1.dp,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "닫기",
                tint = ChatDesign.TextSecondary,
                modifier = Modifier.padding(10.dp).size(20.dp),
            )
        }
    }
}

@Composable
private fun HelpHeroBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            HelpMintLight,
                            HelpMintMid,
                            Color(0xFFF0FDF8),
                        ),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(ChatDesign.SurfaceWhite.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SupportAgent,
                        contentDescription = null,
                        tint = BottomNavSelected,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "무엇을 도와드릴까요?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        ),
                        color = ChatDesign.TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "AI 주방 비서 이용 중 궁금한 점이나\n피드백을 편하게 남겨주세요.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = Color(0xFF047857),
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BottomNavSelected),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = ChatDesign.TextPrimary,
        )
    }
}

@Composable
fun FaqItem(
    index: Int,
    question: String,
    answer: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "faqChevronRotation",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (expanded) BottomNavSelected else HelpMintMid,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (expanded) Color.White else Color(0xFF047857),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = question,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp,
                        ),
                        color = ChatDesign.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = "펼치기",
                        tint = if (expanded) BottomNavSelected else ChatDesign.TextMuted,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = rotation },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(start = 40.dp, top = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HelpMintLight, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = BottomNavSelected,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                            color = Color(0xFF374151),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private data class SupportAction(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

@Composable
private fun SupportHubCard(
    actions: List<SupportAction>,
    modifier: Modifier = Modifier,
) {
    SettingCard(modifier = modifier) {
        actions.forEachIndexed { index, action ->
            SupportActionRow(
                icon = action.icon,
                iconBg = action.iconBg,
                iconTint = action.iconTint,
                title = action.title,
                description = action.description,
                onClick = action.onClick,
            )
            if (index != actions.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = ChatDesign.BorderSoft,
                )
            }
        }
    }
}

@Composable
private fun SupportActionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(iconBg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ChatDesign.TextPrimary,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ChatDesign.TextSecondary,
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "이동",
            tint = ChatDesign.TextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun SettingCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ChatDesign.SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ChatDesign.BorderSoft),
    ) {
        Column {
            content()
        }
    }
}

/** @deprecated Prefer [SupportActionRow] inside [SupportHubCard]; kept for other screens if needed. */
@Composable
fun ActionCard(
    iconBg: Color,
    iconTint: Color,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingCard(modifier = modifier) {
        SupportActionRow(
            icon = icon,
            iconBg = iconBg,
            iconTint = iconTint,
            title = title,
            description = description,
            onClick = onClick,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HelpFeedbackScreenPreview() {
    MyFrigeLocalTheme {
        HelpFeedbackScreen(onClose = {})
    }
}
