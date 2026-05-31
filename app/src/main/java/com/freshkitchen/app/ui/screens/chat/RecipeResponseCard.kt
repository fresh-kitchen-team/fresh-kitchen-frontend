package com.freshkitchen.app.ui.screens.chat

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
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.freshkitchen.app.ui.theme.MyFrigeLocalTheme

private val CardCorner = ChatDesign.CardShape
private val ThumbnailShape = RoundedCornerShape(14.dp)
private val SectionShape = RoundedCornerShape(12.dp)

private val BodyTextColor = ChatDesign.TextPrimary
private val SectionIconGreen = Color(0xFF16A34A)
private val TipBackground = Color(0xFFEEFBF4)
private val TipBorder = Color(0xFFD1FAE5)
private val MissingSectionBackground = Color(0xFFFFF9ED)
private val MissingSectionBorder = Color(0xFFFDE68A)
private val MissingAccent = Color(0xFFD97706)
private val ChipBorder = Color(0xFFE5E7EB)

/**
 * AI recipe card — collapsed: summary + missing ingredients; expanded: + steps + tips.
 *
 * @param expandStateKey Stable key per message (e.g. [com.freshkitchen.app.ui.screens.chat.ChatMessage.id]).
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
        color = ChatDesign.SurfaceWhite,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatDesign.BorderSoft),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
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
                    Spacer(modifier = Modifier.height(14.dp))
                    RecipeSectionHeader(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "재료",
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    IngredientChipsRow(ingredients = recipe.ingredients)
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
                    showTopDivider = recipe.ingredients.isNotEmpty(),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (missingItems.isNotEmpty()) {
                val showDivider = recipe.ingredients.isNotEmpty() || (expanded && (hasSteps || hasTip))
                if (showDivider) {
                    Spacer(modifier = Modifier.height(14.dp))
                    RecipeSectionDivider()
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }
                MissingIngredientSection(ingredients = missingItems)
            }

            if (hasExpandableContent) {
                Spacer(modifier = Modifier.height(12.dp))
                ExpandToggleButton(
                    expanded = expanded,
                    onClick = toggleExpanded,
                )
            }
        }
    }
}

@Composable
private fun RecipeSectionHeader(
    icon: ImageVector,
    title: String,
    iconTint: Color = SectionIconGreen,
    titleColor: Color = BodyTextColor,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            ),
            color = titleColor,
        )
    }
}

@Composable
private fun RecipeSectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = ChatDesign.BorderSoft,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IngredientChipsRow(
    ingredients: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
    showTopDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showTopDivider) {
            Spacer(modifier = Modifier.height(14.dp))
            RecipeSectionDivider()
        }

        val validSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }
        if (validSteps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            RecipeSectionHeader(
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                title = "조리 순서",
            )
            Spacer(modifier = Modifier.height(10.dp))
            validSteps.forEachIndexed { index, step ->
                CookingStepItem(stepNumber = index + 1, text = step)
                if (index != validSteps.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        val tipText = tip?.trim().orEmpty()
        if (tipText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
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
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFE8FAF2),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB8F0D4)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (expanded) "간략하게 보기" else "자세히 보기",
                    color = ChatDesign.ChatPrimary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = ChatDesign.ChatPrimary,
                    modifier = Modifier.size(18.dp),
                )
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
            .size(72.dp)
            .clip(ThumbnailShape)
            .background(Color(0xFFF6F8F7))
            .then(
                if (!showImage) {
                    Modifier.border(1.dp, ChipBorder, ThumbnailShape)
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
                tint = ChatDesign.TextMuted,
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
                tint = SectionIconGreen,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = cookTime,
                style = MaterialTheme.typography.labelMedium,
                color = SectionIconGreen,
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
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChipBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SectionShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChipBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = ChatDesign.ChatPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stepNumber.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = Color.White,
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
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
        shape = SectionShape,
        color = TipBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, TipBorder),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = SectionIconGreen,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "팁",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                    color = SectionIconGreen,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
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
        shape = SectionShape,
        color = MissingSectionBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MissingSectionBorder),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            RecipeSectionHeader(
                icon = Icons.Outlined.WarningAmber,
                title = "부족한 재료",
                iconTint = MissingAccent,
                titleColor = MissingAccent,
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ingredients.forEach { missing ->
                    IngredientChip(label = missing)
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
                title = "토마토 계란 볶음",
                cookTime = "10분",
                ingredients = listOf("계란", "토마토", "소금", "식용유"),
                steps = listOf(
                    "계란을 풀어 준비합니다",
                    "토마토를 먹기 좋게 자릅니다",
                    "팬에 기름을 두르고 토마토를 볶습니다",
                    "계란을 넣고 함께 볶습니다",
                ),
                tip = "토마토는 너무 오래 볶지 않는 것이 좋습니다.",
                missingIngredients = listOf("소금", "식용유"),
                imageUrl = "",
            ),
            expandStateKey = "preview-1",
            modifier = Modifier.padding(16.dp),
        )
    }
}
