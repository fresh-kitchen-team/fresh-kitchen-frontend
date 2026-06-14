package com.freshkitchen.app.data.scan

import com.google.gson.annotations.SerializedName

/**
 * Swagger 공통 래퍼 `{ status, code, message, data }`.
 * Gson 제네릭 역직렬화 이슈를 피하기 위해 Retrofit 응답은 구체 타입
 * [IngredientImageScanApiResponse], [ReceiptImageScanApiResponse]를 사용합니다.
 */

/** POST /api/v1/scan/ingredient-image */
data class IngredientImageScanApiResponse(
    val status: Int,
    val code: String? = null,
    val message: String? = null,
    val data: IngredientImageScanData? = null,
)

/** POST /api/v1/scan/receipt-image */
data class ReceiptImageScanApiResponse(
    val status: Int,
    val code: String? = null,
    val message: String? = null,
    val data: ReceiptImageScanData? = null,
)

data class IngredientImageScanData(
    val scanType: String? = null,
    val imageAsset: ScanImageAsset? = null,
    val recognizedItems: List<IngredientRecognizedItem>? = null,
    val createdAt: String? = null,
)

data class ScanImageAsset(
    val imageAssetId: Long? = null,
    val kind: String? = null,
    val storageProvider: String? = null,
    val imageUrl: String? = null,
)

data class IngredientRecognizedItem(
    val name: String? = null,
    val category: String? = null,
    val confidence: Double? = null,
)

data class ReceiptImageScanData(
    val scanType: String? = null,
    val imageAsset: ScanImageAsset? = null,
    val purchasedAt: String? = null,
    @SerializedName(value = "purchasedAtSourceType", alternate = ["PurchasedAtSourceType"])
    val purchasedAtSourceType: String? = null,
    val recognizedItems: List<ReceiptRecognizedItem>? = null,
    val createdAt: String? = null,
)

data class ReceiptRecognizedItem(
    val name: String? = null,
    val category: String? = null,
    val registeredAt: String? = null,
)

/** POST /api/v1/scan/fridge-image */
data class FridgeImageScanApiResponse(
    val status: Int,
    val code: String? = null,
    val message: String? = null,
    val data: FridgeImageScanData? = null,
)

data class FridgeImageScanData(
    val scanType: String? = null,
    val imageAsset: ScanImageAsset? = null,
    val detectedItems: List<FridgeDetectedItem>? = null,
    val createdAt: String? = null,
)

data class FridgeDetectedItem(
    val name: String? = null,
    val category: String? = null,
)
