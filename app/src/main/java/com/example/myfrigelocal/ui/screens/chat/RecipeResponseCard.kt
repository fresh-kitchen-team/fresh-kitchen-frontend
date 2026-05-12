package com.example.myfrigelocal.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme

private val CardCorner = RoundedCornerShape(22.dp)
private val ThumbnailShape = RoundedCornerShape(14.dp)

private val SectionTitleColor = Color(0xFF374151)
private val BodyTextColor = Color(0xFF111827)
private val StepContainerColor = Color(0xFFF3F4F6)
private val TipBackground = Color(0xFFEEFBF4)
private val MissingSectionBackground = Color(0xFFFFF7E6)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeResponseCard(
    recipe: RecipeUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardCorner,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            RecipeCardHeader(
                title = recipe.title,
                cookTime = recipe.cookTime,
                imageUrl = recipe.imageUrl,
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "재료",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = SectionTitleColor,
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                recipe.ingredients.forEach { IngredientChip(label = it) }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "조리 순서",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                ),
                color = SectionTitleColor,
            )
            Spacer(modifier = Modifier.height(6.dp))
            recipe.steps.forEachIndexed { index, step ->
                CookingStepItem(stepNumber = index + 1, text = step)
                if (index != recipe.steps.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            val tip = recipe.tip?.trim().orEmpty()
            if (tip.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                TipBox(text = tip)
            }

            if (recipe.missingIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                MissingIngredientSection(ingredients = recipe.missingIngredients)
            }
        }
    }
}

@Composable
private fun RecipeCardHeader(
    title: String,
    cookTime: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeThumbnail(imageUrl = imageUrl)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.ifBlank { "레시피" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                ),
                color = BodyTextColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            CookTimeBadge(cookTime = cookTime)
        }
    }
}

@Composable
private fun RecipeThumbnail(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val url = imageUrl.trim()
    val showImage = url.isNotEmpty()

    Box(
        modifier = modifier
            .size(76.dp)
            .clip(ThumbnailShape)
            .background(Color(0xFFF3F4F6))
            .then(
                if (!showImage) {
                    Modifier.border(1.dp, Color(0xFFE5E7EB), ThumbnailShape)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (showImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun CookTimeBadge(
    cookTime: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFE8F9F0),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Color(0xFF15803D),
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = cookTime,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF15803D),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun IngredientChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF9FAFB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = BodyTextColor,
        )
    }
}

@Composable
fun CookingStepItem(
    stepNumber: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = BottomNavSelected,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    ),
                    color = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = StepContainerColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = BodyTextColor,
            )
        }
    }
}

@Composable
fun TipBox(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = TipBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1FAE5)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
                color = BodyTextColor,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissingIngredientSection(
    ingredients: List<String>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MissingSectionBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = "부족한 재료",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                ),
                color = Color(0xFF92400E),
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ingredients.forEach { missing ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF4D6),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBBF24)),
                    ) {
                        Text(
                            text = missing,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF92400E),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RecipeResponseCardPreview() {
    MyFrigeLocalTheme {
        RecipeResponseCard(
            recipe = RecipeUiModel(
                title = "토마토 계란 볶음",
                cookTime = "10분",
                ingredients = listOf("계란", "토마토", "소금", "식용유"),
                steps = listOf(
                    "계란을 풀어 준비합니다",
                    "토마토를 먹기 좋게 자릅니다",
                    "팬에 기름을 두르고 토마토를 볶습니다",
                    "계란을 넣고 함께 볶습니다",
                ),
                tip = "토마토는 너무 오래 볶지 않는 것이 좋아요.",
                missingIngredients = listOf("소금", "식용유"),
                imageUrl = "",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
