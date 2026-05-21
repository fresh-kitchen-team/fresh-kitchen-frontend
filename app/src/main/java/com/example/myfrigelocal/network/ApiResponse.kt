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
// GET /api/v1/items/storages 응답 데이터
// ───────────────────────────────────────────
data class StorageDto(
    val storageId: Long,
    val storageType: String?,  // "FRIDGE" | "FREEZER" | "PANTRY"
    val name: String
)

// ───────────────────────────────────────────
// POST /api/v1/items 요청 데이터
// ───────────────────────────────────────────
data class ItemCreateRequest(
    val name: String,
    val catalogId: Long? = null,   // 시연용 catalog seed ID (null 허용)
    val storageId: Long,
    val expiryDate: String? = null,
    val purchaseDate: String? = null,
    val memo: String? = null
)

data class ItemCreateResponse(
    val id: Long
)

// ───────────────────────────────────────────
// PATCH /api/v1/items/{id} 요청 데이터
// ───────────────────────────────────────────
data class ItemUpdateRequest(
    val name: String? = null,
    val category: String? = null,
    val expiryDate: String? = null,
    val purchaseDate: String? = null,
    val memo: String? = null,
    val storageId: Long? = null
)

// ───────────────────────────────────────────
// PATCH /api/v1/items/{id}/consume 응답 데이터
//   - 소비 처리(폐기율에는 반영되지 않음)
// ───────────────────────────────────────────
data class ItemConsumeResponse(
    val consumedAt: String?
)

// ───────────────────────────────────────────
// GET /api/v1/home/summary 응답 데이터
// ───────────────────────────────────────────
// home/summary 와 analytics/summary 가 동일한 SummaryResponse 스키마 사용
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

// ───────────────────────────────────────────
// GET /api/v1/tips/storage 응답 데이터
// ───────────────────────────────────────────
data class StorageTipDto(
    val id: Long,
    val displayCategory: String,        // "VEGETABLE_FRUIT" | "DAIRY_DRINK" | "MEAT_SEAFOOD" | "ETC"
    val displayCategoryName: String,    // "채소/과일" 등 화면 표시용 라벨
    val name: String,                   // 팁 제목
    val emoji: String,                  // 이모지 (예: "🥦")
    val tip: String,                    // 팁 본문
    val storageType: String             // "FRIDGE" | "FREEZER" | "PANTRY" 등
)

// ───────────────────────────────────────────
// GET /api/v1/tips/recycling 응답 데이터
// ───────────────────────────────────────────
data class RecyclingTipDto(
    val id: Long,
    val name: String,           // 품목 이름 (예: "달걀 껍데기")
    val wasteType: String,      // "일반쓰레기" | "음식물쓰레기" 등
    val description: String     // 설명
)

// ───────────────────────────────────────────
// GET /api/v1/analytics/summary 응답 데이터
//   - overallDiscardRate: 전체 폐기율 (%)
//   - categoryStats[].discardRate: 카테고리별 폐기율 (%) → 막대 그래프
//   - consume 은 폐기율에 반영되지 않고, delete 만 반영 (백엔드 집계)
// ───────────────────────────────────────────
data class AnalyticsSummaryData(
    val totalActiveCount: Int,
    val urgentCount: Int,
    val topUrgentCategory: String?,
    val topUrgentCategoryDisplayName: String?,
    val topUrgentCategoryCount: Int,
    val message: String?,
    val overallDiscardRate: Double,
    val categoryStats: List<AnalyticsCategoryStatDto>?,
    val urgentItems: List<AnalyticsUrgentItemDto>?,
)

data class AnalyticsCategoryStatDto(
    val category: String,
    val displayName: String,
    val activeCount: Int,
    val urgentCount: Int,
    val discardRate: Double,
)

data class AnalyticsUrgentItemDto(
    val id: Long,
    val name: String,
    val emoji: String?,
    val category: String?,
    val categoryDisplayName: String?,
    val expiresAt: String?,
    val dday: Int?,
    val storageType: String?,
)

// ───────────────────────────────────────────
// GET /api/v1/analytics/expiring-items 응답 데이터
//   - maxDDay 가 null 이면 백엔드 기본값(10) 사용
// ───────────────────────────────────────────
data class ExpiringItemDto(
    val id: Long,
    val name: String,
    val emoji: String?,
    val category: String,                  // "VEGETABLE_FRUIT" | "DAIRY_DRINK" | "MEAT_SEAFOOD" | "ETC"
    val categoryDisplayName: String?,      // 화면 표시용 라벨 (예: "채소/과일")
    val expiresAt: String?,
    val dday: Int,
    val storageType: String                // "FRIDGE" | "FREEZER" | "PANTRY"
)