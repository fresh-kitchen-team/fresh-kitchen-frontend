package com.freshkitchen.app.data.scan

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Scan API — multipart 필드명은 Swagger 기준 `file` 만 사용합니다.
 * Base URL은 [com.freshkitchen.app.BuildConfig.SCAN_API_BASE_URL] (trailing `/` 권장).
 */
interface ScanApiService {
    companion object {
        const val INGREDIENT_IMAGE_PATH = "api/v1/scan/ingredient-image"
        const val FRIDGE_IMAGE_PATH = "api/v1/scan/fridge-image"
        const val RECEIPT_IMAGE_PATH = "api/v1/scan/receipt-image"
    }

    @Multipart
    @POST(INGREDIENT_IMAGE_PATH)
    suspend fun scanIngredientImage(
        @Part file: MultipartBody.Part,
    ): IngredientImageScanApiResponse

    @Multipart
    @POST(FRIDGE_IMAGE_PATH)
    suspend fun scanFridgeImage(
        @Part file: MultipartBody.Part,
    ): FridgeImageScanApiResponse

    @Multipart
    @POST(RECEIPT_IMAGE_PATH)
    suspend fun scanReceiptImage(
        @Part file: MultipartBody.Part,
    ): ReceiptImageScanApiResponse
}
