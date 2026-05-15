package com.example.myfrigelocal.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

// ───────────────────────────────────────────
// 식재료 API 인터페이스
// ───────────────────────────────────────────
interface IngredientApiService {

    // GET /api/v1/items — 전체 식재료 목록
    @GET("api/v1/items")
    suspend fun getIngredients(): ApiResponse<List<ItemDto>>

    // PATCH /api/v1/items/{id} — 식재료 수정
    @PATCH("api/v1/items/{id}")
    suspend fun updateItem(
        @Path("id") id: Long,
        @Body request: ItemUpdateRequest
    ): ApiResponse<Void>
}