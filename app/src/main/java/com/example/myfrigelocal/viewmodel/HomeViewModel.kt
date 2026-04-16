package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 저장 공간 데이터 모델
data class StorageInfo(
    val emoji: String,
    val name: String,
    val itemCount: Int
)

// 홈 화면 UI 상태
data class HomeUiState(
    val totalItemCount: Int = 0,
    val recentAddedCount: Int = 0,
    val nearExpiryCount: Int = 0,
    val expiredCount: Int = 0,
    val storageList: List<StorageInfo> = emptyList(),
    val recentItems: List<String> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        // TODO: 실제 API 연동 시 여기서 데이터 fetch
        // 지금은 더미 데이터로 UI 확인
        _uiState.value = HomeUiState(
            totalItemCount = 42,
            recentAddedCount = 3,
            nearExpiryCount = 5,
            expiredCount = 2,
            storageList = listOf(
                StorageInfo(emoji = "❄️", name = "냉동실", itemCount = 12),
                StorageInfo(emoji = "🥛", name = "냉장실", itemCount = 24),
                StorageInfo(emoji = "🥫", name = "팬트리", itemCount = 6),
            ),
            recentItems = listOf("🥛", "🥩", "🥦", "🥚")
        )
    }
}