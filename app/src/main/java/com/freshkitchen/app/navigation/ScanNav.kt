package com.freshkitchen.app.navigation

/**
 * Centralizes Scan feature navigation routes + SavedStateHandle keys.
 * Keeps string usage consistent and avoids typos across screens.
 */
object ScanNav {
    const val routeResult = "scan_result"

    const val keyImageUri = "scan_image_uri"
    const val keyBarcodeValue = "scan_barcode_value"
    const val keyReset = "scan_reset"
    /** Cancel/reset event token (monotonic timestamp). */
    const val keyResetAt = "scan_reset_at"
    /** Which scan tab to return to after cancel. */
    const val keyReturnTab = "scan_return_tab"

    // Receipt scan: sequential multi-item processing
    const val keyReceiptItems = "receipt_items"
    const val keyReceiptIndex = "receipt_index"

    /** First suggested name from ingredient image scan API (optional). */
    const val keyIngredientSuggestion = "scan_ingredient_suggestion"

    /** [com.freshkitchen.app.data.scan.ScanResultUiModel] JSON from scan API (preferred). */
    const val keyScanResultJson = "scan_result_json"

    /** 홈 요약 재조회 트리거 (저장 후 홈 이동 시). */
    const val keyRefreshHome = "home_refresh"
}

