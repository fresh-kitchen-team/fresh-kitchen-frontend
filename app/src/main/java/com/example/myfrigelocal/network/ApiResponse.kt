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
// GET /api/v1/ingredients 응답 데이터
// ───────────────────────────────────────────
data class IngredientDto(
    val ingredientId: Long,
    val userId: Long,
    val storageId: Long,
    val storageName: String?,
    val storageType: String,        // "FRIDGE" | "FREEZER" | "PANTRY"
    val catalogId: Long?,
    val catalogName: String?,
    val catalogCategory: String?,   // 카테고리 (null 가능)
    val name: String,
    val registeredAt: String?,      // 구매일 "2026-05-13"
    val expiresAt: String,          // 유통기한 "2026-05-28"
    val expirySourceType: String?,
    val status: String,             // "ACTIVE" | "CONSUMED" | "DISCARDED" (lifecycle)
    val consumedAt: String?,
    val discardedAt: String?,
    val note: String?,              // 메모
    val sourceType: String?,
    val emoji: String?              // 카탈로그 이모지 (null 가능)
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