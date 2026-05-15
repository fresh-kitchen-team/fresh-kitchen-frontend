package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myfrigelocal.ui.theme.FreshGreenDark
import com.example.myfrigelocal.ui.theme.LightGray
import com.example.myfrigelocal.viewmodel.FoodItem
import com.example.myfrigelocal.viewmodel.FoodStatus
import com.example.myfrigelocal.viewmodel.StorageType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ───────────────────────────────────────────
// 식재료 상세 정보 팝업
// ───────────────────────────────────────────
@Composable
fun FoodItemDetailDialog(
    item: FoodItem,
    onDismiss: () -> Unit = {},
    onEdit: (FoodItem) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // 헤더
                DetailHeader(onDismiss = onDismiss)

                Spacer(modifier = Modifier.height(20.dp))

                // 식재료 이름 + 상태 뱃지
                DetailTitleSection(item = item)

                Spacer(modifier = Modifier.height(16.dp))

                // 보관위치 + 카테고리
                DetailInfoCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DetailField(
                            label = "보관 위치",
                            modifier = Modifier.weight(1f)
                        ) {
                            StorageBadge(
                                text = when (item.storage) {
                                    StorageType.FRIDGE -> "냉장실"
                                    StorageType.FREEZER -> "냉동실"
                                    StorageType.PANTRY -> "팬트리"
                                    StorageType.ALL -> ""
                                }
                            )
                        }
                        DetailField(
                            label = "카테고리",
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(item.category, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 유통기한
                DetailInfoCard {
                    DetailField(label = "유통기한") {
                        Text(item.expiryDate, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // 구매일 (있을 때만 표시)
                if (item.purchaseDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailInfoCard {
                        DetailField(label = "구매일") {
                            Text(item.purchaseDate, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // 메모 (있을 때만 표시)
                if (item.memo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailInfoCard {
                        DetailField(label = "메모") {
                            Text(
                                text = item.memo,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // 유통기한 남은 기간 경고 (임박/경과일 때만)
                if (item.status == FoodStatus.NEAR_EXPIRY || item.status == FoodStatus.EXPIRED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExpiryWarningCard(expiryDate = item.expiryDate, status = item.status)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("닫기", fontSize = 15.sp)
                    }
                    Button(
                        onClick = { onEdit(item) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF97316)
                        )
                    ) {
                        Text("수정하기", fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 헤더 (제목 + X 버튼)
// ───────────────────────────────────────────
@Composable
fun DetailHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "식재료 상세 정보",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.Gray)
        }
    }
}

// ───────────────────────────────────────────
// 이름 + 상태 뱃지 섹션
// ───────────────────────────────────────────
@Composable
fun DetailTitleSection(item: FoodItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 이모지 아이콘
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, fontSize = 32.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // 상태 뱃지
            val (statusText, statusTextColor, statusBgColor) = when (item.status) {
                FoodStatus.FRESH -> Triple("신선", StatusFreshColor, StatusFreshBgColor)
                FoodStatus.NEAR_EXPIRY -> Triple("소비임박", StatusNearExpiryColor, StatusNearExpiryBgColor)
                FoodStatus.EXPIRED -> Triple("유통기한경과", StatusExpiredColor, StatusExpiredBgColor)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBgColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(statusText, color = statusTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ───────────────────────────────────────────
// 정보 카드 (회색 배경 박스)
// ───────────────────────────────────────────
@Composable
fun DetailInfoCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightGray)
            .padding(16.dp)
    ) {
        content()
    }
}

// ───────────────────────────────────────────
// 라벨 + 값 필드
// ───────────────────────────────────────────
@Composable
fun DetailField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        content()
    }
}

// ───────────────────────────────────────────
// 유통기한 남은 기간 경고 카드
// ───────────────────────────────────────────
@Composable
fun ExpiryWarningCard(expiryDate: String, status: FoodStatus) {
    val daysLeft = try {
        val expiry = LocalDate.parse(expiryDate)
        ChronoUnit.DAYS.between(LocalDate.now(), expiry)
    } catch (e: Exception) {
        0L
    }

    val bgColor = Color(0xFFFEF2F2)
    val textColor = Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "유통기한까지 남은 기간",
                    fontSize = 12.sp,
                    color = textColor
                )
            }
            Text(
                text = when {
                    daysLeft < 0 -> "${-daysLeft}일 경과"
                    daysLeft == 0L -> "오늘 만료"
                    else -> "${daysLeft}일"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun FoodItemDetailDialogPreview() {
    FoodItemDetailDialog(
        item = FoodItem(
            id = 4,
            name = "계란",
            category = "유제품",
            storage = StorageType.FRIDGE,
            amount = "10개",
            expiryDate = "2026-03-24",
            status = FoodStatus.NEAR_EXPIRY,
            emoji = "🥚",
            purchaseDate = "2026-03-10",
            memo = "신선도 유지를 위해 냉장 보관 필요.\n1일 안에 소비 예정."
        )
    )
}