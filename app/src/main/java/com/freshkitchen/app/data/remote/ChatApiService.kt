package com.freshkitchen.app.data.remote

import com.freshkitchen.app.data.remote.dto.ChatRoomDetailDto
import com.freshkitchen.app.data.remote.dto.ChatRoomSectionsDto
import com.freshkitchen.app.data.remote.dto.CreateChatRoomResponseDto
import com.freshkitchen.app.data.remote.dto.EmptyApiDataDto
import com.freshkitchen.app.data.remote.dto.SendMessageRequest
import com.freshkitchen.app.data.remote.dto.SendMessageResponseDto
import com.freshkitchen.app.data.remote.dto.UpdateAiSettingRequest
import com.freshkitchen.app.data.remote.dto.UpdateRoomTitleRequest
import com.freshkitchen.app.data.remote.dto.UpdateRoomTitleResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

/**
 * AI Chat API — baseUrl must be `http://api.app-fresh.com/` (trailing slash).
 * Paths are relative: `ai/v1/...` (do **not** prefix `/api/v1`).
 */
interface ChatApiService {

    @GET("ai/v1/chat/room")
    suspend fun getChatRooms(): Response<ApiResponse<ChatRoomSectionsDto>>

    @POST("ai/v1/chat/room")
    suspend fun createChatRoom(): Response<ApiResponse<CreateChatRoomResponseDto>>

    @GET("ai/v1/chat/room/{roomId}")
    suspend fun getChatRoomDetail(
        @Path("roomId") roomId: Long,
    ): Response<ApiResponse<ChatRoomDetailDto>>

    @POST("ai/v1/chat/room/{roomId}")
    suspend fun sendMessage(
        @Path("roomId") roomId: Long,
        @Body body: SendMessageRequest,
    ): Response<ApiResponse<SendMessageResponseDto>>

    /**
     * AI setting API — path supplied at call time via [AiChatApiConfig.AI_SETTING_UPDATE_PATH].
     * Uses `@Url` because the endpoint is not finalized yet.
     */
    @POST
    suspend fun updateAiSetting(
        @Url endpoint: String,
        @Body body: UpdateAiSettingRequest,
    ): Response<ApiResponse<EmptyApiDataDto?>>

    @PATCH("ai/v1/chat/room/{roomId}")
    suspend fun updateRoomTitle(
        @Path("roomId") roomId: Long,
        @Body body: UpdateRoomTitleRequest,
    ): Response<ApiResponse<UpdateRoomTitleResponseDto>>

    /** Swagger: `DELETE /ai/v1/chat/delete/room/{roomId}` — `data` is `{}`. */
    @DELETE("ai/v1/chat/delete/room/{roomId}")
    suspend fun deleteChatRoom(
        @Path("roomId") roomId: Long,
    ): Response<ApiResponse<EmptyApiDataDto?>>
}
