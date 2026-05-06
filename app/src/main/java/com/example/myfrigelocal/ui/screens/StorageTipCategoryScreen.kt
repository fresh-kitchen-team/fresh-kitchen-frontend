package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myfrigelocal.R

@Composable
fun StorageTipCategoryScreen(
    navController: NavHostController,
    type: String,
    modifier: Modifier = Modifier,
) {
    val background = Color(0xFFF6F8F7)

    val uiModel = remember(type) { categoryUiModel(type) }
    val tips = remember(type) { categoryTips(type) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CategoryTopBar(
                title = uiModel.title,
                onBackClick = { navController.popBackStack() },
                containerColor = Color(0xFFF1F6F6),
            )
        },
        containerColor = background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(tips, key = { it.id }) { tip ->
                TipCard(
                    tip = tip,
                    iconBg = uiModel.iconBg,
                )
            }
        }
    }
}

@Composable
private fun CategoryTopBar(
    title: String,
    onBackClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF101418),
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101418),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Immutable
private data class TipUi(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
)

@Immutable
private data class CategoryUi(
    val title: String,
    val iconBg: Color,
)

private fun categoryUiModel(type: String): CategoryUi = when (type) {
    "vegetable" -> CategoryUi(title = "채소 및 과일", iconBg = Color(0xFFEAF7F2))
    "meat" -> CategoryUi(title = "육류", iconBg = Color(0xFFFFE9EA))
    "seafood" -> CategoryUi(title = "수산물", iconBg = Color(0xFFEAF2FF))
    else -> CategoryUi(title = "보관 팁", iconBg = Color(0xFFF1F3F5))
}

private fun categoryTips(type: String): List<TipUi> = when (type) {
    "vegetable" -> listOf(
        TipUi(
            id = "veg_ethylene",
            title = "에틸렌 가스 분리",
            description = "사과, 복숭아 등 에틸렌 가스를 내뿜는 과일은\n다른 채소와 따로 보관해야 빨리 시드는 것을 막을 수 있습니다.",
            iconRes = R.drawable.ic_bg_10,
        ),
        TipUi(
            id = "veg_moisture",
            title = "수분 차단과 유지",
            description = "오이와 키친타월로 감싸 수분을 조절하고,\n수분이 필요한 채소는 밀폐용기에 담아 보관하세요.",
            iconRes = R.drawable.ic_bg_11,
        ),
        TipUi(
            id = "veg_potato",
            title = "감자는 어두운 곳에",
            description = "빛을 받으면 독성 성분이 올라오니 생기므로\n신문지 싸서 서늘하고 어두운 곳에 보관하세요.",
            iconRes = R.drawable.ic_bg_12,
        ),
        TipUi(
            id = "veg_onion_freeze",
            title = "파는 냉동 보관",
            description = "대파는 손질 후 용도에 맞게 썰어 냉동 보관하면\n요리할 때 바로 꺼내 쓰기 편리합니다.",
            iconRes = R.drawable.ic_bg_13,
        ),
        TipUi(
            id = "veg_unwashed",
            title = "씻지 않고 보관",
            description = "대부분의 채소와 과일은 씻어서 보관하면 수분\n때문에 쉽게 무르므로 먹기 직전에 씻는 것이 좋습니다.",
            iconRes = R.drawable.ic_bg_14,
        ),
    )
    "meat" -> listOf(
        TipUi(
            id = "meat_oxidation",
            title = "표면 산화 방지",
            description = "고기 표면에 식용유를 살짝 바르면 공기와의 접촉을 막아 신선도를 더 오래 유지할 수 있습니다.",
            iconRes = R.drawable.ic_bg_0,
        ),
        TipUi(
            id = "meat_portion",
            title = "소분은 필수",
            description = "한 번 먹을 만큼씩 나누어 랩으로 밀착 포장한 뒤 지퍼백에 넣어 냉동 보관하세요.",
            iconRes = R.drawable.ic_bg_1,
        ),
        TipUi(
            id = "meat_kimchi_fridge",
            title = "김치냉장고 활용",
            description = "일반 냉장고보다 온도 변화가 적은 김치냉장고의 육류 전용 칸에 보관하는 것이 좋습니다.",
            iconRes = R.drawable.ic_bg_2,
        ),
        TipUi(
            id = "meat_fridge_days",
            title = "냉장 보관 기한 엄수",
            description = "소고기는 3~5일, 돼지고기와 닭고기는 1~2일\n내에 섭취하고 나머지는 냉동하세요.",
            iconRes = R.drawable.ic_bg_3,
        ),
        TipUi(
            id = "meat_thaw_slow",
            title = "해동은 천천히",
            description = "실온 해동보다는 하루 전날 냉장실로 옮겨 천천히 해동해야 육즙 손실이 적습니다.",
            iconRes = R.drawable.ic_bg_4,
        ),
    )
    "seafood" -> listOf(
        TipUi(
            id = "sea_guts",
            title = "내장 제거",
            description = "생선은 내장부터 부패가 시작되므로 반드시 내장을 제거하고 깨끗이 씻어 보관하세요.",
            iconRes = R.drawable.ic_bg_5,
        ),
        TipUi(
            id = "sea_water_salt",
            title = "물기 제거와 염장",
            description = "세척 후 키친타월로 물기를 완전히 닦고 소금을 약간 뿌려두면 살이 단단해집니다.",
            iconRes = R.drawable.ic_bg_6,
        ),
        TipUi(
            id = "sea_wrap",
            title = "밀폐 포장",
            description = "비린내가 다른 음식에 배지 않도록 랩으로 꼼꼼히 싸고 밀폐용기에 한 번 더 담으세요.",
            iconRes = R.drawable.ic_bg_7,
        ),
        TipUi(
            id = "sea_blanch",
            title = "해산물은 데쳐서 보관",
            description = "오징어나 문어 등은 살짝 데친 후 소분해서 냉동하면 맛과 식감을 더 잘 유지할 수 있습니다.",
            iconRes = R.drawable.ic_bg_8,
        ),
        TipUi(
            id = "sea_shellfish",
            title = "조개류 보관",
            description = "해감한 조개는 물기를 빼고 지퍼백에 담아 냉동 보관하거나, 소금물에 담가 냉장 보관하세요.",
            iconRes = R.drawable.ic_bg_9,
        ),
    )
    else -> emptyList()
}

@Composable
private fun TipCard(
    tip: TipUi,
    iconBg: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(tip.iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101418),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7680),
                )
            }
        }
    }
}

