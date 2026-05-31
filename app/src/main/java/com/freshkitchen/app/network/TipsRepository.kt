package com.freshkitchen.app.network

import com.freshkitchen.app.logging.ApiLog

// ───────────────────────────────────────────
// 분석 / 팁 데이터 레포지토리
//   - Logcat 필터: `tag:MyFridgeApi` 또는 `[Tips] / [Tips:Recycling]` 키워드
// ───────────────────────────────────────────
class TipsRepository(
    private val api: TipsApiService = RetrofitClient.tipsApi
) {

    suspend fun getStorageTips(category: String? = null): List<StorageTipDto>? {
        val sub = "Tips:Storage"
        ApiLog.i(sub, "GET /api/v1/tips/storage START category=${category ?: "(all)"}")
        return try {
            val response = api.getStorageTips(category)
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
            ApiLog.e(sub, "GET /api/v1/tips/storage exception: ${e.message}", e)
            null
        }
    }

    suspend fun getRecyclingTips(): List<RecyclingTipDto>? {
        val sub = "Tips:Recycling"
        ApiLog.i(sub, "GET /api/v1/tips/recycling START")
        return try {
            val response = api.getRecyclingTips()
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
            ApiLog.e(sub, "GET /api/v1/tips/recycling exception: ${e.message}", e)
            null
        }
    }
}
