package com.example.myfrigelocal.network

import retrofit2.http.GET
import retrofit2.http.Query

// ───────────────────────────────────────────
// 분석 / 팁 API 인터페이스
// ───────────────────────────────────────────
interface TipsApiService {

    // GET /api/v1/tips/storage?category={VEGETABLE_FRUIT|DAIRY_DRINK|MEAT_SEAFOOD|ETC}
    // category 가 null 이면 전체 카테고리를 반환
    @GET("api/v1/tips/storage")
    suspend fun getStorageTips(
        @Query("category") category: String? = null
    ): ApiResponse<List<StorageTipDto>>

    // GET /api/v1/tips/recycling
    @GET("api/v1/tips/recycling")
    suspend fun getRecyclingTips(): ApiResponse<List<RecyclingTipDto>>
}
