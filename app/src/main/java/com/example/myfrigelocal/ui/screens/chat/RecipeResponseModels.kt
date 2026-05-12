package com.example.myfrigelocal.ui.screens.chat

/** Matches backend/API `type` field for AI payloads. */
const val AI_RESPONSE_TYPE_TEXT = "text"

const val AI_RESPONSE_TYPE_RECIPE = "recipe"

/**
 * UI model aligned with a typical recipe JSON payload from the backend.
 *
 * Example:
 * ```json
 * {
 *   "type": "recipe",
 *   "title": "토마토 계란 볶음",
 *   "cookTime": "10분",
 *   "ingredients": ["계란", "토마토"],
 *   "steps": ["..."],
 *   "tip": "...",
 *   "missingIngredients": ["소금"],
 *   "imageUrl": ""
 * }
 * ```
 */
data class RecipeUiModel(
    val title: String,
    val cookTime: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val tip: String?,
    val missingIngredients: List<String>,
    val imageUrl: String,
)
