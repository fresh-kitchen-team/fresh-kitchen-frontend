package com.example.myfrigelocal.data.scan

import java.time.LocalDate

private fun todayIsoDate(): String = LocalDate.now().toString()

private fun firstDateOnly(isoDateTime: String?): String? {
    if (isoDateTime.isNullOrBlank()) return null
    val s = isoDateTime.trim()
    if (s.length >= 10 && s[4] == '-' && s[7] == '-') {
        return s.take(10)
    }
    return runCatching { LocalDate.parse(s.take(10)).toString() }.getOrNull()
}

fun mapIngredientScanToUiModel(
    data: IngredientImageScanData,
    localCapturedImageUri: String?,
): ScanResultUiModel {
    val remoteUrl = data.imageAsset?.imageUrl?.trim()?.takeIf { it.isNotEmpty() }
    val createdDay = firstDateOnly(data.createdAt) ?: todayIsoDate()
    val items =
        data.recognizedItems.orEmpty().mapNotNull { row ->
            val n = row.name?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            ScanResultItemUiModel(
                name = n,
                category = ScanResultItemUiModel.DEFAULT_CATEGORY,
                storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                registeredAt = createdDay,
                expiresAt = null,
                confidence = row.confidence,
            )
        }
    return ScanResultUiModel(
        sourceType = "PHOTO",
        localPreviewImageUri = localCapturedImageUri,
        remotePreviewImageUrl = remoteUrl,
        items = items,
        purchasedAt = null,
        purchasedAtSourceType = null,
        imageAssetId = data.imageAsset?.imageAssetId,
    )
}

fun mapReceiptScanToUiModel(
    data: ReceiptImageScanData,
    localCapturedImageUri: String?,
): ScanResultUiModel {
    val purchased = data.purchasedAt?.trim()?.takeIf { it.isNotEmpty() }
    val purchasedDay = firstDateOnly(purchased) ?: purchased
    val items =
        data.recognizedItems.orEmpty().mapNotNull { row ->
            val n = row.name?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val reg =
                firstDateOnly(row.registeredAt)
                    ?: purchasedDay
                    ?: firstDateOnly(data.createdAt)
                    ?: todayIsoDate()
            ScanResultItemUiModel(
                name = n,
                category = ScanResultItemUiModel.DEFAULT_CATEGORY,
                storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                registeredAt = reg,
                expiresAt = row.estimatedExpiresAt?.trim()?.takeIf { it.isNotEmpty() },
                confidence = row.confidence,
            )
        }
    val effectiveItems =
        items.ifEmpty {
            listOf(
                ScanResultItemUiModel(
                    name = "인식된 품목이 없습니다. 이름을 직접 입력해 주세요.",
                    category = ScanResultItemUiModel.DEFAULT_CATEGORY,
                    storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                    registeredAt = purchasedDay ?: firstDateOnly(data.createdAt) ?: todayIsoDate(),
                    expiresAt = null,
                    confidence = null,
                ),
            )
        }
    return ScanResultUiModel(
        sourceType = "RECEIPT",
        localPreviewImageUri = localCapturedImageUri,
        remotePreviewImageUrl = null,
        items = effectiveItems,
        purchasedAt = purchased,
        purchasedAtSourceType =
            data.purchasedAtSourceType?.trim()?.takeIf { it.isNotEmpty() }
                ?: data.sourceType?.trim()?.takeIf { it.isNotEmpty() },
        imageAssetId = null,
    )
}

fun simulatedReceiptUiModel(localUri: String?): ScanResultUiModel {
    val names = listOf("신선한 우유", "사과", "돼지고기")
    val today = todayIsoDate()
    return ScanResultUiModel(
        sourceType = "RECEIPT",
        localPreviewImageUri = localUri,
        remotePreviewImageUrl = null,
        items =
            names.map { n ->
                ScanResultItemUiModel(
                    name = n,
                    category = ScanResultItemUiModel.DEFAULT_CATEGORY,
                    storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                    registeredAt = today,
                    expiresAt = null,
                    confidence = null,
                )
            },
        purchasedAt = today,
        purchasedAtSourceType = "SIMULATED",
        imageAssetId = null,
    )
}

fun simulatedIngredientUiModel(localUri: String?): ScanResultUiModel =
    ScanResultUiModel(
        sourceType = "PHOTO",
        localPreviewImageUri = localUri,
        remotePreviewImageUrl = null,
        items =
            listOf(
                ScanResultItemUiModel(
                    name = "신선한 우유",
                    category = ScanResultItemUiModel.DEFAULT_CATEGORY,
                    storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
                    registeredAt = todayIsoDate(),
                    expiresAt = null,
                    confidence = null,
                ),
            ),
        purchasedAt = null,
        purchasedAtSourceType = null,
        imageAssetId = null,
    )
