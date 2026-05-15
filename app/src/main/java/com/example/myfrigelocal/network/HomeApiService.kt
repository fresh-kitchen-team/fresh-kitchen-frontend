package com.example.myfrigelocal.network

import retrofit2.http.GET

// ───────────────────────────────────────────
// 홈 API 인터페이스
// ───────────────────────────────────────────
interface HomeApiService {

    // GET /api/v1/home/summary
    // Authorization: Bearer {accessToken} (OkHttp 인터셉터에서 자동 추가)
    @GET("api/v1/home/summary")
    suspend fun getHomeSummary(): ApiResponse<HomeSummaryData>
}