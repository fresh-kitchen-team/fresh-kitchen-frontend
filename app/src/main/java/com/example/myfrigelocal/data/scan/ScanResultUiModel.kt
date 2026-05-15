package com.example.myfrigelocal.data.scan

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * 스캔 결과 화면용 UI 모델 (SavedStateHandle JSON 직렬화).
 *
 * @param sourceType `PHOTO` = 식재료 이미지 스캔, `RECEIPT` = 영수증 OCR.
 */
data class ScanResultUiModel(
    val sourceType: String,
    val localPreviewImageUri: String?,
    val remotePreviewImageUrl: String?,
    val items: List<ScanResultItemUiModel>,
    val purchasedAt: String? = null,
    val purchasedAtSourceType: String? = null,
)

data class ScanResultItemUiModel(
    val name: String,
    val category: String,
    val storageType: String,
    val registeredAt: String?,
    val expiresAt: String?,
    val confidence: Double?,
) {
    companion object {
        const val DEFAULT_CATEGORY = "ETC"
        const val DEFAULT_STORAGE = "FRIDGE"
    }
}

private val scanResultGson: Gson =
    GsonBuilder()
        .serializeNulls()
        .create()

fun ScanResultUiModel.toJson(): String = scanResultGson.toJson(this)

fun parseScanResultUiModel(json: String?): ScanResultUiModel? =
    runCatching {
        if (json.isNullOrBlank()) return null
        scanResultGson.fromJson(json, ScanResultUiModel::class.java)
    }.getOrNull()
