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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfrigelocal.R
import com.example.myfrigelocal.ui.theme.FreshGreen
import com.example.myfrigelocal.ui.theme.FreshGreenDark
import com.example.myfrigelocal.viewmodel.LoginState
import com.example.myfrigelocal.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    // 바텀시트 상태
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var isNewUser by remember { mutableStateOf(false) }

    // ── Google Sign-In 설정 ──
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("481500254244-t5ram70598u3m967nmnv3a4o2j0toati.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            android.util.Log.d("LoginScreen", "account: $account, idToken: ${idToken?.take(20)}")
            if (idToken != null) {
                viewModel.loginWithGoogle(
                    idToken = idToken,
                    context = context,
                    displayName = account?.displayName,
                    email = account?.email,
                    photoUrl = account?.photoUrl?.toString()
                )
            } else {
                android.util.Log.e("LoginScreen", "idToken이 null — SHA-1 등록 확인 필요")
            }
        } catch (e: ApiException) {
            android.util.Log.e("LoginScreen", "ApiException: ${e.statusCode} - ${e.message}")
        }
    }

    // ── 로그인 상태 처리 ── (LaunchedEffect: 컴포지션 중 네비게이션 방지)
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val newUser = (loginState as LoginState.Success).isNewUser
            viewModel.resetState()
            if (newUser) onNewUser() else onExistingUser()
        }
    }

    // 바텀시트
    if (showSheet) {
        SocialLoginBottomSheet(
            isNewUser = isNewUser,
            sheetState = sheetState,
            isLoading = loginState is LoginState.Loading,
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                }
            },
            onGoogleClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                }
            },
            onKakaoClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showSheet = false
                    // 카카오 로그인 콜백
                    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                        if (error != null) {
                            android.util.Log.e("LoginScreen", "카카오 로그인 실패: ${error.message}")
                        } else if (token != null) {
                            val idToken = token.idToken
                            if (idToken != null) {
                                android.util.Log.d("LoginScreen", "카카오 로그인 성공: ${idToken.take(20)}")
                                // 카카오 프로필 조회 후 ViewModel에 전달
                                UserApiClient.instance.me { user, _ ->
                                    val nickname = user?.kakaoAccount?.profile?.nickname
                                    val profileImageUrl = user?.kakaoAccount?.profile?.thumbnailImageUrl
                                    viewModel.loginWithKakao(
                                        idToken = idToken,
                                        context = context,
                                        nickname = nickname,
                                        profileImageUrl = profileImageUrl
                                    )
                                }
                            } else {
                                android.util.Log.e("LoginScreen", "카카오 idToken null — Kakao 콘솔에서 openid 스코프 활성화 필요")
                            }
                        }
                    }
                    // 카카오톡 설치 여부에 따라 분기
                    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                            if (error != null) {
                                // 카카오톡 취소 시 카카오 계정으로 fallback
                                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) return@loginWithKakaoTalk
                                UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
                            } else if (token != null) {
                                kakaoCallback(token, null)
                            }
                        }
                    } else {
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
                    }
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
    isLoading: Boolean = false,
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
                enabled = !isLoading,
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