package com.freshkitchen.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freshkitchen.app.ui.theme.FreshGreen
import com.freshkitchen.app.ui.theme.FreshGreenDark
import com.freshkitchen.app.viewmodel.OnboardingSetupViewModel

// ───────────────────────────────────────────
// 온보딩 설정 메인 스크린
// ───────────────────────────────────────────
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OnboardingSetupScreen(
    onFinish: () -> Unit = {},
    onBackToLogin: () -> Unit = {},   // 뒤로가기 → 로그인 화면
    viewModel: OnboardingSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // submitError 발생 시 스낵바 표시
    LaunchedEffect(state.submitError) {
        state.submitError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSubmitError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── 상단 헤더 (뒤로가기 + 건너뛰기) ──
            SetupHeader(
                currentStep = state.currentStep,
                onBack = {
                    if (state.currentStep == 0) onBackToLogin()  // Step 1 → 로그인으로
                    else viewModel.prevStep()                      // Step 2,3 → 이전 스텝으로
                },
                onSkip = onFinish,
                showBack = true  // 모든 스텝에서 뒤로가기 표시
            )

            // ── 진행도 표시 (스텝 인디케이터) ──
            SetupStepIndicator(
                totalSteps = 3,
                currentStep = state.currentStep,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // ── 각 스텝 콘텐츠 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (state.currentStep) {
                    0 -> AllergySetupStep(
                        selectedAllergies = state.selectedAllergies,
                        onToggle = { viewModel.toggleAllergy(it) }
                    )
                    1 -> FoodStyleSetupStep(
                        selectedStyles = state.selectedFoodStyles,
                        onToggle = { viewModel.toggleFoodStyle(it) }
                    )
                    2 -> QuickAddSetupStep(
                        selectedItems = state.selectedQuickItems,
                        onToggle = { viewModel.toggleQuickItem(it) }
                    )
                }
            }

            // ── 하단 버튼 ──
            SetupBottomButton(
                currentStep = state.currentStep,
                isLastStep = state.currentStep == 2,
                isSubmitting = state.isSubmitting,
                onNext = {
                    if (state.currentStep < 2) viewModel.nextStep()
                    else viewModel.submitProfile { onFinish() }
                },
                onSkip = onFinish
            )
        }
    }
}

// ───────────────────────────────────────────
// 상단 헤더
// ───────────────────────────────────────────
@Composable
fun SetupHeader(
    currentStep: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    showBack: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.Gray
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        TextButton(onClick = onSkip) {
            Text("건너뛰기", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

// ───────────────────────────────────────────
// 스텝 진행 인디케이터 (1/3, 2/3, 3/3)
// ───────────────────────────────────────────
@Composable
fun SetupStepIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(totalSteps) { index ->
                val color = if (index <= currentStep) FreshGreenDark else Color(0xFFE0E0E0)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${currentStep + 1} / $totalSteps",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

// ───────────────────────────────────────────
// Step 1: 알러지 정보 선택
// ───────────────────────────────────────────
@Composable
fun AllergySetupStep(
    selectedAllergies: Set<String>,
    onToggle: (String) -> Unit
) {
    val allergyItems = listOf(
        "🥛 유제품", "🥚 계란", "🌾 밀·글루텐", "🦐 갑각류",
        "🥜 땅콩", "🫘 콩·대두"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 타이틀
        Text(
            text = "알러지가 있으신가요?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "선택한 알러지 재료가 포함된 경우\nAI가 미리 알려드릴게요",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 알러지 칩 그리드 (2열)
        val chunked = allergyItems.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    SelectableChip(
                        label = item,
                        isSelected = item in selectedAllergies,
                        onClick = { onToggle(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 홀수일 경우 빈 공간 채우기
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 선택 없음 안내
        if (selectedAllergies.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "알러지가 없으면 선택 없이 다음으로!",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FreshGreenDark.copy(alpha = 0.08f))
                    .border(1.dp, FreshGreenDark.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "${selectedAllergies.size}개 선택됨",
                    fontSize = 13.sp,
                    color = FreshGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ───────────────────────────────────────────
// Step 2: 선호 음식 스타일 선택
// ───────────────────────────────────────────
@Composable
fun FoodStyleSetupStep(
    selectedStyles: Set<String>,
    onToggle: (String) -> Unit
) {
    val styleItems = listOf(
        "🍚 한식", "🍣 일식", "🥢 중식", "🍝 양식",
        "🥗 채식·비건", "🍲 담백한 것"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "선호하는 음식 스타일은?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "선택한 스타일에 맞는 레시피를\nAI가 추천해드릴게요 (중복 선택 가능)",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 음식 스타일 칩 그리드 (2열)
        val chunked = styleItems.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    SelectableChip(
                        label = item,
                        isSelected = item in selectedStyles,
                        onClick = { onToggle(item) },
                        modifier = Modifier.weight(1f),
                        selectedColor = Color(0xFFF97316)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ───────────────────────────────────────────
// Step 3: 식재료 간편 등록
// ───────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickAddSetupStep(
    selectedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    // 카테고리별 식재료 목록
    val categories = listOf(
        "유제품·계란" to listOf("🥛 우유", "🥚 계란", "🧀 치즈", "🫙 요거트"),
        "채소" to listOf("🥕 당근", "🧅 양파", "🥦 브로콜리", "🍅 토마토", "🥬 배추", "🧄 마늘"),
        "고기·해산물" to listOf("🍗 닭고기", "🥩 소고기", "🐷 돼지고기", "🐟 생선", "🦐 새우"),
        "과일" to listOf("🍎 사과", "🍌 바나나", "🍊 오렌지", "🍇 포도"),
        "가공식품·기타" to listOf("🥬 김치", "🫘 두부", "🐟 참치캔", "🍜 라면", "🥜 땅콩버터")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "지금 냉장고에 뭐가 있나요?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "자주 쓰는 식재료를 선택하면\n바로 냉장고에 등록돼요!",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 22.sp
        )

        if (selectedItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF97316).copy(alpha = 0.08f))
                    .border(1.dp, Color(0xFFF97316).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "${selectedItems.size}개 선택됨",
                    fontSize = 13.sp,
                    color = Color(0xFFF97316),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 카테고리별 렌더링
        categories.forEach { (category, items) ->
            Text(
                text = category,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 줄 바꿈 칩 레이아웃
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { item ->
                    SelectableChip(
                        label = item,
                        isSelected = item in selectedItems,
                        onClick = { onToggle(item) },
                        selectedColor = Color(0xFFF97316)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ───────────────────────────────────────────
// 선택 가능한 칩 컴포넌트
// ───────────────────────────────────────────
@Composable
fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = FreshGreenDark
) {
    val backgroundColor = if (isSelected) selectedColor.copy(alpha = 0.12f) else Color(0xFFF5F5F5)
    val borderColor = if (isSelected) selectedColor else Color.Transparent
    val textColor = if (isSelected) selectedColor else Color.DarkGray

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

// ───────────────────────────────────────────
// 하단 버튼 영역
// ───────────────────────────────────────────
@Composable
fun SetupBottomButton(
    currentStep: Int,
    isLastStep: Boolean,
    isSubmitting: Boolean = false,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onNext,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastStep) Color(0xFFF97316) else FreshGreen
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = when (currentStep) {
                        0 -> "다음  ›"
                        1 -> "다음  ›"
                        else -> "완료  ✓"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingSetupScreenPreview() {
    OnboardingSetupScreen()
}