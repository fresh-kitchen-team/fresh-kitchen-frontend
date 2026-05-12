package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ───────────────────────────────────────────
// 검색 UI 상태
// ───────────────────────────────────────────
data class SearchUiState(
    val query: String = "",
    val recentSearches: List<String> = listOf("오렌지", "브로콜리", "아스파라거스", "육류"),
    val suggestions: List<String> = emptyList(),
    val searchResults: List<FoodItem>? = null, // null = 검색 전 초기 상태
    val nearExpiryItems: List<FoodItem> = emptyList()
)

// ───────────────────────────────────────────
// 검색 ViewModel
// ───────────────────────────────────────────
class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // 더미 데이터 (백엔드 연동 시 Repository로 교체)
    private val allItems = listOf(
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

    // 자동완성 후보: 식재료명 + 카테고리 + 보관장소
    private val allSuggestions: List<String> =
        allItems.map { it.name } +
                allItems.map { it.category }.distinct() +
                listOf("냉장", "냉동", "팬트리")

    init {
        _uiState.value = _uiState.value.copy(
            nearExpiryItems = allItems.filter { it.status == FoodStatus.NEAR_EXPIRY }
        )
    }

    /** 검색어 입력 중 호출 - 자동완성 갱신 */
    fun onQueryChange(query: String) {
        val suggestions = if (query.isBlank()) emptyList()
        else allSuggestions
            .filter { it.contains(query.trim(), ignoreCase = true) }
            .take(5)

        _uiState.value = _uiState.value.copy(
            query = query,
            suggestions = suggestions,
            searchResults = null
        )
    }

    /** 실제 검색 실행 */
    fun search(query: String = _uiState.value.query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        // 최근 검색어 업데이트 (중복 제거 후 최신 순으로 앞에 추가, 최대 10개)
        val updatedRecent = listOf(trimmed) +
                _uiState.value.recentSearches.filter { it != trimmed }
        val capped = updatedRecent.take(10)

        // 보관 장소 이름으로 검색
        val results: List<FoodItem> = when (trimmed) {
            "냉장" -> allItems.filter { it.storage == StorageType.FRIDGE }
            "냉동" -> allItems.filter { it.storage == StorageType.FREEZER }
            "팬트리" -> allItems.filter { it.storage == StorageType.PANTRY }
            else -> allItems.filter { item ->
                item.name.contains(trimmed, ignoreCase = true) ||
                        item.category.contains(trimmed, ignoreCase = true)
            }
        }

        _uiState.value = _uiState.value.copy(
            query = trimmed,
            suggestions = emptyList(),
            searchResults = results,
            recentSearches = capped
        )
    }

    /** 최근 검색어 개별 삭제 */
    fun removeRecentSearch(keyword: String) {
        _uiState.value = _uiState.value.copy(
            recentSearches = _uiState.value.recentSearches.filter { it != keyword }
        )
    }

    /** 최근 검색어 전체 삭제 */
    fun clearRecentSearches() {
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }

    /** 검색어 지우기 (X 버튼) */
    fun clearQuery() {
        _uiState.value = _uiState.value.copy(
            query = "",
            suggestions = emptyList(),
            searchResults = null
        )
    }
}