package com.example.myfrigelocal.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface IngredientApiService {

    @POST("api/v1/items")
    suspend fun addItem(
        @Body request: ItemCreateRequest
    ): ApiResponse<ItemCreateResponse>

    @GET("api/v1/items")
    suspend fun getIngredients(): ApiResponse<List<ItemDto>>

    @GET("api/v1/items/storages")
    suspend fun getStorages(): ApiResponse<List<StorageDto>>

    @PATCH("api/v1/items/{id}")
    suspend fun updateItem(
        @Path("id") id: Long,
        @Body request: ItemUpdateRequest
    ): ApiResponse<Void>

    // PATCH /api/v1/items/{itemId}/consume — ?? ?? (??? ???)
    @PATCH("api/v1/items/{itemId}/consume")
    suspend fun consumeItem(
        @Path("itemId") itemId: Long
    ): ApiResponse<ItemConsumeResponse>

    // DELETE /api/v1/items/{itemId} — ?? ?? (??? ??)
    @DELETE("api/v1/items/{itemId}")
    suspend fun deleteItem(
        @Path("itemId") itemId: Long
    ): ApiResponse<Void>
}
