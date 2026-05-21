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
// GET /api/v1/items, GET /api/v1/items/{id} 응답 데이터
// ───────────────────────────────────────────
data class ItemDto(
    val id: Long,
    val name: String,
    val status: String,             // "FRESH" | "NEAR_EXPIRY" | "EXPIRED"
    val catalogId: Long?,           // 카탈로그 ID (null 가능)
    val storageId: Long,
    val storage: String,            // "FRIDGE" | "FREEZER" | "PANTRY"
    val category: String?,          // "VEGETABLE"|"FRUIT"|"MEAT"|"SEAFOOD"|"DAIRY"|"SAUCE"|"DRINK"|"ETC"
    val expiryDate: String?,        // 유통기한 "2026-05-28" (null 가능)
    val emoji: String?,             // 이모지 (null 가능)
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
    val storageId: Long,
    val expiryDate: String? = null,
    val purchaseDate: String? = null,
    val memo: String? = null,
    val imageAssetId: Long? = null
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
// GET /api/v1/home/summary 응답 데이터
// ───────────────────────────────────────────
// home/summary 와 analytics/summary 가 동일한 SummaryResponse 스키마 사용
data class HomeSummaryData(
    val totalCount: Int,
    val freshCount: Int,
    val nearExpiryCount: Int,
    val expiredCount: Int,
    val storages: List<StorageDto>,       // storageId, storageType, name
    val nearExpiryItems: List<RecentItemDto>,
    val expiredItems: List<RecentItemDto>,
    val recentItems: List<RecentItemDto>
)

data class RecentItemDto(
    val id: Long,
    val name: String,
    val storage: String,
    val expiryDate: String?,
    val status: String,     // "FRESH" | "NEAR_EXPIRY" | "EXPIRED"
    val emoji: String?
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
//   - totalCount = 현재 보관 중인 식재료 수
//   - expiredCount = 폐기 처리(delete) 된 식재료 수
//     (consume 은 폐기율에 반영되지 않음 - 백엔드 측 집계 룰)
// ───────────────────────────────────────────
// analytics/summary 도 동일한 SummaryResponse 스키마 사용 — HomeSummaryData 와 동일
typealias AnalyticsSummaryData = HomeSummaryData

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