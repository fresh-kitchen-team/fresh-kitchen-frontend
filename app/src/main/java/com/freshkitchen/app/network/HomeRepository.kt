package com.freshkitchen.app.network

// ───────────────────────────────────────────
// 홈 데이터 레포지토리
// ───────────────────────────────────────────
class HomeRepository(
    private val api: HomeApiService = RetrofitClient.homeApi
) {
    suspend fun getHomeSummary(): HomeSummaryData? {
        return try {
            val response = api.getHomeSummary()
            if (response.code == "COMMON-200") response.data else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}