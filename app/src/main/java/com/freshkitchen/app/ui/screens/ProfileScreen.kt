package com.freshkitchen.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.freshkitchen.app.ui.theme.FreshGreen
import com.freshkitchen.app.ui.theme.FreshGreenDark
import com.freshkitchen.app.viewmodel.ProfileViewModel

// ───────────────────────────────────────────
// 프로필 화면
// ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 이미지 피커 (갤러리에서 선택)
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    // 저장 완료 스낵바
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("저장되었습니다 ✓")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("프로필 설정", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── 프로필 사진 ──
            ProfilePhotoSection(
                imageUrl = uiState.profileImageUrl,
                onCameraClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 닉네임 ──
            ProfileSectionLabel("닉네임")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = { viewModel.onNicknameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FreshGreenDark,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(28.dp))

            // ── 알러지 정보 ──
            ProfileSectionHeader(
                title = "알러지 정보",
                subtitle = "해당 재료 포함 시 AI가 알려드려요"
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileChipGrid(
                items = listOf(
                    "🥛 유제품", "🥚 계란", "🌾 밀·글루텐", "🦐 갑각류",
                    "🥜 견과류", "🫘 콩·대두", "🐟 생선", "🥜 땅콩",
                    "🍑 복숭아", "🐷 돼지고기", "🍄 버섯", "🧅 파·마늘"
                ),
                selected = uiState.selectedAllergies,
                onToggle = { viewModel.toggleAllergy(it) },
                selectedColor = Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(28.dp))

            // ── 선호 식재료 ──
            ProfileSectionHeader(
                title = "선호 식재료",
                subtitle = "자주 쓰는 재료를 선택해두세요"
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 기본 식재료
            ProfileSubLabel("기본 식재료")
            Spacer(modifier = Modifier.height(8.dp))
            ProfileChipGrid(
                items = listOf("🧅 양파", "🧄 마늘", "🌿 대파", "🥔 감자", "🥕 당근", "🍅 토마토"),
                selected = uiState.selectedIngredients,
                onToggle = { viewModel.toggleIngredient(it) },
                selectedColor = FreshGreenDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 인기 재료
            ProfileSubLabel("인기 재료")
            Spacer(modifier = Modifier.height(8.dp))
            ProfileChipGrid(
                items = listOf("🥑 아보카도", "🐟 연어", "🥩 스테이크용 등심", "🍗 닭가슴살", "🦐 새우", "🧀 치즈"),
                selected = uiState.selectedIngredients,
                onToggle = { viewModel.toggleIngredient(it) },
                selectedColor = FreshGreenDark
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(28.dp))

            // ── 선호 음식 스타일 ──
            ProfileSectionHeader(
                title = "선호 음식 스타일",
                subtitle = "AI 레시피 추천에 활용돼요"
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileChipGrid(
                items = listOf(
                    "🍚 한식", "🍣 일식", "🥢 중식", "🍝 양식",
                    "🌮 멕시칸", "🥗 채식·비건", "🌶️ 매콤한 것", "🍯 달달한 것",
                    "🍲 담백한 것", "🥩 고기 위주"
                ),
                selected = uiState.selectedFoodStyles,
                onToggle = { viewModel.toggleFoodStyle(it) },
                selectedColor = Color(0xFFF97316)
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(28.dp))

            // ── 사용 가능 식기구 ──
            ProfileSectionHeader(
                title = "사용 가능 식기구",
                subtitle = "보유한 조리 도구를 선택해두세요"
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileChipGrid(
                items = listOf(
                    "🍳 프라이팬", "🥘 냄비", "♨️ 압력솥", "🫕 찜기",
                    "🔥 오븐", "⚡ 전자레인지", "💨 에어프라이어", "🍚 밥솥",
                    "🧊 블렌더", "🔪 그릴"
                ),
                selected = uiState.selectedUtensils,
                onToggle = { viewModel.toggleUtensil(it) },
                selectedColor = Color(0xFF6366F1)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── 저장 버튼 ──
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Text("저장하기", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ───────────────────────────────────────────
// 프로필 사진 영역
// ───────────────────────────────────────────
@Composable
fun ProfilePhotoSection(
    imageUrl: String? = null,
    onCameraClick: () -> Unit = {}
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        // 프로필 사진 원형
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9))
                .border(2.dp, Color(0xFFE0E0E0), CircleShape)
                .clickable { onCameraClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "프로필 사진",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = FreshGreenDark
                )
            }
        }
        // 편집 버튼 (우측 하단)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(FreshGreenDark)
                .clickable { onCameraClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "사진 변경",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ───────────────────────────────────────────
// 섹션 헤더 (제목 + 부제목)
// ───────────────────────────────────────────
@Composable
fun ProfileSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
    }
}

// ───────────────────────────────────────────
// 섹션 라벨 (입력 필드용)
// ───────────────────────────────────────────
@Composable
fun ProfileSectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.fillMaxWidth()
    )
}

// ───────────────────────────────────────────
// 소섹션 라벨 (기본 식재료, 인기 재료 등)
// ───────────────────────────────────────────
@Composable
fun ProfileSubLabel(label: String) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Gray,
        modifier = Modifier.fillMaxWidth()
    )
}

// ───────────────────────────────────────────
// 칩 그리드 (FlowRow)
// ───────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileChipGrid(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    selectedColor: Color = FreshGreenDark
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            SelectableChip(
                label = item,
                isSelected = item in selected,
                onClick = { onToggle(item) },
                selectedColor = selectedColor
            )
        }
    }
}

// ───────────────────────────────────────────
// 미리보기
// ───────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}