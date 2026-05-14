package com.example.myfrigelocal.data.remote

import com.example.myfrigelocal.data.remote.dto.ChatMessageDto
import com.example.myfrigelocal.data.remote.dto.ChatRoomDto
import com.example.myfrigelocal.data.remote.dto.CreateChatRoomRequest
import com.example.myfrigelocal.data.remote.dto.SendMessageRequest
import com.example.myfrigelocal.data.remote.dto.UpdateRoomTitleRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {

    @GET("api/v1/chat/rooms")
    suspend fun getRooms(): Response<ApiResponse<List<ChatRoomDto>>>

    @POST("api/v1/chat/rooms")
    suspend fun createRoom(@Body body: CreateChatRoomRequest): Response<ApiResponse<ChatRoomDto>>

    @GET("api/v1/chat/rooms/{roomId}/messages")
    suspend fun getMessages(@Path("roomId") roomId: Long): Response<ApiResponse<List<ChatMessageDto>>>

    @POST("api/v1/chat/rooms/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: Long,
        @Body body: SendMessageRequest,
    ): Response<ApiResponse<ChatMessageDto>>

    /**
     * Swagger returns `"data": {}` — use [Any] so Gson accepts an empty object.
     */
    @PATCH("api/v1/chat/rooms/{roomId}")
    suspend fun updateRoomTitle(
        @Path("roomId") roomId: Long,
        @Body body: UpdateRoomTitleRequest,
    ): Response<ApiResponse<Any>>
}
