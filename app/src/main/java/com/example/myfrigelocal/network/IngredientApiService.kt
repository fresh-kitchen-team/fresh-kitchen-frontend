package com.example.myfrigelocal.network

import retrofit2.http.GET

// ───────────────────────────────────────────
// 식재료 API 인터페이스
// ───────────────────────────────────────────
interface IngredientApiService {

    // GET /api/v1/ingredients — 전체 식재료 목록
    @GET("api/v1/ingredients")
    suspend fun getIngredients(): ApiResponse<List<IngredientDto>>
}