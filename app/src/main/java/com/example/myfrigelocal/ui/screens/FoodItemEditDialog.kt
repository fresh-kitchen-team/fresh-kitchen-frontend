package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

// ───────────────────────────────────────────
// 식재료 수정 팝업
// ───────────────────────────────────────────
@Composable
fun FoodItemEditDialog(
    item: FoodItem,
    onDismiss: () -> Unit = {},
    onSave: (FoodItem) -> Unit = {}
) {
    // 편집 상태 (item 값으로 초기화)
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var amount by remember { mutableStateOf(item.amount) }
    var expiryDate by remember { mutableStateOf(item.expiryDate) }
    var purchaseDate by remember { mutableStateOf(item.purchaseDate) }
    var memo by remember { mutableStateOf(item.memo) }
    var storage by remember { mutableStateOf(item.storage) }
    var status by remember { mutableStateOf(item.status) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("식재료 수정", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 이름
                EditField(label = "식재료 이름") {
                    EditTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "예) 우유"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 카테고리 + 수량
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditField(label = "카테고리", modifier = Modifier.weight(1f)) {
                        EditTextField(
                            value = category,
                            onValueChange = { category = it },
                            placeholder = "예) 유제품"
                        )
                    }
                    EditField(label = "수량", modifier = Modifier.weight(1f)) {
                        EditTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            placeholder = "예) 1L"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 유통기한 + 구매일
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditField(label = "유통기한", modifier = Modifier.weight(1f)) {
                        EditTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            placeholder = "YYYY-MM-DD"
                        )
                    }
                    EditField(label = "구매일 (선택)", modifier = Modifier.weight(1f)) {
                        EditTextField(
                            value = purchaseDate,
                            onValueChange = { purchaseDate = it },
                            placeholder = "YYYY-MM-DD"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 보관 위치 선택
                EditField(label = "보관 위치") {
                    StorageSelector(
                        selected = storage,
                        onSelect = { storage = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 상태 선택
                EditField(label = "상태") {
                    StatusSelector(
                        selected = status,
                        onSelect = { status = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 메모
                EditField(label = "메모 (선택)") {
                    EditTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        placeholder = "메모를 입력하세요",
                        singleLine = false,
                        minLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("취소", fontSize = 15.sp)
                    }
                    Button(
                        onClick = {
                            onSave(
                                item.copy(
                                    name = name.trim(),
                                    category = category.trim(),
                                    amount = amount.trim(),
                                    expiryDate = expiryDate.trim(),
                                    purchaseDate = purchaseDate.trim(),
                                    memo = memo.trim(),
                                    storage = storage,
                                    status = status
                                )
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF97316)
                        ),
                        enabled = name.isNotBlank() && expiryDate.isNotBlank()
                    ) {
                        Text("저장하기", fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ───────────────────────────────────────────
// 라벨 + 입력 필드
// ───────────────────────────────────────────
@Composable
fun EditField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        content()
    }
}

// ───────────────────────────────────────────
// 텍스트 입력 필드
// ───────────────────────────────────────────
@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.LightGray) },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FreshGreenDark,
            unfocusedBorderColor = Color(0xFFE0E0E0)
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}

// ───────────────────────────────────────────
// 보관 위치 선택 (칩 형태)
// ───────────────────────────────────────────
@Composable
fun StorageSelector(
    selected: StorageType,
    onSelect: (StorageType) -> Unit
) {
    val options = listOf(
        StorageType.FRIDGE to "냉장실",
        StorageType.FREEZER to "냉동실",
        StorageType.PANTRY to "팬트리"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (type, label) ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) FreshGreenDark else LightGray)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) FreshGreenDark else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(type) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else Color.DarkGray,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ───────────────────────────────────────────
// 상태 선택 (칩 형태)
// ───────────────────────────────────────────
@Composable
fun StatusSelector(
    selected: FoodStatus,
    onSelect: (FoodStatus) -> Unit
) {
    val options = listOf(
        FoodStatus.FRESH to ("신선" to StatusFreshColor),
        FoodStatus.NEAR_EXPIRY to ("소비임박" to StatusNearExpiryColor),
        FoodStatus.EXPIRED to ("유통기한경과" to StatusExpiredColor)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (statusType, info) ->
            val (label, color) = info
            val isSelected = selected == statusType
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) color.copy(alpha = 0.15f) else LightGray)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) color else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(statusType) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = if (isSelected) color else Color.DarkGray,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun FoodItemEditDialogPreview() {
    FoodItemEditDialog(
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
            memo = "신선도 유지를 위해 냉장 보관 필요."
        )
    )
}