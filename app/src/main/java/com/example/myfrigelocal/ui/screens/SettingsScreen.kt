package com.example.myfrigelocal.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfrigelocal.ui.theme.FreshGreen
import com.example.myfrigelocal.ui.theme.FreshGreenDark
import com.example.myfrigelocal.viewmodel.SettingsViewModel

// ───────────────────────────────────────────
// 설정 화면
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 탈퇴 확인 다이얼로그
    var showWithdrawDialog by remember { mutableStateOf(false) }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("탈퇴하기", fontWeight = FontWeight.Bold) },
            text = { Text("탈퇴하면 모든 데이터가 삭제되며\n복구할 수 없어요. 정말 탈퇴할까요?") },
            confirmButton = {
                TextButton(onClick = {
                    showWithdrawDialog = false
                    // TODO: 탈퇴 API 호출
                }) {
                    Text("탈퇴", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("취소", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── 알림 설정 ──
            SettingsSectionTitle("알림 설정")
            SettingsCard {
                // 유통기한 알림
                SettingsToggleRow(
                    title = "유통기한 알림",
                    description = "식재료의 유통기한이 임박했을 때 알림을 보냅니다.",
                    checked = uiState.expiryAlarmEnabled,
                    onCheckedChange = { viewModel.toggleExpiryAlarm(it) }
                )
                SettingsDivider()

                // 사진 등록 알림
                SettingsToggleRow(
                    title = "사진 등록 알림",
                    description = "새로운 식재료 사진이 감지되면 등록 여부를 물어봅니다.",
                    checked = uiState.photoAlarmEnabled,
                    onCheckedChange = { viewModel.togglePhotoAlarm(it) }
                )
                SettingsDivider()

                // 푸시 알림 권한
                SettingsButtonRow(
                    title = "푸시 알림 권한",
                    description = "시스템 알림 허용 상태를 관리합니다.",
                    buttonText = "설정 이동",
                    onButtonClick = {
                        // 시스템 알림 설정으로 이동
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 계정 ──
            SettingsSectionTitle("계정")
            SettingsCard {
                // 로그인 계정 정보
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FreshGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            // TODO: 백엔드 연동 시 실제 계정 정보로 교체
                            "Google 계정으로 로그인됨",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "chef.alchemist@gmail.com",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                SettingsDivider()

                // 로그아웃
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.logout(context) { onLogout() } }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("로그아웃", fontSize = 15.sp)
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                SettingsDivider()

                // 탈퇴하기
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWithdrawDialog = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("탈퇴하기", fontSize = 15.sp, color = Color(0xFFEF4444))
                    Icon(
                        Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 앱 정보 ──
            SettingsSectionTitle("앱 정보")
            SettingsCard {
                SettingsNavigationRow(title = "버전 정보", value = "v1.0.0")
                SettingsDivider()
                SettingsNavigationRow(title = "이용약관", onClick = { /* TODO */ })
                SettingsDivider()
                SettingsNavigationRow(title = "개인정보처리방침", onClick = { /* TODO */ })
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ───────────────────────────────────────────
// 섹션 타이틀
// ───────────────────────────────────────────
@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ───────────────────────────────────────────
// 설정 카드 컨테이너
// ───────────────────────────────────────────
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        content = content
    )
}

// ───────────────────────────────────────────
// 구분선
// ───────────────────────────────────────────
@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFF0F0F0)
    )
}

// ───────────────────────────────────────────
// 토글 행 (유통기한 알림, 사진 등록 알림)
// ───────────────────────────────────────────
@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FreshGreen
            )
        )
    }
}

// ───────────────────────────────────────────
// 버튼 행 (푸시 알림 권한)
// ───────────────────────────────────────────
@Composable
fun SettingsButtonRow(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedButton(
            onClick = onButtonClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(buttonText, fontSize = 13.sp)
        }
    }
}

// ───────────────────────────────────────────
// 네비게이션 행 (앱 정보)
// ───────────────────────────────────────────
@Composable
fun SettingsNavigationRow(
    title: String,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 15.sp)
        if (value != null) {
            Text(value, fontSize = 14.sp, color = Color.Gray)
        } else {
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}