package com.freshkitchen.app.network

import retrofit2.http.POST

// ───────────────────────────────────────────
// 개발 환경 전용 API (운영 비활성화)
// ───────────────────────────────────────────
interface DevApiService {

    // POST /api/v1/dev/notifications/expiring
    // 스케줄러 cron을 기다리지 않고 즉시 1회 실행
    @POST("api/v1/dev/notifications/expiring")
    suspend fun triggerExpiryNotification(): ApiResponse<Any>
}
