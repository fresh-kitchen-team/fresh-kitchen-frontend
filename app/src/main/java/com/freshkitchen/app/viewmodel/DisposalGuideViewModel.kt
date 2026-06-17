package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.logging.ApiLog
import com.freshkitchen.app.network.RecyclingTipDto
import com.freshkitchen.app.network.TipsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 분리배출 라벨 색조
//   - 일반쓰레기: 빨강, 음식물쓰레기: 초록, 그 외: 회색
// ───────────────────────────────────────────
enum class DisposalWasteTone { General, Food, Other }

// ───────────────────────────────────────────
// 폐기 가이드 UI 모델
// ───────────────────────────────────────────
data class DisposalItemUi(
    val id: Long,
    val name: String,
    val description: String,
    val wasteType: String,
    val tone: DisposalWasteTone,
)

// ───────────────────────────────────────────
// 폐기 가이드 화면 UI 상태
// ───────────────────────────────────────────
data class DisposalGuideUiState(
    val items: List<DisposalItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ───────────────────────────────────────────
// DisposalGuideViewModel
// ───────────────────────────────────────────
@HiltViewModel
class DisposalGuideViewModel @Inject constructor(
    private val repository: TipsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisposalGuideUiState())
    val uiState: StateFlow<DisposalGuideUiState> = _uiState.asStateFlow()

    init {
        loadRecyclingTips()
    }

    fun loadRecyclingTips() {
        viewModelScope.launch {
            ApiLog.i(TAG, "loadRecyclingTips() START")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val data = repository.getRecyclingTips()

            if (data != null) {
                val items = data.map { it.toUi() }
                val toneStats = items.groupingBy { it.tone }.eachCount()
                ApiLog.i(
                    TAG,
                    "loadRecyclingTips() OK items=${items.size} tones=$toneStats",
                )
                _uiState.value = DisposalGuideUiState(
                    items = items,
                    isLoading = false,
                )
            } else {
                ApiLog.w(TAG, "loadRecyclingTips() FAILED - repository 가 null 반환")
                _uiState.value = DisposalGuideUiState(
                    isLoading = false,
                    error = "분리배출 가이드를 불러오지 못했어요. 다시 시도해주세요.",
                )
            }
        }
    }

    private fun RecyclingTipDto.toUi(): DisposalItemUi {
        val tone = when {
            wasteType.contains("일반") -> DisposalWasteTone.General
            wasteType.contains("음식") -> DisposalWasteTone.Food
            else -> DisposalWasteTone.Other
        }
        return DisposalItemUi(
            id = id,
            name = name,
            description = description,
            wasteType = wasteType,
            tone = tone,
        )
    }

    companion object {
        private const val TAG = "Tips:Recycling"
    }
}
