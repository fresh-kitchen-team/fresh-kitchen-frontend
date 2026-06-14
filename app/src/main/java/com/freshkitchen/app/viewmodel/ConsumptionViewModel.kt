package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.logging.ApiLog
import com.freshkitchen.app.network.AnalyticsRepository
import com.freshkitchen.app.network.ExpiringItemDto
import com.freshkitchen.app.network.IngredientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    /** 현재 소비 처리(PATCH /consume) API 호출 중인 itemId 들. 버튼 중복 클릭 방지용. */
    val consumingIds: Set<Long> = emptySet(),
)

// ───────────────────────────────────────────
// ConsumptionViewModel
//   - GET /api/v1/analytics/expiring-items?maxDDay=7
//   - 모든 저장 공간(냉장/냉동/팬트리) 결과를 한 번에 받아두고
//     화면에서 필터 chip 으로 클라이언트 사이드 필터링한다.
// ───────────────────────────────────────────
@HiltViewModel
class ConsumptionViewModel @Inject constructor(
    private val repository: AnalyticsRepository,
    private val ingredientRepository: IngredientRepository,
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
     * "소비 완료" 클릭 → PATCH /api/v1/items/{id}/consume 호출.
     *  - 폐기율(/analytics/summary)에는 반영되지 않는 "소비 처리"
     *  - 성공: 리스트에서 해당 항목 제거
     *  - 실패: 리스트는 유지하고 error 메시지 노출 (Snackbar 등에서 표시)
     *  - 동일 id 재호출은 무시 (중복 클릭 방지)
     */
    fun consumeItem(id: Long, onSuccess: () -> Unit = {}) {
        if (id in _uiState.value.consumingIds) {
            ApiLog.d(TAG, "consumeItem() ignore duplicate id=$id (already in-flight)")
            return
        }

        viewModelScope.launch {
            ApiLog.i(TAG, "consumeItem() START id=$id (PATCH /consume)")
            _uiState.value = _uiState.value.copy(
                consumingIds = _uiState.value.consumingIds + id,
                error = null,
            )

            val ok = ingredientRepository.consumeItem(id)
            val current = _uiState.value
            if (ok) {
                val newItems = current.items.filterNot { it.id == id }
                ApiLog.i(
                    TAG,
                    "consumeItem() OK id=$id 로컬 제거. 남은 itemCount=${newItems.size}",
                )
                _uiState.value = current.copy(
                    items = newItems,
                    consumingIds = current.consumingIds - id,
                )
                onSuccess()
            } else {
                ApiLog.w(TAG, "consumeItem() FAILED id=$id")
                _uiState.value = current.copy(
                    consumingIds = current.consumingIds - id,
                    error = "소비 처리에 실패했어요. 잠시 후 다시 시도해주세요.",
                )
            }
        }
    }

    /** Snackbar 등에서 에러를 한 번 보여준 뒤 호출해 상태를 비운다. */
    fun clearError() {
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
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
