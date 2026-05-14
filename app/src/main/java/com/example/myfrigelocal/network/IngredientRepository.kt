package com.example.myfrigelocal.network

// ───────────────────────────────────────────
// 식재료 데이터 레포지토리
// ───────────────────────────────────────────
class IngredientRepository(
    private val api: IngredientApiService = RetrofitClient.ingredientApi
) {
    suspend fun getIngredients(): List<IngredientDto> {
        return try {
            val response = api.getIngredients()
            if (response.code == "COMMON-200") response.data ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}