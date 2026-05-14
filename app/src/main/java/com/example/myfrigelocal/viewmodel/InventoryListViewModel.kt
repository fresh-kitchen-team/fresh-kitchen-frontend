package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.network.IngredientDto
import com.example.myfrigelocal.network.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ───────────────────────────────────────────
// 식재료 상태 (유통기한 기반)
// ───────────────────────────────────────────
enum class FoodStatus {
    FRESH,       // 신선 (4일 이상)
    NEAR_EXPIRY, // 소비임박 (0~3일)
    EXPIRED      // 유통기한경과
}

// ───────────────────────────────────────────
// 저장 공간
// ───────────────────────────────────────────
enum class StorageType {
    ALL, FRIDGE, FREEZER, PANTRY
}

// ───────────────────────────────────────────
// 필터 탭
// ───────────────────────────────────────────
enum class InventoryFilter {
    ALL, FRIDGE, FREEZER, PANTRY, RECENT, NEAR_EXPIRY, EXPIRED
}

// ───────────────────────────────────────────
// 식재료 데이터 모델 (UI용)
// ───────────────────────────────────────────
data class FoodItem(
    val id: Int,
    val name: String,
    val category: String,
    val storage: StorageType,
    val amount: String,
    val expiryDate: String,
    val status: FoodStatus,
    val emoji: String,
    val purchaseDate: String = "",
    val memo: String = ""
)

// ───────────────────────────────────────────
// 화면 UI 상태
// ───────────────────────────────────────────
data class InventoryListUiState(
    val selectedFilter: InventoryFilter = InventoryFilter.ALL,
    val allItems: List<FoodItem> = emptyList(),
    val filteredItems: List<FoodItem> = emptyList(),
    val totalCount: Int = 0,
    val freshCount: Int = 0,
    val nearExpiryCount: Int = 0,
    val expiredCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ───────────────────────────────────────────
// InventoryListViewModel
// ───────────────────────────────────────────
class InventoryListViewModel(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val repository: IngredientRepository = IngredientRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryListUiState())
    val uiState: StateFlow<InventoryListUiState> = _uiState.asStateFlow()

    private var allItems = mutableListOf<FoodItem>()

    init {
        val filterKey = savedStateHandle.get<String>("filter") ?: "all"
        val initialFilter = filterKey.toInventoryFilter()
        loadIngredients(initialFilter)
    }

    fun onFilterSelected(filter: InventoryFilter) {
        updateState(filter)
    }

    fun updateItem(updatedItem: FoodItem) {
        val index = allItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            allItems[index] = updatedItem
            updateState(_uiState.value.selectedFilter)
        }
    }

    private fun loadIngredients(initialFilter: InventoryFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val dtos = repository.getIngredients()

            if (dtos.isNotEmpty()) {
                // ACTIVE 상태(삭제/소비 안 된)만 표시
                allItems = dtos
                    .filter { it.status == "ACTIVE" }
                    .map { it.toFoodItem() }
                    .toMutableList()
                updateState(initialFilter)
            } else {
                _uiState.value = InventoryListUiState(
                    isLoading = false,
                    error = "데이터를 불러오지 못했어요."
                )
            }
        }
    }

    private fun updateState(filter: InventoryFilter) {
        val filtered = when (filter) {
            InventoryFilter.ALL      -> allItems
            InventoryFilter.FRIDGE   -> allItems.filter { it.storage == StorageType.FRIDGE }
            InventoryFilter.FREEZER  -> allItems.filter { it.storage == StorageType.FREEZER }
            InventoryFilter.PANTRY   -> allItems.filter { it.storage == StorageType.PANTRY }
            InventoryFilter.RECENT   -> allItems.takeLast(5)
            InventoryFilter.NEAR_EXPIRY -> allItems.filter { it.status == FoodStatus.NEAR_EXPIRY }
            InventoryFilter.EXPIRED  -> allItems.filter { it.status == FoodStatus.EXPIRED }
        }

        _uiState.value = InventoryListUiState(
            selectedFilter = filter,
            allItems = allItems,
            filteredItems = filtered,
            totalCount = filtered.size,
            freshCount = filtered.count { it.status == FoodStatus.FRESH },
            nearExpiryCount = filtered.count { it.status == FoodStatus.NEAR_EXPIRY },
            expiredCount = filtered.count { it.status == FoodStatus.EXPIRED },
            isLoading = false
        )
    }
}

// ───────────────────────────────────────────
// 확장 함수: IngredientDto → FoodItem 변환
// ───────────────────────────────────────────
private fun IngredientDto.toFoodItem(): FoodItem {
    val storage = when (storageType) {
        "FREEZER" -> StorageType.FREEZER
        "PANTRY"  -> StorageType.PANTRY
        else      -> StorageType.FRIDGE
    }

    // 유통기한 기반 신선도 계산
    val foodStatus = try {
        val expiry = LocalDate.parse(expiresAt)
        val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiry)
        when {
            daysLeft < 0 -> FoodStatus.EXPIRED
            daysLeft <= 3 -> FoodStatus.NEAR_EXPIRY
            else -> FoodStatus.FRESH
        }
    } catch (e: Exception) {
        FoodStatus.FRESH
    }

    return FoodItem(
        id = ingredientId.toInt(),
        name = name,
        category = catalogCategory ?: catalogName ?: "기타",
        storage = storage,
        amount = "",                          // 백엔드 미지원 필드
        expiryDate = expiresAt,
        status = foodStatus,
        emoji = emoji ?: "🍽️",               // 카탈로그 이모지 없으면 기본값
        purchaseDate = registeredAt ?: "",
        memo = note ?: ""
    )
}

// ───────────────────────────────────────────
// 확장 함수: filterKey → InventoryFilter 변환
// ───────────────────────────────────────────
private fun String.toInventoryFilter() = when (this) {
    "fridge"     -> InventoryFilter.FRIDGE
    "freezer"    -> InventoryFilter.FREEZER
    "pantry"     -> InventoryFilter.PANTRY
    "recent"     -> InventoryFilter.RECENT
    "near_expiry" -> InventoryFilter.NEAR_EXPIRY
    "expired"    -> InventoryFilter.EXPIRED
    else         -> InventoryFilter.ALL
}