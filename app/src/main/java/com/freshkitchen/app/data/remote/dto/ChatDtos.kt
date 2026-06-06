package com.freshkitchen.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** GET /api/v1/chat/room — one row in today / last7Days / last30Days. */
data class ChatRoomSummaryDto(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("sender") val sender: String? = null,
    @SerializedName("content") val content: String? = null,
)

/** GET /api/v1/chat/room — `data` payload. */
data class ChatRoomSectionsDto(
    @SerializedName("today") val today: List<ChatRoomSummaryDto>? = null,
    @SerializedName("last7Days") val last7Days: List<ChatRoomSummaryDto>? = null,
    @SerializedName("last30Days") val last30Days: List<ChatRoomSummaryDto>? = null,
)

/** POST /api/v1/chat/room — `data` payload. */
data class CreateChatRoomResponseDto(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

data class RecipeDto(
    @SerializedName("name") val name: String,
    @SerializedName("ingredients") val ingredients: List<String>? = null,
    @SerializedName("steps") val steps: List<String>? = null,
    @SerializedName("time") val time: String? = null,
)

/** aiPayload.matchedItems — inventory rows matched to the recipe. */
data class MatchedItemDto(
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("name") val name: String,
)

data class AiPayloadDto(
    @SerializedName("recipes") val recipes: List<RecipeDto>? = null,
    @SerializedName("tips") val tips: List<String>? = null,
    @SerializedName("missingIngredients") val missingIngredients: List<String>? = null,
    @SerializedName("matchedItems") val matchedItems: List<MatchedItemDto>? = null,
)

/** GET /api/v1/chat/room/{roomId} — one message. */
data class ChatMessageDto(
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("sender") val sender: String,
    /** Swagger field `text`; some servers may still send `content`. */
    @SerializedName(value = "text", alternate = ["content"])
    val text: String,
    @SerializedName("aiPayload") val aiPayload: AiPayloadDto? = null,
    /** `RECIPE` (card) or `GENERAL` (text bubble). */
    @SerializedName("uiType") val uiType: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

/** GET /api/v1/chat/room/{roomId} — `data` payload. */
data class ChatRoomDetailDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("messages") val messages: List<ChatMessageDto>? = null,
)

data class ChatIngredientDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("expiresAt") val expiresAt: String? = null,
)

/**
 * Swagger `userPreferences` — 네 배열 키를 항상 JSON에 포함하려면 빈 리스트 기본값 사용
 * (Gson은 null 프로퍼티를 생략해 `{}`만 보내고, 서버는 필수 배열을 기대할 수 있음).
 */
data class UserPreferencesDto(
    @SerializedName("allergies") val allergies: List<String> = emptyList(),
    @SerializedName("preferredIngredients") val preferredIngredients: List<String> = emptyList(),
    @SerializedName("preferredFoodStyles") val preferredFoodStyles: List<String> = emptyList(),
    @SerializedName("cookingTool") val cookingTool: List<String> = emptyList(),
)

/** AI setting flags — sent via dedicated setting API (not with chat messages). */
data class AiSettingDto(
    @SerializedName("responseStyle") val responseStyle: Boolean = true,
    @SerializedName("priorityExpiration") val priorityExpiration: Boolean = true,
    @SerializedName("priorityNutrition") val priorityNutrition: Boolean = true,
    @SerializedName("priorityFrequent") val priorityFrequent: Boolean = true,
    @SerializedName("provideExtraInfo") val provideExtraInfo: Boolean = true,
)

/** POST /api/v1/chat/room/{roomId} — request body (`message` only). */
data class SendMessageRequest(
    @SerializedName("message") val message: String,
)

/** POST /api/v1/chat/room/{roomId} — `data` payload. */
data class SendMessageResponseDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("aiMessage") val aiMessage: ChatMessageDto,
)

data class UpdateRoomTitleRequest(
    @SerializedName("title") val title: String,
)

/** PATCH /api/v1/chat/room/{roomId} — `data` payload. */
data class UpdateRoomTitleResponseDto(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

/** DELETE `/api/v1/chat/delete/room/{roomId}` — Swagger `data: {}`. */
class EmptyApiDataDto
