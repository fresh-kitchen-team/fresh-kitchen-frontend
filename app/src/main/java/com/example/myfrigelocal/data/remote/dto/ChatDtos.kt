package com.example.myfrigelocal.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatRoomDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class ChatMessageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("content") val content: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("aiPayload") val aiPayload: String? = null,
    @SerializedName("createdAt") val createdAt: String,
)

data class CreateChatRoomRequest(
    @SerializedName("title") val title: String,
)

data class SendMessageRequest(
    @SerializedName("message") val message: String,
)

data class UpdateRoomTitleRequest(
    @SerializedName("title") val title: String,
)
