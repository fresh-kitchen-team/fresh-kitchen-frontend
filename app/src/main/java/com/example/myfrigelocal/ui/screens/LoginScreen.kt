package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfrigelocal.R
import com.example.myfrigelocal.ui.theme.FreshGreen
import com.example.myfrigelocal.ui.theme.FreshGreenDark

// ───────────────────────────────────────────
// 로그인 화면
// ───────────────────────────────────────────
@Composable
fun LoginScreen(
    onNewUser: () -> Unit = {},      // 처음 시작하기 → onboarding_setup
    onExistingUser: () -> Unit = {}, // 이미 계정 있어요 → home
    onBackClick: () -> Unit = {}     // 뒤로가기 → onboarding
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        // ── 뒤로가기 버튼 (좌측 상단) ──
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.Gray
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1.5f))

            // ── 앱 아이콘 ──
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mascot),
                    contentDescription = "앱 아이콘",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 앱 이름 ──
            Text(
                text = "Fresh\nKitchen",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FreshGreenDark,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── 서브타이틀 ──
            Text(
                text = "AI와 함께하는 똑똑한 식재료 라이프",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── 처음 시작하기 버튼 (신규 회원) ──
            // TODO: 백엔드 연동 시 카카오/구글 OAuth로 교체
            Button(
                onClick = onNewUser,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Text(
                    text = "처음 시작하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 이미 계정 있어요 버튼 (기존 회원) ──
            // TODO: 백엔드 연동 시 카카오/구글 OAuth로 교체
            OutlinedButton(
                onClick = onExistingUser,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FreshGreenDark
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = FreshGreenDark
                )
            ) {
                Text(
                    text = "이미 계정이 있어요",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreshGreenDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── 약관 안내 ──
            Text(
                text = "로그인 시 Fresh Kitchen의 이용약관 및 개인정보\n처리방침에 동의하게 됩니다.",
                fontSize = 12.sp,
                color = Color(0xFFBDBDBD),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}