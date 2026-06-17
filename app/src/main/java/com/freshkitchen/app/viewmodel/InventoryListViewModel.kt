package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.network.ItemDto
import com.freshkitchen.app.network.IngredientRepository
import com.freshkitchen.app.network.ItemUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val memo: String = "",
    val storageId: Long = 0L,
    val representativeImage: com.freshkitchen.app.network.RepresentativeImageDto? = null
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
    val error: String? = null,
    // ── 선택 모드 ──
    val isSelectMode: Boolean = false,
    val selectedItemIds: Set<Int> = emptySet(),
    val isProcessing: Boolean = false,
)

// ───────────────────────────────────────────
// InventoryListViewModel
// ───────────────────────────────────────────
@HiltViewModel
class InventoryListViewModel @Inject constructor(
    private val repository: IngredientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryListUiState())
    val uiState: StateFlow<InventoryListUiState> = _uiState.asStateFlow()

    private var allItems = mutableListOf<FoodItem>()

    // StorageType → 서버 storageId 매핑 (로드 시 채워짐)
    private val storageIdMap = mutableMapOf<StorageType, Long>()

    init {
        loadIngredients()
    }

    fun onFilterSelected(filter: InventoryFilter) {
        updateState(filter)
    }

    // ───────────────────────────────────────────
    // 선택 모드 토글
    // ───────────────────────────────────────────
    fun toggleSelectMode() {
        _uiState.value = _uiState.value.copy(
            isSelectMode = !_uiState.value.isSelectMode,
            selectedItemIds = emptySet()
        )
    }

    // 개별 아이템 선택/해제
    fun toggleItemSelection(id: Int) {
        val current = _uiState.value.selectedItemIds
        _uiState.value = _uiState.value.copy(
            selectedItemIds = if (id in current) current - id else current + id
        )
    }

    // ───────────────────────────────────────────
    // 폐기 — DELETE /api/v1/items/{id} (유통기한 경과 아이템)
    // ───────────────────────────────────────────
    fun deleteSelected(onComplete: () -> Unit = {}) {
        val ids = _uiState.value.selectedItemIds.toSet()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val successIds = mutableSetOf<Int>()
            ids.forEach { id ->
                if (repository.deleteItem(id.toLong())) successIds.add(id)
            }
            if (successIds.isNotEmpty()) {
                allItems = allItems.filter { it.id !in successIds }.toMutableList()
            }
            val failCount = ids.size - successIds.size
            updateState(_uiState.value.selectedFilter)
            if (failCount > 0) {
                _uiState.value = _uiState.value.copy(
                    error = "${failCount}개 항목을 삭제하지 못했어요. 다시 시도해주세요."
                )
            }
            onComplete()
        }
    }

    // ───────────────────────────────────────────
    // 소비 — POST /api/v1/items/{id}/consume (유통기한 전 소비)
    // ───────────────────────────────────────────
    fun consumeSelected(onComplete: () -> Unit = {}) {
        val ids = _uiState.value.selectedItemIds.toSet()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val successIds = mutableSetOf<Int>()
            ids.forEach { id ->
                if (repository.consumeItem(id.toLong())) successIds.add(id)
            }
            if (successIds.isNotEmpty()) {
                allItems = allItems.filter { it.id !in successIds }.toMutableList()
            }
            val failCount = ids.size - successIds.size
            updateState(_uiState.value.selectedFilter)
            if (failCount > 0) {
                _uiState.value = _uiState.value.copy(
                    error = "${failCount}개 항목을 소비 처리하지 못했어요. 다시 시도해주세요."
                )
            }
            onComplete()
        }
    }

    fun updateItem(updatedItem: FoodItem) {
        val index = allItems.indexOfFirst { it.id == updatedItem.id }
        if (index == -1) return

        val originalItem = allItems[index]

        // 낙관적 업데이트 — UI 즉시 반영
        allItems = allItems.toMutableList().also { it[index] = updatedItem }
        updateState(_uiState.value.selectedFilter)

        // 서버에 PATCH 요청
        viewModelScope.launch {
            val success = repository.updateItem(
                id = updatedItem.id.toLong(),
                request = ItemUpdateRequest(
                    name = updatedItem.name,
                    category = updatedItem.category.ifEmpty { null },
                    expiryDate = updatedItem.expiryDate.ifEmpty { null },
                    purchaseDate = updatedItem.purchaseDate.ifEmpty { null },
                    memo = updatedItem.memo.ifEmpty { null },
                    storageId =
                        updatedItem.storageId.takeIf { it > 0L }
                            ?: storageIdMap[updatedItem.storage],
                )
            )
            if (!success) {
                // API 실패 시 원래 값으로 롤백
                val rollbackIndex = allItems.indexOfFirst { it.id == originalItem.id }
                if (rollbackIndex != -1) {
                    allItems = allItems.toMutableList().also { it[rollbackIndex] = originalItem }
                    updateState(_uiState.value.selectedFilter)
                }
                _uiState.value = _uiState.value.copy(
                    error = "수정 내용을 저장하지 못했어요. 다시 시도해주세요."
                )
            }
        }
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getIngredients().fold(
                onSuccess = { dtos ->
                    // 빈 목록도 정상 상태 — empty state로 표시
                    allItems = dtos.map { it.toFoodItem() }.toMutableList()

                    // storageId 매핑 채우기
                    dtos.forEach { dto ->
                        val type = when (dto.storage) {
                            "FREEZER" -> StorageType.FREEZER
                            "PANTRY"  -> StorageType.PANTRY
                            else      -> StorageType.FRIDGE
                        }
                        storageIdMap[type] = dto.storageId
                    }

                    updateState(_uiState.value.selectedFilter)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "데이터를 불러오지 못했어요."
                    )
                }
            )
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

        // RECENT는 최근 추가 순(서버 반환 역순) 유지, 나머지는 유통기한 오름차순 정렬
        val sorted = if (filter == InventoryFilter.RECENT) {
            filtered.reversed()
        } else {
            filtered.sortedWith(compareBy(nullsLast()) {
                it.expiryDate.takeIf { d -> d.isNotBlank() }
            })
        }

        _uiState.value = InventoryListUiState(
            selectedFilter = filter,
            allItems = allItems,
            filteredItems = sorted,
            totalCount = filtered.size,
            freshCount = filtered.count { it.status == FoodStatus.FRESH },
            nearExpiryCount = filtered.count { it.status == FoodStatus.NEAR_EXPIRY },
            expiredCount = filtered.count { it.status == FoodStatus.EXPIRED },
            isLoading = false
        )
    }
}

// ───────────────────────────────────────────
// 확장 함수: ItemDto → FoodItem 변환
// ───────────────────────────────────────────
internal fun ItemDto.toFoodItem(): FoodItem {
    val storageType = when (storage) {
        "FREEZER" -> StorageType.FREEZER
        "PANTRY"  -> StorageType.PANTRY
        else      -> StorageType.FRIDGE
    }

    // 서버가 신선도 상태를 직접 내려줌 (FRESH / NEAR_EXPIRY / EXPIRED)
    val foodStatus = when (status) {
        "NEAR_EXPIRY" -> FoodStatus.NEAR_EXPIRY
        "EXPIRED"     -> FoodStatus.EXPIRED
        else          -> FoodStatus.FRESH
    }

    return FoodItem(
        id = id.toInt(),
        name = name,
        category = category ?: "기타",
        storage = storageType,
        amount = "",                          // 백엔드 미지원 필드
        expiryDate = expiryDate ?: "",
        status = foodStatus,
        emoji = emoji ?: "🍽️",               // representativeImage 없을 때 폴백
        purchaseDate = purchaseDate ?: "",
        memo = memo ?: "",
        storageId = storageId,
        representativeImage = representativeImage
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