package com.freshkitchen.app.data.scan

/** POST /api/v1/items — Swagger `storageType`: FRIDGE | FREEZER | PANTRY */
fun normalizeStorageTypeForApi(raw: String?): String {
    val t = raw?.trim().orEmpty().uppercase()
    return when {
        t == "FRIDGE" || t == "FREEZER" || t == "PANTRY" -> t
        t.contains("냉장") -> "FRIDGE"
        t.contains("냉동") -> "FREEZER"
        t.contains("팬트리") || t.contains("실온") || t.contains("상온") -> "PANTRY"
        else -> ScanResultItemUiModel.DEFAULT_STORAGE
    }
}
