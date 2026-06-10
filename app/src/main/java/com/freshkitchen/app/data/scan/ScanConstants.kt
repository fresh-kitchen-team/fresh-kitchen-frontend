package com.freshkitchen.app.data.scan

/**
 * 스캔 결과의 출처(sourceType) 상수.
 *
 * 주의: 이 값들은 [ScanResultUiModel.sourceType] 으로 JSON 직렬화되어
 * `SavedStateHandle` 에 저장/복원되고, 저장 API(`POST /api/v1/items`)의
 * `sourceType` 으로도 전송됩니다. 문자열 값을 변경하면 안 됩니다.
 */
object ScanSourceType {
    /** 식재료 이미지 스캔 + 냉장고 스캔(저장 시 PHOTO 로 전송) */
    const val PHOTO = "PHOTO"

    /** 영수증 OCR 스캔 */
    const val RECEIPT = "RECEIPT"

    /** 냉장고 내부 스캔 (UI 모델 구분용) */
    const val FRIDGE = "FRIDGE"
}
