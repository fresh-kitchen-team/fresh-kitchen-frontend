package com.freshkitchen.app.data.repository

import com.freshkitchen.app.data.remote.dto.AiPayloadDto
import com.freshkitchen.app.data.remote.dto.ChatMessageDto
import com.freshkitchen.app.data.remote.dto.MatchedItemDto
import com.freshkitchen.app.data.remote.dto.RecipeDto
import com.freshkitchen.app.ui.screens.chat.AI_RESPONSE_TYPE_RECIPE
import com.freshkitchen.app.ui.screens.chat.AI_RESPONSE_TYPE_TEXT
import com.freshkitchen.app.ui.screens.chat.ChatMessage
import com.freshkitchen.app.ui.screens.chat.RecipeMatchedItemUi
import com.freshkitchen.app.ui.screens.chat.RecipeUiModel
import com.freshkitchen.app.ui.screens.chat.Sender

/**
 * Maps [ChatMessageDto] (Swagger /ai/v1) to UI [ChatMessage].
 *
 * `sender`: treats USER / user / AI / Ai / ASSISTANT (case-insensitive) safely.
 * `uiType`: `RECIPE` → recipe card when [aiPayload] has a recipe; `GENERAL` → text bubble.
 */
fun ChatMessageDto.toChatMessage(): ChatMessage {
    val normalized = sender.trim().uppercase()
    val senderEnum =
        when (normalized) {
            "USER" -> Sender.User
            "AI", "ASSISTANT", "BOT", "SYSTEM" -> Sender.Ai
            else -> if (normalized.contains("USER")) Sender.User else Sender.Ai
        }
    val recipe = aiPayload?.toPrimaryRecipeUiModel()
    val responseType =
        when (uiType?.trim()?.uppercase()) {
            "RECIPE" -> AI_RESPONSE_TYPE_RECIPE
            "GENERAL" -> AI_RESPONSE_TYPE_TEXT
            else -> if (recipe != null) AI_RESPONSE_TYPE_RECIPE else AI_RESPONSE_TYPE_TEXT
        }
    return ChatMessage(
        id = messageId.toString(),
        sender = senderEnum,
        text = text,
        responseType = responseType,
        recipe = recipe,
    )
}

private fun AiPayloadDto.toPrimaryRecipeUiModel(): RecipeUiModel? {
    val first = recipes?.firstOrNull() ?: return null
    val matched = matchedItems.orEmpty().toMatchedItemUiList()
    return first.toRecipeUiModel(
        tips = tips.orEmpty(),
        missingFromPayload = missingIngredients.orEmpty(),
        matchedItems = matched,
    )
}

private fun List<MatchedItemDto>.toMatchedItemUiList(): List<RecipeMatchedItemUi> =
    mapIndexed { index, dto ->
        RecipeMatchedItemUi(
            itemId = dto.itemId,
            name = dto.name.trim(),
            rowKey = "matched_${dto.itemId}_$index",
        )
    }

private fun RecipeDto.toRecipeUiModel(
    tips: List<String>,
    missingFromPayload: List<String>,
    matchedItems: List<RecipeMatchedItemUi>,
): RecipeUiModel {
    val tipJoined = tips.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n").ifBlank { null }
    return RecipeUiModel(
        title = name.trim().ifBlank { "레시피" },
        cookTime = time?.trim().orEmpty().ifBlank { "—" },
        ingredients = ingredients.orEmpty(),
        steps = steps.orEmpty(),
        tip = tipJoined,
        missingIngredients = missingFromPayload,
        imageUrl = "",
        matchedItems = matchedItems,
    )
}
