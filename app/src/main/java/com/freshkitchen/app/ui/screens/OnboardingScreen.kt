package com.freshkitchen.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.freshkitchen.app.R
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 온보딩 페이지 데이터
// ───────────────────────────────────────────
data class OnboardingPage(
    val drawableRes: Int,
    val iconBackground: Color,
    val title: String,
    val subtitle: String,
    val description: String
)

// ───────────────────────────────────────────
// 온보딩 스크린 (하단바 없음)
// ───────────────────────────────────────────
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit = {}
) {
    val pages = listOf(
        OnboardingPage(
            drawableRes = R.drawable.ic_mascot,
            iconBackground = Color(0xFFE8F5E9),
            title = "모든 식재료를 알뜰하게,",
            subtitle = "소비는 간결하게",
            description = "주방 인벤토리로 식재료를 체계적으로 관리하고\n음식물 쓰레기를 줄여보세요"
        ),
        OnboardingPage(
            drawableRes = R.drawable.ic_bell,
            iconBackground = Color(0xFFF97316),
            title = "유통기한 임박 알림으로",
            subtitle = "낭비 없는 주방 생활",
            description = "소비 임박 식재료를 미리 알려드려\n버리는 음식 없이 알뜰하게 사용하세요"
        ),
        OnboardingPage(
            drawableRes = R.drawable.ic_mascot_wink,
            iconBackground = Color(0xFFE8F5E9),
            title = "사진 한 장으로 끝내는",
            subtitle = "초간단 냉장고 등록",
            description = "촬영, 갤러리, 바코드 스캔으로 빠르게\n식재료를 등록하세요"
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    fun navigateToPage(targetPage: Int) {
        coroutineScope.launch {
            launch { offsetX.animateTo(-300f, tween(250, easing = EaseIn)) }
            alpha.animateTo(0f, tween(200, easing = EaseIn))
            currentPage = targetPage
            offsetX.snapTo(300f)
            launch { offsetX.animateTo(0f, tween(300, easing = EaseOut)) }
            alpha.animateTo(1f, tween(300, easing = EaseOut))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 건너뛰기 버튼
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("건너뛰기", color = Color.Gray, fontSize = 14.sp)
        }

        // 페이지 콘텐츠 (슬라이드 + 페이드 애니메이션)
        OnboardingPageContent(
            page = pages[currentPage],
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    this.alpha = alpha.value
                }
        )

        // 하단 (인디케이터 + 버튼)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(
                totalPages = pages.size,
                currentPage = currentPage
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        navigateToPage(currentPage + 1)
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Text(
                    text = if (currentPage < pages.size - 1) "다음  ›" else "시작하기  ✓",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// ───────────────────────────────────────────
// 페이지 콘텐츠
// ───────────────────────────────────────────
@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    // 아이콘 튀어오르기 애니메이션
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    LaunchedEffect(page) {
        visible = false
        kotlinx.coroutines.delay(150)
        visible = true
    }

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // 아이콘 (튀어오르기 효과)
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(page.iconBackground),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = page.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = page.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        Text(
            text = page.subtitle,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = FreshGreenDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(200.dp))
    }
}

// ───────────────────────────────────────────
// 페이지 인디케이터
// ───────────────────────────────────────────
@Composable
fun PageIndicator(totalPages: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalPages) { index ->
            val width by animateDpAsState(
                targetValue = if (index == currentPage) 24.dp else 8.dp,
                animationSpec = tween(300),
                label = "dot_width"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) FreshGreenDark
                        else Color(0xFFE0E0E0)
                    )
            )
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen()
}