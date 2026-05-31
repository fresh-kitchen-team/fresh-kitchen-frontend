package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.logging.ApiLog
import com.freshkitchen.app.network.AnalyticsCategoryStatDto
import com.freshkitchen.app.network.AnalyticsRepository
import com.freshkitchen.app.network.ExpiringItemDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ───────────────────────────────────────────
// 카테고리별 폐기율 막대 UI 모델
// ───────────────────────────────────────────
data class CategoryRateUi(
    val category: StorageTipCategoryType,
    val label: String,
    val itemCount: Int,     // activeCount (참고용)
    val percent: Int,       // discardRate 0..100
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
    val disposalRatePercent: Int? = null,    // overallDiscardRate
    val totalActiveCount: Int = 0,
    /** categoryStats[].urgentCount 합계 — 유통기한 3일 이내 식재료 수 */
    val urgentItemCount: Int = 0,
    val expiringChips: List<ExpiringChipUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ───────────────────────────────────────────
// AnalyticsViewModel
//   - GET /api/v1/analytics/summary → overallDiscardRate, categoryStats[].discardRate
//   - GET /api/v1/analytics/expiring-items → 소비 권장 알림 chip
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

            if (summary == null && expiring == null) {
                ApiLog.w(TAG, "loadAnalytics() FAILED - 두 API 모두 null")
                _uiState.value = AnalyticsUiState(
                    isLoading = false,
                    error = "분석 데이터를 불러오지 못했어요. 다시 시도해주세요.",
                )
                return@launch
            }

            val expiringList = expiring.orEmpty()
            val categoryRates = buildCategoryRates(summary?.categoryStats)
            val expiringChips = buildExpiringChips(expiringList)
            val totalActive = summary?.totalActiveCount ?: 0
            val disposalRate = summary?.overallDiscardRate
                ?.roundToInt()
                ?.coerceIn(0, 100)
                ?.takeIf { totalActive > 0 }
            val urgentItemCount = summary?.categoryStats.orEmpty().sumOf { it.urgentCount }

            ApiLog.i(
                TAG,
                "loadAnalytics() OK totalActive=$totalActive overallDiscardRate=${summary?.overallDiscardRate} " +
                    "urgentItemCount=$urgentItemCount " +
                    "categoryStats=${summary?.categoryStats?.size ?: 0} " +
                    "categories=${categoryRates.joinToString(prefix = "{", postfix = "}") { "${it.category.apiValue}=${it.percent}%" }} " +
                    "expiringItems=${expiringList.size} chips=${expiringChips.size}",
            )

            _uiState.value = AnalyticsUiState(
                categoryRates = categoryRates,
                disposalRatePercent = disposalRate,
                totalActiveCount = totalActive,
                urgentItemCount = urgentItemCount,
                expiringChips = expiringChips,
                isLoading = false,
                error = null,
            )
        }
    }

    private fun buildExpiringChips(items: List<ExpiringItemDto>): List<ExpiringChipUi> {
        val today = java.time.LocalDate.now()
        return items
            .map { it to it.computeDday(today) }
            .filter { (_, dday) -> dday <= CHIP_MAX_DDAY }
            .sortedBy { (_, dday) -> dday }
            .take(MAX_CHIPS)
            .map { (dto, dday) -> ExpiringChipUi(id = dto.id, name = dto.name, dday = dday) }
    }

    /** summary.categoryStats[].discardRate 를 막대 그래프 UI 로 변환 */
    private fun buildCategoryRates(stats: List<AnalyticsCategoryStatDto>?): List<CategoryRateUi> {
        val statByType = stats.orEmpty()
            .mapNotNull { stat ->
                StorageTipCategoryType.fromApi(stat.category)?.let { it to stat }
            }
            .toMap()

        return StorageTipCategoryType.entries.map { type ->
            val stat = statByType[type]
            CategoryRateUi(
                category = type,
                label = stat?.displayName?.takeIf { it.isNotBlank() } ?: type.shortDisplayName(),
                itemCount = stat?.activeCount ?: 0,
                percent = stat?.discardRate?.roundToInt()?.coerceIn(0, 100) ?: 0,
            )
        }
    }

    private fun StorageTipCategoryType.shortDisplayName(): String = when (this) {
        StorageTipCategoryType.VEGETABLE_FRUIT -> "채소/과일"
        StorageTipCategoryType.DAIRY_DRINK -> "유제품"
        StorageTipCategoryType.MEAT_SEAFOOD -> "육류/수산물"
        StorageTipCategoryType.ETC -> "기타"
    }

    companion object {
        private const val TAG = "Analytics"
        private const val MAX_CHIPS = 6
        private const val CHIP_MAX_DDAY = 10
    }
}
