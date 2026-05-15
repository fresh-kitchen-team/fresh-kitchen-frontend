package com.example.myfrigelocal.network

// ───────────────────────────────────────────
// 백엔드 공통 응답 래퍼
// { "status": 200, "code": "COMMON-200", "message": "Success", "data": { ... } }
// ───────────────────────────────────────────
data class ApiResponse<T>(
    val status: Int,
    val code: String,
    val message: String,
    val data: T?
)

// ───────────────────────────────────────────
// GET /api/v1/items 응답 데이터
// ───────────────────────────────────────────
data class ItemDto(
    val id: Long,
    val name: String,
    val status: String,             // "FRESH" | "NEAR_EXPIRY" | "EXPIRED" (신선도 상태)
    val storageId: Long,
    val storage: String,            // "FRIDGE" | "FREEZER" | "PANTRY"
    val category: String?,          // 카테고리 (null 가능)
    val expiryDate: String?,        // 유통기한 "2026-05-28" (null 가능)
    val emoji: String?,             // 카탈로그 이모지 (null 가능)
    val purchaseDate: String?,      // 구매일 "2026-05-13"
    val memo: String?               // 메모
)

// ───────────────────────────────────────────
// PATCH /api/v1/items/{id} 요청 데이터
// ───────────────────────────────────────────
data class ItemUpdateRequest(
    val name: String? = null,
    val expiryDate: String? = null,
    val purchaseDate: String? = null,
    val memo: String? = null
)

// ───────────────────────────────────────────
// GET /api/v1/home/summary 응답 데이터
// ───────────────────────────────────────────
data class HomeSummaryData(
    val totalCount: Int,
    val freshCount: Int,
    val nearExpiryCount: Int,
    val expiredCount: Int,
    val storages: List<StorageSummaryDto>,
    val nearExpiryItems: List<RecentItemDto>,
    val expiredItems: List<RecentItemDto>,
    val recentItems: List<RecentItemDto>
)

data class StorageSummaryDto(
    val storage: String,    // "FRIDGE" | "FREEZER" | "PANTRY"
    val emoji: String,
    val name: String,
    val itemCount: Int,
    val filterKey: String   // "fridge" | "freezer" | "pantry"
)

data class RecentItemDto(
    val id: Long,
    val name: String,
    val storage: String,
    val expiryDate: String,
    val status: String,     // "FRESH" | "NEAR_EXPIRY" | "EXPIRED"
    val emoji: String
)