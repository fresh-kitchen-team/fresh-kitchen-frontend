package com.example.myfrigelocal.data.scan

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** GET /api/v1/items/storages — Swagger `StorageSummaryResponse` 목록 래핑. */
data class ItemStoragesEnvelope(
    val status: Int,
    val code: String? = null,
    val message: String? = null,
    val data: List<StorageListItemDto>? = null,
)

data class StorageListItemDto(
    val storageId: Long,
    val storageType: String,
    val name: String,
)

/** POST /api/v1/items 요청 본문 (imageAssetId·category 없으면 null). */
data class CreateItemRequest(
    val name: String,
    val storageId: Long,
    val expiryDate: String?,
    val purchaseDate: String?,
    val memo: String?,
    val imageAssetId: Long?,
    val category: String? = null,
)

data class CreateItemEnvelope(
    val status: Int,
    val code: String? = null,
    val message: String? = null,
    val data: JsonElement? = null,
)

interface ItemsApiService {
    @GET("api/v1/items/storages")
    suspend fun getStorages(): ItemStoragesEnvelope

    @POST("api/v1/items")
    suspend fun createItem(@Body body: CreateItemRequest): CreateItemEnvelope
}
