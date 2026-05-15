package com.example.myfrigelocal.data.remote.dto

import com.google.gson.annotations.SerializedName

/** GET /ai/v1/chat/room — one row in today / last7Days / last30Days. */
data class ChatRoomSummaryDto(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("sender") val sender: String? = null,
    @SerializedName("content") val content: String? = null,
)

/** GET /ai/v1/chat/room — `data` payload. */
data class ChatRoomSectionsDto(
    @SerializedName("today") val today: List<ChatRoomSummaryDto>? = null,
    @SerializedName("last7Days") val last7Days: List<ChatRoomSummaryDto>? = null,
    @SerializedName("last30Days") val last30Days: List<ChatRoomSummaryDto>? = null,
)

/** POST /ai/v1/chat/room — `data` payload. */
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

data class AiPayloadDto(
    @SerializedName("recipes") val recipes: List<RecipeDto>? = null,
    @SerializedName("tips") val tips: List<String>? = null,
    @SerializedName("missingIngredients") val missingIngredients: List<String>? = null,
)

/** GET /ai/v1/chat/room/{roomId} — one message. */
data class ChatMessageDto(
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("sender") val sender: String,
    /** Swagger field `text`; some servers may still send `content`. */
    @SerializedName(value = "text", alternate = ["content"])
    val text: String,
    @SerializedName("aiPayload") val aiPayload: AiPayloadDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

/** GET /ai/v1/chat/room/{roomId} — `data` payload. */
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

/**
 * POST /ai/v1/chat/room/{roomId} — Swagger 예시와 동일한 키 구조.
 *
 * `ingredients` / `userPreferences`는 스키마상 존재해야 하며, 빈 배열만으로는 서버 검증(@NotEmpty 등)에 걸릴 수 있음.
 * 실제 재고는 [com.example.myfrigelocal.data.repository.FridgeRepository]에서 채우고,
 * 비어 있으면 ViewModel에서 사용자 메시지 기반 단일 항목으로 보완한다.
 */
data class SendMessageRequest(
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("ingredients") val ingredients: List<ChatIngredientDto>,
    @SerializedName("userPreferences") val userPreferences: UserPreferencesDto,
)

/** POST /ai/v1/chat/room/{roomId} — `data` payload. */
data class SendMessageResponseDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("aiMessage") val aiMessage: ChatMessageDto,
)

data class UpdateRoomTitleRequest(
    @SerializedName("title") val title: String,
)

/** PATCH /ai/v1/chat/room/{roomId} — `data` payload. */
data class UpdateRoomTitleResponseDto(
    @SerializedName("roomId") val roomId: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)
