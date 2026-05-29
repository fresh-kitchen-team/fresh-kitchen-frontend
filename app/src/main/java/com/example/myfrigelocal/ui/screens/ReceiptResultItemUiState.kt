package com.example.myfrigelocal.ui.screens

import com.example.myfrigelocal.data.scan.ScanResultItemUiModel
import com.example.myfrigelocal.data.scan.ScanResultUiModel
import java.time.LocalDate
import java.util.UUID

/** 영수증 OCR 결과 화면 전용 편집 상태 (UI layer only). */
data class ReceiptResultItemUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = ScanResultItemUiModel.DEFAULT_CATEGORY,
    val storageType: String,
    val expiresAt: String,
    /** 빠른 설정 적용 전 유통기한 — 카드별 초기화에 사용 */
    val initialExpiresAt: String,
    val registeredAt: String?,
)

internal fun todayIsoDate(): String = LocalDate.now().toString()

internal fun ScanResultItemUiModel.toReceiptResultItemUiState(): ReceiptResultItemUiState {
    val expiry = expiresAt?.trim()?.takeIf { it.isNotEmpty() } ?: todayIsoDate()
    return ReceiptResultItemUiState(
        name = name,
        category = category.ifBlank { ScanResultItemUiModel.DEFAULT_CATEGORY },
        storageType = storageType.ifBlank { ScanResultItemUiModel.DEFAULT_STORAGE },
        expiresAt = expiry,
        initialExpiresAt = expiry,
        registeredAt = registeredAt,
    )
}

internal fun buildReceiptListFromScan(model: ScanResultUiModel): List<ReceiptResultItemUiState> =
    model.items.map { it.toReceiptResultItemUiState() }

internal fun buildReceiptListFromLegacyNames(names: List<String>): List<ReceiptResultItemUiState> {
    val today = todayIsoDate()
    return names.map { n ->
        ReceiptResultItemUiState(
            name = n,
            category = ScanResultItemUiModel.DEFAULT_CATEGORY,
            storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
            expiresAt = today,
            initialExpiresAt = today,
            registeredAt = today,
        )
    }
}

internal fun formatPurchasedAtSourceLabel(raw: String?): String? =
    when (raw?.trim()?.uppercase()) {
        null, "" -> null
        "OCR" -> "OCR 인식"
        "DEFAULT_TODAY" -> "기본값"
        "SIMULATED" -> "시뮬레이션"
        else -> raw
    }

internal val StorageTypeOptions =
    listOf(
        "FRIDGE" to "냉장실",
        "FREEZER" to "냉동실",
        "PANTRY" to "팬트리",
    )

internal fun storageTypeToDisplay(code: String): String =
    StorageTypeOptions.firstOrNull { it.first.equals(code, ignoreCase = true) }?.second ?: code
