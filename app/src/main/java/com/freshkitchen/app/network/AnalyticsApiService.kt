package com.freshkitchen.app.network

import retrofit2.http.GET
import retrofit2.http.Query

// ───────────────────────────────────────────
// 소비 분석 API 인터페이스
// ───────────────────────────────────────────
interface AnalyticsApiService {

    // GET /api/v1/analytics/summary
    //  - overallDiscardRate, categoryStats[].discardRate (카테고리별 폐기율 막대)
    @GET("api/v1/analytics/summary")
    suspend fun getAnalyticsSummary(): ApiResponse<AnalyticsSummaryData>

    // GET /api/v1/analytics/expiring-items?maxDDay={N}&storageType={FRIDGE|FREEZER|PANTRY}
    //  - 카테고리별 폐기율 막대 그래프 / 임박 식재료 chip 에 사용
    @GET("api/v1/analytics/expiring-items")
    suspend fun getExpiringItems(
        @Query("maxDDay") maxDDay: Int? = null,
        @Query("storageType") storageType: String? = null,
    ): ApiResponse<List<ExpiringItemDto>>
}
