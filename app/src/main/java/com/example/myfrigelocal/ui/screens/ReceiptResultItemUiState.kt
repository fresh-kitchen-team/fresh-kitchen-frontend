package com.example.myfrigelocal.ui.screens

import com.example.myfrigelocal.data.scan.ScanResultItemUiModel
import com.example.myfrigelocal.data.scan.ScanResultUiModel
import java.time.LocalDate
import java.util.UUID

/** 영수증 OCR 결과 화면 전용 편집 상태 (UI layer only). */
data class ReceiptResultItemUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val storageType: String,
    val expiresAt: String,
    val registeredAt: String?,
)

internal fun todayIsoDate(): String = LocalDate.now().toString()

internal fun ScanResultItemUiModel.toReceiptResultItemUiState(): ReceiptResultItemUiState =
    ReceiptResultItemUiState(
        name = name,
        storageType = storageType.ifBlank { ScanResultItemUiModel.DEFAULT_STORAGE },
        expiresAt = expiresAt?.trim()?.takeIf { it.isNotEmpty() } ?: todayIsoDate(),
        registeredAt = registeredAt,
    )

internal fun buildReceiptListFromScan(model: ScanResultUiModel): List<ReceiptResultItemUiState> =
    model.items.map { it.toReceiptResultItemUiState() }

internal fun buildReceiptListFromLegacyNames(names: List<String>): List<ReceiptResultItemUiState> {
    val today = todayIsoDate()
    return names.map { n ->
        ReceiptResultItemUiState(
            name = n,
            storageType = ScanResultItemUiModel.DEFAULT_STORAGE,
            expiresAt = today,
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
