package com.example.myfrigelocal.navigation

/**
 * Centralizes Scan feature navigation routes + SavedStateHandle keys.
 * Keeps string usage consistent and avoids typos across screens.
 */
object ScanNav {
    const val routeResult = "scan_result"

    const val keyImageUri = "scan_image_uri"
    const val keyBarcodeValue = "scan_barcode_value"
    const val keyReset = "scan_reset"

    // Receipt scan: sequential multi-item processing
    const val keyReceiptItems = "receipt_items"
    const val keyReceiptIndex = "receipt_index"
}

