package com.example.myfrigelocal.ui.screens.chat

/** Matches backend `uiType` for general chat bubbles. */
const val AI_RESPONSE_TYPE_TEXT = "GENERAL"

/** Matches backend `uiType` for recipe cards. */
const val AI_RESPONSE_TYPE_RECIPE = "RECIPE"

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
