package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.logging.ApiLog
import com.example.myfrigelocal.network.AnalyticsRepository
import com.example.myfrigelocal.network.ExpiringItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// ───────────────────────────────────────────
// 소비 권장 알림 화면 - 저장 공간 필터
// ───────────────────────────────────────────
enum class ConsumptionStorageFilter(val label: String, val apiValue: String?) {
    All("전체", null),
    Fridge("냉장", "FRIDGE"),
    Freezer("냉동", "FREEZER"),
    Pantry("팬트리", "PANTRY");
}

// ───────────────────────────────────────────
// dday 위험도 톤
//   ≤1 일: Critical(빨강), ≤3 일: Warning(주황), 그 외: Info(초록)
// ───────────────────────────────────────────
enum class ConsumptionDdayTone { Critical, Warning, Info }

// ───────────────────────────────────────────
// 소비 권장 알림 카드 UI 모델
// ───────────────────────────────────────────
data class ConsumptionItemUi(
    val id: Long,
    val name: String,
    val emoji: String?,
    val storageType: String,        // "FRIDGE" | "FREEZER" | "PANTRY"
    val storageLabel: String,       // "냉장실" | "냉동실" | "팬트리"
    val expiryDate: String?,        // 화면에 보일 "2026-05-20" 형식
    val dday: Int,                  // 클라이언트 계산 dday
    val ddayLabel: String,          // "D-3" 등
    val ddayTone: ConsumptionDdayTone,
)

// ───────────────────────────────────────────
// 소비 권장 알림 화면 UI 상태
// ───────────────────────────────────────────
data class ConsumptionUiState(
    val items: List<ConsumptionItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ───────────────────────────────────────────
// ConsumptionViewModel
//   - GET /api/v1/analytics/expiring-items?maxDDay=7
//   - 모든 저장 공간(냉장/냉동/팬트리) 결과를 한 번에 받아두고
//     화면에서 필터 chip 으로 클라이언트 사이드 필터링한다.
// ───────────────────────────────────────────
class ConsumptionViewModel(
    private val repository: AnalyticsRepository = AnalyticsRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsumptionUiState())
    val uiState: StateFlow<ConsumptionUiState> = _uiState.asStateFlow()

    init {
        loadExpiringItems()
    }

    fun loadExpiringItems() {
        viewModelScope.launch {
            ApiLog.i(TAG, "loadExpiringItems() START maxDDay=$MAX_DDAY")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val data = repository.getExpiringItems(maxDDay = MAX_DDAY)
            if (data == null) {
                ApiLog.w(TAG, "loadExpiringItems() FAILED")
                _uiState.value = ConsumptionUiState(
                    isLoading = false,
                    error = "임박 식재료를 불러오지 못했어요. 다시 시도해주세요.",
                )
                return@launch
            }

            val today = LocalDate.now()
            val items = data
                .map { dto -> dto.toUi(today) }
                .filter { it.dday <= MAX_DDAY }    // 백엔드가 7 일 초과 항목을 섞어 줘도 클라에서 한 번 더 필터
                .sortedWith(compareBy({ it.dday }, { it.name }))

            ApiLog.i(
                TAG,
                "loadExpiringItems() OK rawSize=${data.size} uiSize=${items.size} " +
                    "byStorage=${items.groupingBy { it.storageType }.eachCount()}",
            )

            _uiState.value = ConsumptionUiState(
                items = items,
                isLoading = false,
                error = null,
            )
        }
    }

    /**
     * 화면에서 "소비 완료" 를 누른 항목을 리스트에서 제거.
     * (실제 백엔드 소비 처리 API 는 아직 미연동이라 로컬 상태만 갱신)
     */
    fun removeItemLocally(id: Long) {
        val current = _uiState.value
        _uiState.value = current.copy(items = current.items.filterNot { it.id == id })
    }

    private fun ExpiringItemDto.toUi(today: LocalDate): ConsumptionItemUi {
        val computedDday = computeDday(today)
        val tone = when {
            computedDday <= 1 -> ConsumptionDdayTone.Critical
            computedDday <= 3 -> ConsumptionDdayTone.Warning
            else -> ConsumptionDdayTone.Info
        }
        return ConsumptionItemUi(
            id = id,
            name = name,
            emoji = emoji,
            storageType = storageType,
            storageLabel = storageType.toKoreanStorageLabel(),
            expiryDate = expiresAt?.substringBefore("T"),
            dday = computedDday,
            ddayLabel = formatDdayLabel(computedDday),
            ddayTone = tone,
        )
    }

    private fun String.toKoreanStorageLabel(): String = when (uppercase()) {
        "FRIDGE" -> "냉장실"
        "FREEZER" -> "냉동실"
        "PANTRY" -> "팬트리"
        else -> this
    }

    companion object {
        private const val TAG = "Consumption"
        private const val MAX_DDAY = 7
    }
}
