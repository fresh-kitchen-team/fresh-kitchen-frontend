package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 식재료 상태
enum class FoodStatus {
    FRESH,      // 신선
    NEAR_EXPIRY, // 소비임박
    EXPIRED     // 유통기한경과
}

// 저장 공간
enum class StorageType {
    ALL, FRIDGE, FREEZER, PANTRY
}

// 필터 탭
enum class InventoryFilter {
    ALL, FRIDGE, FREEZER, PANTRY, RECENT, NEAR_EXPIRY, EXPIRED
}

// 식재료 데이터 모델
data class FoodItem(
    val id: Int,
    val name: String,
    val category: String,
    val storage: StorageType,
    val amount: String,
    val expiryDate: String,
    val status: FoodStatus,
    val emoji: String
)

// 화면 UI 상태
data class InventoryListUiState(
    val selectedFilter: InventoryFilter = InventoryFilter.ALL,
    val allItems: List<FoodItem> = emptyList(),
    val filteredItems: List<FoodItem> = emptyList(),
    val totalCount: Int = 0,
    val freshCount: Int = 0,
    val nearExpiryCount: Int = 0,
    val expiredCount: Int = 0,
    val isLoading: Boolean = false
)

class InventoryListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryListUiState())
    val uiState: StateFlow<InventoryListUiState> = _uiState.asStateFlow()

    // 더미 데이터 (실제 API 연동 시 교체)
    private val dummyItems = listOf(
        FoodItem(1, "신선한 우유", "유제품", StorageType.FRIDGE, "1L", "2026-03-25", FoodStatus.FRESH, "🥛"),
        FoodItem(2, "소고기 안심", "육류", StorageType.FREEZER, "500g", "2026-04-15", FoodStatus.FRESH, "🥩"),
        FoodItem(3, "유기농 브로콜리", "채소", StorageType.FRIDGE, "1개", "2026-03-28", FoodStatus.FRESH, "🥦"),
        FoodItem(4, "계란", "유제품", StorageType.FRIDGE, "10개", "2026-03-24", FoodStatus.NEAR_EXPIRY, "🥚"),
        FoodItem(5, "김치", "발효식품", StorageType.FRIDGE, "500g", "2026-04-20", FoodStatus.FRESH, "🥬"),
        FoodItem(6, "토마토", "채소", StorageType.FRIDGE, "5개", "2026-03-23", FoodStatus.NEAR_EXPIRY, "🍅"),
        FoodItem(7, "햄", "육류", StorageType.FRIDGE, "300g", "2026-03-20", FoodStatus.EXPIRED, "🍖"),
        FoodItem(8, "요거트", "유제품", StorageType.FRIDGE, "4개", "2026-03-21", FoodStatus.EXPIRED, "🫙"),
        FoodItem(9, "바나나", "과일", StorageType.PANTRY, "5개", "2026-03-24", FoodStatus.NEAR_EXPIRY, "🍌"),
        FoodItem(10, "참치캔", "가공식품", StorageType.PANTRY, "3개", "2027-01-01", FoodStatus.FRESH, "🐟"),
    )

    init {
        loadItems()
    }

    private fun loadItems() {
        updateState(InventoryFilter.ALL)
    }

    fun onFilterSelected(filter: InventoryFilter) {
        updateState(filter)
    }

    private fun updateState(filter: InventoryFilter) {
        val filtered = when (filter) {
            InventoryFilter.ALL -> dummyItems
            InventoryFilter.FRIDGE -> dummyItems.filter { it.storage == StorageType.FRIDGE }
            InventoryFilter.FREEZER -> dummyItems.filter { it.storage == StorageType.FREEZER }
            InventoryFilter.PANTRY -> dummyItems.filter { it.storage == StorageType.PANTRY }
            InventoryFilter.RECENT -> dummyItems.takeLast(5)
            InventoryFilter.NEAR_EXPIRY -> dummyItems.filter { it.status == FoodStatus.NEAR_EXPIRY }
            InventoryFilter.EXPIRED -> dummyItems.filter { it.status == FoodStatus.EXPIRED }
        }

        _uiState.value = InventoryListUiState(
            selectedFilter = filter,
            allItems = dummyItems,
            filteredItems = filtered,
            totalCount = filtered.size,
            freshCount = filtered.count { it.status == FoodStatus.FRESH },
            nearExpiryCount = filtered.count { it.status == FoodStatus.NEAR_EXPIRY },
            expiredCount = filtered.count { it.status == FoodStatus.EXPIRED }
        )
    }
}