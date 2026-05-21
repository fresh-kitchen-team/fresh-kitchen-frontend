package com.example.myfrigelocal.network

import android.util.Log

// ───────────────────────────────────────────
// 식재료 데이터 레포지토리
// ───────────────────────────────────────────
class IngredientRepository(
    private val api: IngredientApiService = RetrofitClient.ingredientApi
) {
    suspend fun getIngredients(): List<ItemDto> {
        return try {
            val response = api.getIngredients()
            if (response.code == "COMMON-200") response.data ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addItem(request: ItemCreateRequest): Boolean {
        return try {
            val response = api.addItem(request)
            Log.d("IngredientRepo", "addItem 응답 code=${response.code}, name=${request.name}")
            response.code == "COMMON-200"
        } catch (e: Exception) {
            Log.e("IngredientRepo", "addItem 예외: ${e.message}, name=${request.name}")
            false
        }
    }

    suspend fun getStorages(): List<StorageDto> {
        return try {
            val response = api.getStorages()
            if (response.code == "COMMON-200") response.data ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            Log.e("IngredientRepo", "getStorages 예외: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateItem(id: Long, request: ItemUpdateRequest): Boolean {
        return try {
            val response = api.updateItem(id, request)
            response.code == "COMMON-200"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}