package com.example.myfrigelocal.data.mapper

import com.example.myfrigelocal.data.model.Ingredient
import com.example.myfrigelocal.data.remote.dto.ChatIngredientDto

fun Ingredient.toChatIngredientDto(): ChatIngredientDto {
    val longId = id.toLongOrNull()
        ?: (id.hashCode().toLong() and Long.MAX_VALUE)
    return ChatIngredientDto(
        id = longId,
        name = name,
        expiresAt = null, // TODO: Ingredient 모델에 유통기한 필드 생기면 매핑
    )
}
