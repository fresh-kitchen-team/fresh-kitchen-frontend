package com.example.myfrigelocal.data.repository

import com.example.myfrigelocal.data.remote.ApiResponse
import com.example.myfrigelocal.data.remote.ChatApiService
import com.example.myfrigelocal.data.remote.dto.ChatMessageDto
import com.example.myfrigelocal.data.remote.dto.ChatRoomDto
import com.example.myfrigelocal.data.remote.dto.CreateChatRoomRequest
import com.example.myfrigelocal.data.remote.dto.SendMessageRequest
import com.example.myfrigelocal.data.remote.dto.UpdateRoomTitleRequest
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ChatRepository(
    private val api: ChatApiService,
) {

    suspend fun listRooms(): Result<List<ChatRoomDto>> =
        unwrapList(api.getRooms())

    suspend fun createRoom(title: String): Result<ChatRoomDto> =
        unwrapSingle(api.createRoom(CreateChatRoomRequest(title)))

    suspend fun getMessages(roomId: Long): Result<List<ChatMessageDto>> =
        unwrapList(api.getMessages(roomId))

    suspend fun sendMessage(roomId: Long, message: String): Result<ChatMessageDto> =
        unwrapSingle(api.sendMessage(roomId, SendMessageRequest(message)))

    suspend fun updateRoomTitle(roomId: Long, title: String): Result<Unit> {
        return try {
            val response = api.updateRoomTitle(roomId, UpdateRoomTitleRequest(title))
            if (!response.isSuccessful) {
                return Result.failure(HttpException(response))
            }
            val body = response.body() ?: return Result.failure(IllegalStateException("Empty body"))
            if (!body.isBusinessSuccess()) {
                return Result.failure(
                    IllegalStateException(body.message ?: "updateRoomTitle failed (status=${body.status})"),
                )
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    private fun <T> ApiResponse<T>.isBusinessSuccess(): Boolean =
        status == 0 || status == 200

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
        }
    }

    private fun <T> unwrapList(response: Response<ApiResponse<List<T>>>): Result<List<T>> {
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
            Result.success(body.data.orEmpty())
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}
