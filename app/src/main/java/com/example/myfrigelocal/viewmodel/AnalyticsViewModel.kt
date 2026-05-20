package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.logging.ApiLog
import com.example.myfrigelocal.network.AnalyticsRepository
import com.example.myfrigelocal.network.ExpiringItemDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 카테고리별 폐기율 막대 UI 모델
// ───────────────────────────────────────────
data class CategoryRateUi(
    val category: StorageTipCategoryType,   // 동일한 4 분류 enum 재사용 (VEGETABLE_FRUIT, DAIRY_DRINK, MEAT_SEAFOOD, ETC)
    val label: String,                      // 화면 라벨 (서버 categoryDisplayName 우선)
    val itemCount: Int,                     // 해당 카테고리 임박 식재료 수
    val percent: Int,                       // 0..100, 합 ≒ 100
)

// ───────────────────────────────────────────
// 임박 식재료 chip UI 모델 ("닭가슴살 (D-5)")
// ───────────────────────────────────────────
data class ExpiringChipUi(
    val id: Long,
    val name: String,
    val dday: Int,
)

// ───────────────────────────────────────────
// 소비 분석 화면 UI 상태
// ───────────────────────────────────────────
data class AnalyticsUiState(
    val categoryRates: List<CategoryRateUi> = emptyList(),
    val disposalRatePercent: Int? = null,    // 전체 폐기율 (expiredCount / totalCount * 100)
    val expiredCount: Int = 0,
    val totalCount: Int = 0,
    val expiringChips: List<ExpiringChipUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ───────────────────────────────────────────
// AnalyticsViewModel
//   - analytics/summary 와 analytics/expiring-items 두 API 를
//     동시에 호출하여 화면 상태를 구성한다.
// ───────────────────────────────────────────
class AnalyticsViewModel(
    private val repository: AnalyticsRepository = AnalyticsRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            ApiLog.i(TAG, "loadAnalytics() START")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val summaryDeferred = async { repository.getAnalyticsSummary() }
            val expiringDeferred = async { repository.getExpiringItems() }
            val summary = summaryDeferred.await()
            val expiring = expiringDeferred.await()

            // 둘 다 실패하면 에러, 하나만 실패하면 가능한 부분만 채워준다.
            if (summary == null && expiring == null) {
                ApiLog.w(TAG, "loadAnalytics() FAILED - 두 API 모두 null")
                _uiState.value = AnalyticsUiState(
                    isLoading = false,
                    error = "분석 데이터를 불러오지 못했어요. 다시 시도해주세요.",
                )
                return@launch
            }

            val expiringList: List<ExpiringItemDto> = expiring.orEmpty()
            val categoryRates = buildCategoryRates(expiringList)
            val expiringChips = buildExpiringChips(expiringList)

            val total = summary?.totalCount ?: 0
            val expired = summary?.expiredCount ?: 0
            val disposalRate = if (total > 0) (expired * 100 / total).coerceIn(0, 100) else null

            ApiLog.i(
                TAG,
                "loadAnalytics() OK total=$total expired=$expired rate=${disposalRate ?: "n/a"}% " +
                    "categories=${categoryRates.joinToString(prefix = "{", postfix = "}") { "${it.category.apiValue}=${it.itemCount}(${it.percent}%)" }} " +
                    "chips=${expiringChips.size}",
            )

            _uiState.value = AnalyticsUiState(
                categoryRates = categoryRates,
                disposalRatePercent = disposalRate,
                expiredCount = expired,
                totalCount = total,
                expiringChips = expiringChips,
                isLoading = false,
                error = null,
            )
        }
    }

    /**
     * "소비 권장 알림" 카드 chip 용 목록.
     *  - 카드 본문에 "7일 이내" 라고 적혀 있어 동일 임계로 필터링
     *  - 백엔드 dday 가 모두 0 으로 오는 케이스가 있어 expiresAt 으로 클라 계산 후 사용
     *  - 임박한 순서(dday 오름차순) 로 정렬해 상위 MAX_CHIPS 개만 노출
     */
    private fun buildExpiringChips(items: List<ExpiringItemDto>): List<ExpiringChipUi> {
        val today = java.time.LocalDate.now()
        return items
            .map { it to it.computeDday(today) }
            .filter { (_, dday) -> dday <= CHIP_MAX_DDAY }
            .sortedBy { (_, dday) -> dday }
            .take(MAX_CHIPS)
            .map { (dto, dday) -> ExpiringChipUi(id = dto.id, name = dto.name, dday = dday) }
    }

    /**
     * 임박 식재료를 4 카테고리로 묶고 비율(%) 산출.
     *  - 데이터가 한 건도 없으면 모든 카테고리 0% 로 반환
     *  - 반올림 누적 오차로 합이 99 또는 101 이 되더라도 시각화에는 무영향
     */
    private fun buildCategoryRates(items: List<ExpiringItemDto>): List<CategoryRateUi> {
        val grouped = items.groupBy { StorageTipCategoryType.fromApi(it.category) }
        val total = items.size
        val skippedUnknown = grouped[null]?.size ?: 0
        if (skippedUnknown > 0) {
            ApiLog.w(
                TAG,
                "알 수 없는 category $skippedUnknown 건 무시됨 (허용값: ${StorageTipCategoryType.entries.joinToString { it.apiValue }})",
            )
        }

        return StorageTipCategoryType.entries.map { type ->
            val group = grouped[type].orEmpty()
            val count = group.size
            val percent = if (total > 0) (count * 100 / total) else 0
            val label = group.firstOrNull()?.categoryDisplayName?.takeIf { it.isNotBlank() }
                ?: type.shortDisplayName()
            CategoryRateUi(
                category = type,
                label = label,
                itemCount = count,
                percent = percent,
            )
        }
    }

    /**
     * 막대 차트 라벨은 좁은 폭에 맞춰 짧게 보여준다.
     * (시안 라벨 "채소류 / 유제품 / 육류 / 기타" 와 톤 맞춤)
     */
    private fun StorageTipCategoryType.shortDisplayName(): String = when (this) {
        StorageTipCategoryType.VEGETABLE_FRUIT -> "채소/과일"
        StorageTipCategoryType.DAIRY_DRINK -> "유제품"
        StorageTipCategoryType.MEAT_SEAFOOD -> "육류/수산물"
        StorageTipCategoryType.ETC -> "기타"
    }

    companion object {
        private const val TAG = "Analytics"
        private const val MAX_CHIPS = 6
        private const val CHIP_MAX_DDAY = 10   // 카드 문구 "7일 이내" 와 일치
    }
}
