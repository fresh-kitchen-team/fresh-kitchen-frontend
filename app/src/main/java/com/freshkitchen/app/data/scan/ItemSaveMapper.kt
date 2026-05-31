package com.freshkitchen.app.data.scan

/**
 * 스캔 결과 화면의 보관장소 표시 문자열과 GET /items/storages 목록을 맞춰 [storageId]를 고릅니다.
 */
fun resolveStorageIdForSave(
    storages: List<StorageListItemDto>,
    displayName: String,
    fallbackStorageTypeCode: String,
): Long? {
    val t = displayName.trim()
    if (t.isNotEmpty()) {
        storages.firstOrNull { row -> row.name.trim() == t }?.let { return it.storageId }
    }
    val typeGuess =
        when {
            t.contains("냉장") -> "FRIDGE"
            t.contains("냉동") -> "FREEZER"
            t.contains("실온") || t.contains("상온") -> "PANTRY"
            t.contains("팬트리") -> "PANTRY"
            else -> fallbackStorageTypeCode.trim().uppercase().ifEmpty { "FRIDGE" }
        }
    storages.firstOrNull { it.storageType.equals(typeGuess, ignoreCase = true) }?.let { return it.storageId }
    return storages.firstOrNull()?.storageId
}
