package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.network.IngredientRepository
import com.freshkitchen.app.network.ItemCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 수동 추가 UI 상태
// ───────────────────────────────────────────
data class ManualAddUiState(
    val name: String = "",
    val selectedStorage: StorageType = StorageType.FRIDGE,
    val expiryDate: String = "",
    val purchaseDate: String = "",
    val memo: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

// ───────────────────────────────────────────
// 수동 추가 ViewModel
// ───────────────────────────────────────────
class ManualAddViewModel(
    private val repository: IngredientRepository = IngredientRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualAddUiState())
    val uiState: StateFlow<ManualAddUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String)         { _uiState.value = _uiState.value.copy(name = name) }
    fun onStorageChange(s: StorageType)    { _uiState.value = _uiState.value.copy(selectedStorage = s) }
    fun onExpiryDateChange(date: String)   { _uiState.value = _uiState.value.copy(expiryDate = date) }
    fun onPurchaseDateChange(date: String) { _uiState.value = _uiState.value.copy(purchaseDate = date) }
    fun onMemoChange(memo: String)         { _uiState.value = _uiState.value.copy(memo = memo) }
    fun clearError()                       { _uiState.value = _uiState.value.copy(error = null) }

    // 숫자 8자리 → "YYYY-MM-DD" 변환 (8자리 미만이면 null)
    private fun formatDate(digits: String): String? {
        if (digits.length != 8) return null
        return "${digits.substring(0, 4)}-${digits.substring(4, 6)}-${digits.substring(6, 8)}"
    }

    // ── 식재료 추가 제출 ──
    fun submit(onSuccess: () -> Unit) {
        val s = _uiState.value

        if (s.name.isBlank()) {
            _uiState.value = s.copy(error = "식재료 이름을 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = s.copy(isSubmitting = true, error = null)
            val success = repository.addItem(
                ItemCreateRequest(
                    name         = s.name.trim(),
                    storageType  = s.selectedStorage.name,  // "FRIDGE" | "FREEZER" | "PANTRY"
                    expiryDate   = formatDate(s.expiryDate),
                    purchaseDate = formatDate(s.purchaseDate),
                    memo         = s.memo.ifBlank { null }
                )
            )
            if (success) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "식재료 추가에 실패했어요. 다시 시도해주세요."
                )
            }
        }
    }
}
