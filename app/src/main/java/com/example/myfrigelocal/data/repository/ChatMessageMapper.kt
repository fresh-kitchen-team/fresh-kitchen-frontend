package com.example.myfrigelocal.data.repository

import com.example.myfrigelocal.data.remote.dto.ChatMessageDto
import com.example.myfrigelocal.ui.screens.chat.AI_RESPONSE_TYPE_TEXT
import com.example.myfrigelocal.ui.screens.chat.ChatMessage
import com.example.myfrigelocal.ui.screens.chat.Sender

/**
 * Maps Swagger [ChatMessageDto] to UI [ChatMessage].
 *
 * Swagger: `sender` is typically `"USER"` / `"AI"` (uppercase).
 *
 * TODO: [ChatMessageDto.aiPayload] is a **String** in Swagger — do not parse into [RecipeUiModel]
 * until the backend documents the exact JSON format. When confirmed, map recipe payloads here
 * and set [ChatMessage.responseType] / [ChatMessage.recipe] accordingly.
 */
fun ChatMessageDto.toChatMessage(): ChatMessage {
    val normalized = sender.trim().uppercase()
    val senderEnum = when (normalized) {
        "USER" -> Sender.User
        "AI" -> Sender.Ai
        else -> Sender.Ai
    }
    return ChatMessage(
        id = id.toString(),
        sender = senderEnum,
        text = content,
        responseType = AI_RESPONSE_TYPE_TEXT,
        recipe = null,
        aiPayloadRaw = aiPayload,
    )
}
