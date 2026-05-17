package com.example.myfrigelocal.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myfrigelocal.ui.theme.BottomNavSelected
import com.example.myfrigelocal.ui.theme.MyFrigeLocalTheme

private val CardCorner = RoundedCornerShape(18.dp)
private val ThumbnailShape = RoundedCornerShape(12.dp)

private val SectionTitleColor = Color(0xFF374151)
private val BodyTextColor = Color(0xFF111827)
private val StepContainerColor = Color(0xFFF3F4F6)
private val TipBackground = Color(0xFFEEFBF4)
private val MissingSectionBackground = Color(0xFFFFF7E6)

/**
 * AI recipe card — collapsed: summary + missing ingredients; expanded: + steps + tips.
 *
 * @param expandStateKey Stable key per message (e.g. [com.example.myfrigelocal.ui.screens.chat.ChatMessage.id]).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeResponseCard(
    recipe: RecipeUiModel,
    expandStateKey: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(expandStateKey) { mutableStateOf(false) }

    val hasSteps = recipe.steps.any { it.isNotBlank() }
    val hasTip = !recipe.tip.isNullOrBlank()
    val missingItems = recipe.missingIngredients.map { it.trim() }.filter { it.isNotEmpty() }
    val hasExpandableContent = hasSteps || hasTip

    val toggleExpanded: () -> Unit = {
        if (hasExpandableContent) {
            expanded = !expanded
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardCorner,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (hasExpandableContent) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = toggleExpanded,
                            )
                        } else {
                            Modifier
                        },
                    ),
            ) {
                RecipeCardHeader(
                    title = recipe.title,
                    cookTime = recipe.cookTime,
                    imageUrl = recipe.imageUrl,
                )

                if (recipe.ingredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "재료",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                        ),
                        color = SectionTitleColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IngredientChipsRow(ingredients = recipe.ingredients)
                }

                if (missingItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MissingIngredientSection(ingredients = missingItems)
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                RecipeDetailSection(
                    steps = recipe.steps,
                    tip = recipe.tip,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            if (hasExpandableContent) {
                Spacer(modifier = Modifier.height(4.dp))
                ExpandToggleButton(
                    expanded = expanded,
                    onClick = toggleExpanded,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IngredientChipsRow(
    ingredients: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ingredients
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { IngredientChip(label = it) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeDetailSection(
    steps: List<String>,
    tip: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val validSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }
        if (validSteps.isNotEmpty()) {
            Text(
                text = "조리 순서",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                ),
                color = SectionTitleColor,
            )
            Spacer(modifier = Modifier.height(6.dp))
            validSteps.forEachIndexed { index, step ->
                CookingStepItem(stepNumber = index + 1, text = step)
                if (index != validSteps.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        val tipText = tip?.trim().orEmpty()
        if (tipText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            TipBox(text = tipText)
        }
    }
}

@Composable
private fun ExpandToggleButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = if (expanded) "접기" else "더보기",
            color = BottomNavSelected,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = BottomNavSelected,
            modifier = Modifier.size(18.dp),
        )
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.ifBlank { "레시피" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                ),
                color = BodyTextColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            CookTimeBadge(cookTime = cookTime.ifBlank { "—" })
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
            .size(64.dp)
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
                modifier = Modifier.size(28.dp),
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Color(0xFF15803D),
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = cookTime,
                style = MaterialTheme.typography.labelMedium,
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
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9FAFB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
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
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.size(22.dp),
            shape = CircleShape,
            color = BottomNavSelected,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    ),
                    color = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
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
                modifier = Modifier.weight(1f),
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ingredients.forEach { missing ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
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
private fun RecipeResponseCardCollapsedPreview() {
    MyFrigeLocalTheme {
        RecipeResponseCard(
            recipe = RecipeUiModel(
                title = "김치볶음밥",
                cookTime = "20분",
                ingredients = listOf("밥", "김치", "계란", "참기름"),
                steps = listOf(
                    "김치를 잘게 썹니다.",
                    "팬에 기름을 두르고 볶습니다.",
                    "밥과 함께 볶아 마무리합니다.",
                ),
                tip = "김치는 너무 오래 볶지 마세요.",
                missingIngredients = listOf("계란"),
                imageUrl = "",
            ),
            expandStateKey = "preview-1",
            modifier = Modifier.padding(16.dp),
        )
    }
}
