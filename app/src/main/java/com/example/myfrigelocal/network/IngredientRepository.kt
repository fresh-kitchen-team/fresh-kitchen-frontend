package com.example.myfrigelocal.network

import android.util.Log
import com.example.myfrigelocal.logging.ApiLog

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
            response.code == "COMMON-201"
        } catch (e: Exception) {
            Log.e("IngredientRepo", "addItem 예외: ${e.message}, name=${request.name}")
            false
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

    /**
     * PATCH /api/v1/items/{id}/consume
     *
     * 식재료를 "소비 처리"한다. 폐기율(/analytics/summary)에는 반영되지 않는다.
     * 소비 권장 알림 화면의 "소비 완료" 버튼이 호출한다.
     */
    suspend fun consumeItem(id: Long): Boolean {
        val sub = "Items:Consume"
        ApiLog.i(sub, "PATCH /api/v1/items/$id/consume START")
        return try {
            val response = api.consumeItem(id)
            ApiLog.i(
                sub,
                "envelope status=${response.status} code=${response.code} " +
                    "message=${response.message} consumedAt=${response.data?.consumedAt}",
            )
            val ok = response.code == "COMMON-200"
            if (!ok) {
                ApiLog.w(sub, "비정상 응답 code=${response.code} message=${response.message}")
            } else {
                ApiLog.i(sub, "OK id=$id 소비 처리 성공 consumedAt=${response.data?.consumedAt}")
            }
            ok
        } catch (e: Exception) {
            ApiLog.e(sub, "PATCH /api/v1/items/$id/consume exception: ${e.message}", e)
            false
        }
    }

    /**
     * DELETE /api/v1/items/{id}
     *
     * 식재료를 실제 폐기 처리한다. 폐기율(/analytics/summary)에 반영된다.
     * "소비 완료" 가 아닌, 만료/폐기 흐름에서만 사용해야 한다.
     */
    suspend fun deleteItem(id: Long): Boolean {
        val sub = "Items:Delete"
        ApiLog.i(sub, "DELETE /api/v1/items/$id START")
        return try {
            val response = api.deleteItem(id)
            ApiLog.i(
                sub,
                "envelope status=${response.status} code=${response.code} message=${response.message}",
            )
            val ok = response.code == "COMMON-200"
            if (!ok) {
                ApiLog.w(sub, "비정상 응답 code=${response.code} message=${response.message}")
            } else {
                ApiLog.i(sub, "OK id=$id 삭제 성공")
            }
            ok
        } catch (e: Exception) {
            ApiLog.e(sub, "DELETE /api/v1/items/$id exception: ${e.message}", e)
            false
        }
    }
}