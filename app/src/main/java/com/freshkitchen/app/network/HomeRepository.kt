package com.freshkitchen.app.network

// ───────────────────────────────────────────
// 홈 데이터 레포지토리
// ───────────────────────────────────────────
class HomeRepository(
    private val api: HomeApiService,
) {
    suspend fun getHomeSummary(): Result<HomeSummaryData> {
        return try {
            val response = api.getHomeSummary()
            if (response.code == "COMMON-200" && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("API 오류: ${response.code}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}