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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 로그인 화면
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNewUser: () -> Unit = {},      // 처음 시작하기 → onboarding_setup
    onExistingUser: () -> Unit = {}, // 이미 계정 있어요 → home
    onBackClick: () -> Unit = {}     // 뒤로가기 → onboarding
) {
    // 바텀시트 상태
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var isNewUser by remember { mutableStateOf(false) }

    // 바텀시트
    if (showSheet) {
        SocialLoginBottomSheet(
            isNewUser = isNewUser,
            sheetState = sheetState,
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                }
            },
            onGoogleClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                    // TODO: 구글 OAuth 연동 — 로그인 API 응답의 accessToken을 받은 뒤:
                    //   AuthTokenStore.setAccessToken(accessToken)
                    //   (선택) EncryptedSharedPreferences / DataStore에 저장 후 앱 기동 시 복원.
                    // 임시 테스트 토큰은 소스에 하드코딩하지 말고, 디버그 빌드 전용 메뉴나 디버거로 setAccessToken 호출.
                    if (isNewUser) onNewUser() else onExistingUser()
                }
            },
            onKakaoClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                    // TODO: 카카오 OAuth 연동 — 성공 시 동일하게 AuthTokenStore.setAccessToken(accessToken) 호출.
                    if (isNewUser) onNewUser() else onExistingUser()
                }
            }
        )
    }

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
            Button(
                onClick = {
                    isNewUser = true
                    showSheet = true
                },
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
            OutlinedButton(
                onClick = {
                    isNewUser = false
                    showSheet = true
                },
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
// 소셜 로그인 바텀시트
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialLoginBottomSheet(
    isNewUser: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onGoogleClick: () -> Unit,
    onKakaoClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 타이틀
            Text(
                text = if (isNewUser) "어떤 계정으로 시작할까요?" else "어떤 계정으로 로그인할까요?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isNewUser) "계정으로 간편하게 가입할 수 있어요"
                else "기존에 가입한 계정으로 로그인하세요",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 구글 버튼
            OutlinedButton(
                onClick = onGoogleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF3C3C3C)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 구글 G 로고 (텍스트로 대체 — 실제 연동 시 이미지로 교체)
                    Text(
                        text = "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isNewUser) "Google로 시작하기" else "Google로 로그인",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 카카오 버튼
            Button(
                onClick = onKakaoClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE500),
                    contentColor = Color(0xFF191919)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 카카오 로고 (텍스트로 대체 — 실제 연동 시 이미지로 교체)
                    Text(
                        text = "K",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191919)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isNewUser) "카카오로 시작하기" else "카카오로 로그인",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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