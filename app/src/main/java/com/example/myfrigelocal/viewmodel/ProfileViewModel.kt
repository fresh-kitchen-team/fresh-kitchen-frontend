package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ───────────────────────────────────────────
// 프로필 UI 상태
// ───────────────────────────────────────────
data class ProfileUiState(
    val nickname: String = "미식가_주디",
    val selectedAllergies: Set<String> = emptySet(),
    val selectedIngredients: Set<String> = emptySet(),
    val selectedFoodStyles: Set<String> = emptySet(),
    val isSaved: Boolean = false
)

// ───────────────────────────────────────────
// 프로필 ViewModel
// ───────────────────────────────────────────
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onNicknameChange(value: String) {
        _uiState.value = _uiState.value.copy(nickname = value, isSaved = false)
    }

    fun toggleAllergy(allergy: String) {
        val current = _uiState.value.selectedAllergies
        _uiState.value = _uiState.value.copy(
            selectedAllergies = if (allergy in current) current - allergy else current + allergy,
            isSaved = false
        )
    }

    fun toggleIngredient(ingredient: String) {
        val current = _uiState.value.selectedIngredients
        _uiState.value = _uiState.value.copy(
            selectedIngredients = if (ingredient in current) current - ingredient else current + ingredient,
            isSaved = false
        )
    }

    fun toggleFoodStyle(style: String) {
        val current = _uiState.value.selectedFoodStyles
        _uiState.value = _uiState.value.copy(
            selectedFoodStyles = if (style in current) current - style else current + style,
            isSaved = false
        )
    }

    fun save() {
        // TODO: 백엔드 연동 시 API 호출로 교체
        _uiState.value = _uiState.value.copy(isSaved = true)
    }
}