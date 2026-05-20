package com.example.myfrigelocal.network

import com.example.myfrigelocal.logging.ApiLog

// ───────────────────────────────────────────
// 소비 분석 데이터 레포지토리
//   - Logcat 필터: `tag:MyFridgeApi` 또는 `Analytics:Summary` / `Analytics:Expiring`
// ───────────────────────────────────────────
class AnalyticsRepository(
    private val api: AnalyticsApiService = RetrofitClient.analyticsApi
) {

    suspend fun getAnalyticsSummary(): AnalyticsSummaryData? {
        val sub = "Analytics:Summary"
        ApiLog.i(sub, "GET /api/v1/analytics/summary START")
        return try {
            val response = api.getAnalyticsSummary()
            ApiLog.i(
                sub,
                "envelope status=${response.status} code=${response.code} message=${response.message} hasData=${response.data != null}",
            )
            if (response.code == "COMMON-200") {
                val d = response.data
                if (d != null) {
                    ApiLog.i(
                        sub,
                        "OK total=${d.totalCount} fresh=${d.freshCount} near=${d.nearExpiryCount} expired=${d.expiredCount}",
                    )
                }
                d
            } else {
                ApiLog.w(sub, "비정상 응답 code=${response.code} message=${response.message}")
                null
            }
        } catch (e: Exception) {
            ApiLog.e(sub, "GET /api/v1/analytics/summary exception: ${e.message}", e)
            null
        }
    }

    suspend fun getExpiringItems(
        maxDDay: Int? = null,
        storageType: String? = null,
    ): List<ExpiringItemDto>? {
        val sub = "Analytics:Expiring"
        ApiLog.i(
            sub,
            "GET /api/v1/analytics/expiring-items START maxDDay=${maxDDay ?: "(default)"} storageType=${storageType ?: "(all)"}",
        )
        return try {
            val response = api.getExpiringItems(maxDDay = maxDDay, storageType = storageType)
            ApiLog.i(
                sub,
                "envelope status=${response.status} code=${response.code} message=${response.message} dataSize=${response.data?.size ?: 0}",
            )
            if (response.code == "COMMON-200") {
                val list = response.data.orEmpty()
                ApiLog.i(sub, "OK items=${list.size}")
                list
            } else {
                ApiLog.w(sub, "비정상 응답 code=${response.code} message=${response.message}")
                null
            }
        } catch (e: Exception) {
            ApiLog.e(sub, "GET /api/v1/analytics/expiring-items exception: ${e.message}", e)
            null
        }
    }
}
