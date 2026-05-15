package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.network.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 저장 공간 데이터 모델
// ───────────────────────────────────────────
data class StorageInfo(
    val emoji: String,
    val name: String,
    val itemCount: Int,
    val filterKey: String
)

// ───────────────────────────────────────────
// 최근 추가 식재료 UI 모델
// ───────────────────────────────────────────
data class RecentItemUi(
    val emoji: String,
    val name: String
)

// ───────────────────────────────────────────
// 홈 화면 UI 상태
// ───────────────────────────────────────────
data class HomeUiState(
    val totalItemCount: Int = 0,
    val recentAddedCount: Int = 0,
    val nearExpiryCount: Int = 0,
    val expiredCount: Int = 0,
    val storageList: List<StorageInfo> = emptyList(),
    val recentItems: List<RecentItemUi> = emptyList(), // 이모지 + 이름
    val isLoading: Boolean = false,
    val error: String? = null
)

// ───────────────────────────────────────────
// HomeViewModel
// ───────────────────────────────────────────
class HomeViewModel(
    private val repository: HomeRepository = HomeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val data = repository.getHomeSummary()

            if (data != null) {
                _uiState.value = HomeUiState(
                    totalItemCount = data.totalCount,
                    recentAddedCount = data.recentItems.size,
                    nearExpiryCount = data.nearExpiryCount,
                    expiredCount = data.expiredCount,
                    storageList = data.storages.map { storage ->
                        StorageInfo(
                            emoji = storage.emoji,
                            name = storage.name,
                            itemCount = storage.itemCount,
                            filterKey = storage.filterKey
                        )
                    },
                    recentItems = data.recentItems.map { RecentItemUi(emoji = it.emoji, name = it.name) },
                    isLoading = false
                )
            } else {
                // API 실패 시 빈 화면 + 에러 메시지
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = "데이터를 불러오지 못했어요. 다시 시도해주세요."
                )
            }
        }
    }
}