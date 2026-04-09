package com.example.myfrigelocal.ui.screens.help

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme

@Composable
fun HelpFeedbackScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onContactSupportClick: () -> Unit = {},
    onReportIssueClick: () -> Unit = {},
) {
    BackHandler { onClose() }

    var receiveCompletedNotification by rememberSaveable { mutableStateOf(true) }

    val faqQuestions = remember {
        listOf(
            "레시피 생성은 왜 시간이 걸리나요?",
            "생성이 완료되면 어떻게 알 수 있나요?",
            "어떤 질문을 해야 정확한 추천을 받을 수 있나요?",
            "이미지가 포함된 답변은 어떻게 제공되나요?",
        )
    }
    val expandedStates = remember {
        mutableStateListOf(false, false, false, false)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TopBar(
                title = "도움말 및 피드백",
                onClose = onClose,
            )
        }

        item {
            Text(
                text = "자주 묻는 질문",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        item {
            SettingCard {
                faqQuestions.forEachIndexed { index, question ->
                    FaqItem(
                        question = question,
                        expanded = expandedStates[index],
                        onToggle = { expandedStates[index] = !expandedStates[index] },
                    )
                    if (index != faqQuestions.lastIndex) {
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                    }
                }
            }
        }

        item {
            SettingCard {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                    Text(
                        text = "AI가 레시피를 생성하는 동안 시간이 소요\n될 수 있습니다.\n생성이 완료되면 알림으로 안내됩니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF111827),
                    )

                    Spacer(modifier = Modifier.size(14.dp))

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "생성 완료 알림 받기",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF111827),
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = receiveCompletedNotification,
                                onCheckedChange = { receiveCompletedNotification = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2EEA92),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFD1D5DB),
                                ),
                            )
                        }
                    }
                }
            }
        }

        item {
            ActionCard(
                iconBg = Color(0xFFDFF7ED),
                iconTint = BottomNavSelected,
                icon = Icons.Outlined.ChatBubble,
                title = "문의 보내기",
                description = "전문 상담원이 답변해 드립니다",
                onClick = onContactSupportClick,
            )
        }

        item {
            ActionCard(
                iconBg = Color(0xFFFDECEC),
                iconTint = Color(0xFFE24A4A),
                icon = Icons.Outlined.ReportProblem,
                title = "문제 신고하기",
                description = "오류나 버그를 제보해주세요",
                onClick = onReportIssueClick,
            )
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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
                tint = BottomNavSelected,
            )
        }
    }
}

@Composable
fun FaqItem(
    question: String,
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
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF111827),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = "Expand",
                tint = Color(0xFF6B7280),
                modifier = Modifier
                    .size(22.dp)
                    .padding(start = 8.dp)
                    .graphicsLayer { rotationZ = rotation },
            )
        }

        AnimatedVisibility(visible = expanded) {
            Text(
                text = "내용을 여기에 추가하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
fun SettingCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun ActionCard(
    iconBg: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827),
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Go",
                tint = Color(0xFF9CA3AF),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun HelpFeedbackScreenPreview() {
    MyFrigeLocalTheme {
        HelpFeedbackScreen(onClose = {})
    }
}

