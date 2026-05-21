package com.example.myfrigelocal.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// ???????????????????????????????????????????
// ÿÿÿÿÿÿÿ API ÿÿÿ?ÿÿÿÿÿÿÿÿÿ
// ???????????????????????????????????????????
interface IngredientApiService {

    // POST /api/v1/items ÿ ÿÿÿÿÿÿÿ ??
    @POST("api/v1/items")
    suspend fun addItem(
        @Body request: ItemCreateRequest
    ): ApiResponse<ItemCreateResponse>

    // GET /api/v1/items ÿ ?? ÿÿÿÿÿÿÿ ÿÿÿÿÿ
    @GET("api/v1/items")
    suspend fun getIngredients(): ApiResponse<List<ItemDto>>

    // PATCH /api/v1/items/{id} ÿ ÿÿÿÿÿÿÿ ÿÿÿ?
    @PATCH("api/v1/items/{id}")
    suspend fun updateItem(
        @Path("id") id: Long,
        @Body request: ItemUpdateRequest
    ): ApiResponse<Void>

    // PATCH /api/v1/items/{id}/consume
    //   - Mark item as consumed. NOT counted as disposal in /analytics/summary.
    //   - Used by the "consume complete" button on the consumption recommendation screen.
    @PATCH("api/v1/items/{id}/consume")
    suspend fun consumeItem(
        @Path("id") id: Long
    ): ApiResponse<ItemConsumeResponse>

    // DELETE /api/v1/items/{id}
    //   - Discard item. Counted as disposal in /analytics/summary.
    //   - Use only for actual disposal/expired flows, NOT for "consume complete".
    @DELETE("api/v1/items/{id}")
    suspend fun deleteItem(
        @Path("id") id: Long
    ): ApiResponse<Void>
}