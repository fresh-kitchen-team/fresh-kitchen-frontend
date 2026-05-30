package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.network.IngredientRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 검색 UI 상태
// ───────────────────────────────────────────
data class SearchUiState(
    val query: String = "",
    val recentSearches: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val searchResults: List<FoodItem>? = null, // null = 검색 전 초기 상태
    val nearExpiryItems: List<FoodItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ───────────────────────────────────────────
// 검색 ViewModel
// ───────────────────────────────────────────
class SearchViewModel(
    private val repository: IngredientRepository = IngredientRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadNearExpiryItems()
    }

    // ── 소비임박 목록 로드 (초기 화면용) ──
    private fun loadNearExpiryItems() {
        viewModelScope.launch {
            val items = repository.getIngredients()
                .map { it.toFoodItem() }
                .filter { it.status == FoodStatus.NEAR_EXPIRY }
                .sortedWith(compareBy(nullsLast()) {
                    it.expiryDate.takeIf { d -> d.isNotBlank() }
                })
            _uiState.value = _uiState.value.copy(nearExpiryItems = items)
        }
    }

    /** 검색어 입력 중 호출 */
    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            suggestions = emptyList(),
            searchResults = null,
            error = null
        )
    }

    /** 실제 검색 실행 — GET /api/v1/items?name={query} */
    fun search(query: String = _uiState.value.query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        // 중복 요청 방지
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                query = trimmed,
                suggestions = emptyList(),
                isLoading = true,
                error = null
            )

            val results = repository.searchItems(trimmed).map { it.toFoodItem() }

            // 최근 검색어 업데이트 (중복 제거, 최신순, 최대 10개)
            val updatedRecent = listOf(trimmed) +
                    _uiState.value.recentSearches.filter { it != trimmed }

            _uiState.value = _uiState.value.copy(
                searchResults = results,
                recentSearches = updatedRecent.take(10),
                isLoading = false
            )
        }
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
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            query = "",
            suggestions = emptyList(),
            searchResults = null,
            isLoading = false,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
