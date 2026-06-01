package com.freshkitchen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshkitchen.app.ui.theme.FreshGreenDark
import com.freshkitchen.app.ui.theme.LightGray
import com.freshkitchen.app.viewmodel.ManualAddViewModel
import com.freshkitchen.app.viewmodel.StorageType
import java.time.LocalDate

// ───────────────────────────────────────────
// 수동 식재료 추가 화면
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddScreen(
    onBackClick: () -> Unit = {},
    onAddSuccess: () -> Unit = {},
    viewModel: ManualAddViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var quickOffsetDays by remember { mutableIntStateOf(0) }

    // 에러 스낵바
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "식재료 추가",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── 식재료 이름 ──
            FormSection(title = "식재료 이름", required = true) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = { Text("예) 우유, 양파, 닭가슴살", color = Color.LightGray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenDark,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // ── 저장 공간 ──
            FormSection(title = "저장 공간", required = true) {
                ManualAddStorageSelector(
                    selected = uiState.selectedStorage,
                    onSelect = viewModel::onStorageChange
                )
            }

            // ── 유통기한 ──
            FormSection(title = "유통기한", required = false) {
                OutlinedTextField(
                    value = uiState.expiryDate,
                    onValueChange = { input ->
                        quickOffsetDays = 0
                        viewModel.onExpiryDateChange(input.filter { it.isDigit() }.take(8))
                    },
                    placeholder = { Text("YYYY-MM-DD", color = Color.LightGray) },
                    singleLine = true,
                    visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.Gray)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenDark,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                // ── 빠른 날짜 설정 칩 (+1, +3, +7, +10) + 초기화 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 7, 10).forEach { days ->
                        val active = quickOffsetDays > 0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    val newOffset = quickOffsetDays + days
                                    quickOffsetDays = newOffset
                                    val newDate = LocalDate.now().plusDays(newOffset.toLong()).toString()
                                    viewModel.onExpiryDateChange(newDate.filter { it.isDigit() })
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+${days}일",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (active) Color(0xFF16A34A) else Color(0xFF94A3B8),
                                )
                            }
                        }
                    }
                    // 초기화 버튼
                    val hasDate = uiState.expiryDate.isNotEmpty()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (hasDate) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable {
                                quickOffsetDays = 0
                                viewModel.onExpiryDateChange("")
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "초기화",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (hasDate) Color(0xFFB91C1C) else Color(0xFF94A3B8),
                            )
                        }
                    }
                }
                if (quickOffsetDays > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "오늘 기준 +${quickOffsetDays}일 · 누적",
                        fontSize = 12.sp,
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // ── 구입일 ──
            FormSection(title = "구입일", required = false) {
                OutlinedTextField(
                    value = uiState.purchaseDate,
                    onValueChange = { input ->
                        viewModel.onPurchaseDateChange(input.filter { it.isDigit() }.take(8))
                    },
                    placeholder = { Text("YYYY-MM-DD", color = Color.LightGray) },
                    singleLine = true,
                    visualTransformation = DateVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.Gray)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenDark,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // ── 메모 ──
            FormSection(title = "메모", required = false) {
                OutlinedTextField(
                    value = uiState.memo,
                    onValueChange = viewModel::onMemoChange,
                    placeholder = { Text("특이사항을 입력하세요", color = Color.LightGray) },
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenDark,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 추가하기 버튼 ──
            Button(
                onClick = { viewModel.submit(onAddSuccess) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FreshGreenDark,
                    disabledContainerColor = Color(0xFFB0B0B0)
                )
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "추가하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ───────────────────────────────────────────
// 저장 공간 선택 (냉장실 / 냉동실 / 팬트리)
// ───────────────────────────────────────────
@Composable
private fun ManualAddStorageSelector(
    selected: StorageType,
    onSelect: (StorageType) -> Unit,
) {
    val options = listOf(
        StorageType.FRIDGE  to "❄️ 냉장실",
        StorageType.FREEZER to "🧊 냉동실",
        StorageType.PANTRY  to "🥫 팬트리",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (type, label) ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) FreshGreenDark else LightGray)
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color.DarkGray
                )
            }
        }
    }
}

// ───────────────────────────────────────────
// 폼 섹션 레이아웃
// ───────────────────────────────────────────
@Composable
private fun FormSection(
    title: String,
    required: Boolean,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            if (required) {
                Text(
                    text = " *",
                    fontSize = 14.sp,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "  (선택)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        content()
    }
}

// ───────────────────────────────────────────
// 날짜 VisualTransformation: 숫자 8자리 입력 → YYYY-MM-DD 표시
// 실제 저장값은 숫자만, 커서 위치 버그 없음
// ───────────────────────────────────────────
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 4 || i == 6) append('-')
                append(c)
            }
        }

        val offsetMapping = object : OffsetMapping {
            // 원본(숫자) 커서 → 변환(YYYY-MM-DD) 커서
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 4 -> offset
                offset <= 6 -> offset + 1   // dash 1개 추가됨
                else        -> offset + 2   // dash 2개 추가됨
            }
            // 변환 커서 → 원본 커서
            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 4 -> offset
                offset <= 7 -> offset - 1
                else        -> offset - 2
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
