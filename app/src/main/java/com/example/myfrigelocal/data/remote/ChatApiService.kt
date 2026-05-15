package com.example.myfrigelocal.data.remote

import com.example.myfrigelocal.data.remote.dto.ChatRoomDetailDto
import com.example.myfrigelocal.data.remote.dto.ChatRoomSectionsDto
import com.example.myfrigelocal.data.remote.dto.CreateChatRoomResponseDto
import com.example.myfrigelocal.data.remote.dto.SendMessageRequest
import com.example.myfrigelocal.data.remote.dto.SendMessageResponseDto
import com.example.myfrigelocal.data.remote.dto.UpdateRoomTitleRequest
import com.example.myfrigelocal.data.remote.dto.UpdateRoomTitleResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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

    @PATCH("ai/v1/chat/room/{roomId}")
    suspend fun updateRoomTitle(
        @Path("roomId") roomId: Long,
        @Body body: UpdateRoomTitleRequest,
    ): Response<ApiResponse<UpdateRoomTitleResponseDto>>
}
