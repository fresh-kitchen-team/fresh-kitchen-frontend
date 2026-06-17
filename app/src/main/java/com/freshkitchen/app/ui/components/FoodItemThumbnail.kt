package com.freshkitchen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freshkitchen.app.ui.theme.LightGray
import com.freshkitchen.app.viewmodel.FoodItem

/**
 * 식재료 대표 이미지 or 이모지 표시 컴포저블
 *
 * - representativeImage.type == "PHOTO" → Coil AsyncImage
 *   - isDetail = false (목록/그리드): thumbnailUrl 사용
 *   - isDetail = true  (상세 다이얼로그): imageUrl 사용
 * - 나머지 → emoji 텍스트 (representativeImage.emoji 우선, 없으면 item.emoji 폴백)
 *
 * @param item        표시할 FoodItem
 * @param size        박스 크기 (가로 = 세로)
 * @param emojiSize   이모지 폰트 크기
 * @param cornerRadius 모서리 둥글기 (기본 10.dp)
 * @param isDetail    true면 원본(imageUrl), false면 썸네일(thumbnailUrl) 사용
 */
@Composable
fun FoodItemThumbnail(
    item: FoodItem,
    size: Dp,
    emojiSize: TextUnit,
    cornerRadius: Dp = 10.dp,
    isDetail: Boolean = false,
) {
    val repImg = item.representativeImage

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (repImg?.type == "PHOTO") {
            val url = if (isDetail) repImg.imageUrl else (repImg.thumbnailUrl ?: repImg.imageUrl)
            AsyncImage(
                model = url,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            val emoji = repImg?.emoji ?: item.emoji
            Text(text = emoji, fontSize = emojiSize)
        }
    }
}
