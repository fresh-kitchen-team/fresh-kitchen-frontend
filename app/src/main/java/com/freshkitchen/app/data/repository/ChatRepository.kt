package com.freshkitchen.app.data.repository

import com.freshkitchen.app.data.remote.ApiResponse
import com.freshkitchen.app.data.remote.ChatApiService
import com.freshkitchen.app.data.remote.isBusinessSuccess
import com.freshkitchen.app.data.remote.dto.AiSettingDto
import com.freshkitchen.app.data.remote.dto.ChatRoomDetailDto
import com.freshkitchen.app.data.remote.dto.ChatRoomSectionsDto
import com.freshkitchen.app.data.remote.dto.CreateChatRoomResponseDto
import com.freshkitchen.app.data.remote.dto.EmptyApiDataDto
import com.freshkitchen.app.data.remote.dto.SendMessageRequest
import com.freshkitchen.app.data.remote.dto.SendMessageResponseDto
import com.freshkitchen.app.data.remote.dto.UpdateRoomTitleRequest
import com.freshkitchen.app.data.remote.dto.UpdateRoomTitleResponseDto
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import com.google.gson.JsonParseException

class ChatRepository(
    private val api: ChatApiService,
) {

    suspend fun getChatRooms(): Result<ChatRoomSectionsDto> =
        unwrapSingle(api.getChatRooms())

    suspend fun createChatRoom(): Result<CreateChatRoomResponseDto> =
        unwrapSingle(api.createChatRoom())

    suspend fun getChatRoomDetail(roomId: Long): Result<ChatRoomDetailDto> =
        unwrapSingle(api.getChatRoomDetail(roomId))

    suspend fun sendMessage(roomId: Long, body: SendMessageRequest): Result<SendMessageResponseDto> =
        unwrapSingle(api.sendMessage(roomId, body))

    suspend fun getAiSetting(): Result<AiSettingDto> =
        unwrapSingle(api.getAiSetting())

    suspend fun updateAiSetting(aiSetting: AiSettingDto): Result<AiSettingDto> =
        unwrapSingle(api.updateAiSetting(aiSetting))

    suspend fun updateRoomTitle(roomId: Long, title: String): Result<UpdateRoomTitleResponseDto> =
        unwrapSingle(api.updateRoomTitle(roomId, UpdateRoomTitleRequest(title)))

    suspend fun deleteChatRoom(roomId: Long): Result<Unit> =
        unwrapSuccess(api.deleteChatRoom(roomId))

    private fun unwrapSuccess(response: Response<ApiResponse<EmptyApiDataDto?>>): Result<Unit> {
        return try {
            if (!response.isSuccessful) {
                return Result.failure(HttpException(response))
            }
            val body = response.body() ?: return Result.failure(IllegalStateException("Empty body"))
            if (!body.isBusinessSuccess()) {
                return Result.failure(
                    IllegalStateException(body.message ?: "Request failed (status=${body.status})"),
                )
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: JsonParseException) {
            Result.failure(e)
        } catch (e: RuntimeException) {
            Result.failure(e)
        }
    }

    private fun <T> unwrapSingle(response: Response<ApiResponse<T>>): Result<T> {
        return try {
            if (!response.isSuccessful) {
                return Result.failure(HttpException(response))
            }
            val body = response.body() ?: return Result.failure(IllegalStateException("Empty body"))
            if (!body.isBusinessSuccess()) {
                return Result.failure(
                    IllegalStateException(body.message ?: "Request failed (status=${body.status})"),
                )
            }
            val data = body.data ?: return Result.failure(
                IllegalStateException(body.message ?: "Missing data"),
            )
            Result.success(data)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: JsonParseException) {
            Result.failure(e)
        } catch (e: RuntimeException) {
            // Gson / Retrofit conversion errors often surface as RuntimeException subclasses.
            Result.failure(e)
        }
    }
}
