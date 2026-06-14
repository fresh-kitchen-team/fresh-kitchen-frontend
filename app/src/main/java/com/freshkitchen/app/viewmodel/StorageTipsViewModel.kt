package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.logging.ApiLog
import com.freshkitchen.app.network.StorageTipDto
import com.freshkitchen.app.network.TipsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 보관 팁 카테고리 enum
//   - API 의 displayCategory 값과 1:1 로 매핑
// ───────────────────────────────────────────
enum class StorageTipCategoryType(val apiValue: String, val displayName: String) {
    VEGETABLE_FRUIT("VEGETABLE_FRUIT", "채소/과일"),
    DAIRY_DRINK("DAIRY_DRINK", "유제품"),
    MEAT_SEAFOOD("MEAT_SEAFOOD", "육류/수산물"),
    ETC("ETC", "기타");

    companion object {
        fun fromApi(value: String?): StorageTipCategoryType? =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }
    }
}

// ───────────────────────────────────────────
// 보관 팁 UI 모델
// ───────────────────────────────────────────
data class StorageTipUi(
    val id: Long,
    val category: StorageTipCategoryType,
    val displayCategoryName: String,
    val name: String,
    val emoji: String,
    val tip: String,
    val storageType: String,
)

// ───────────────────────────────────────────
// 보관 팁 화면 UI 상태
// ───────────────────────────────────────────
data class StorageTipsUiState(
    val tipsByCategory: Map<StorageTipCategoryType, List<StorageTipUi>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ───────────────────────────────────────────
// StorageTipsViewModel
//   - 보관 팁 전체 목록을 한번에 불러오고
//     카테고리별로 묶어 UI 에 전달한다.
// ───────────────────────────────────────────
@HiltViewModel
class StorageTipsViewModel @Inject constructor(
    private val repository: TipsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageTipsUiState())
    val uiState: StateFlow<StorageTipsUiState> = _uiState.asStateFlow()

    init {
        loadStorageTips()
    }

    fun loadStorageTips() {
        viewModelScope.launch {
            ApiLog.i(TAG, "loadStorageTips() START")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val data = repository.getStorageTips(category = null)

            if (data != null) {
                val mapped = data.mapNotNull { dto -> dto.toUiOrNull() }
                val grouped = StorageTipCategoryType.entries.associateWith { category ->
                    mapped.filter { it.category == category }
                }
                val skipped = data.size - mapped.size
                if (skipped > 0) {
                    ApiLog.w(
                        TAG,
                        "알 수 없는 displayCategory $skipped 건 무시됨 (허용값: ${StorageTipCategoryType.entries.joinToString { it.apiValue }})",
                    )
                }
                ApiLog.i(
                    TAG,
                    "loadStorageTips() OK total=${data.size} mapped=${mapped.size} " +
                        grouped.entries.joinToString(prefix = "{", postfix = "}") { "${it.key.apiValue}=${it.value.size}" },
                )

                _uiState.value = StorageTipsUiState(
                    tipsByCategory = grouped,
                    isLoading = false,
                )
            } else {
                ApiLog.w(TAG, "loadStorageTips() FAILED - repository 가 null 반환")
                _uiState.value = StorageTipsUiState(
                    isLoading = false,
                    error = "보관 팁을 불러오지 못했어요. 다시 시도해주세요.",
                )
            }
        }
    }

    private fun StorageTipDto.toUiOrNull(): StorageTipUi? {
        val category = StorageTipCategoryType.fromApi(displayCategory) ?: return null
        return StorageTipUi(
            id = id,
            category = category,
            displayCategoryName = displayCategoryName.ifBlank { category.displayName },
            name = name,
            emoji = emoji,
            tip = tip,
            storageType = storageType,
        )
    }

    companion object {
        private const val TAG = "Tips:Storage"
    }
}
