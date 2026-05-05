package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ───────────────────────────────────────────
// 온보딩 설정 UI 상태
// ───────────────────────────────────────────
data class OnboardingSetupState(
    val currentStep: Int = 0,                         // 0: 알러지, 1: 음식 스타일, 2: 식재료 등록
    val selectedAllergies: Set<String> = emptySet(),
    val selectedFoodStyles: Set<String> = emptySet(),
    val selectedQuickItems: Set<String> = emptySet()
)

// ───────────────────────────────────────────
// 온보딩 설정 ViewModel
// ───────────────────────────────────────────
class OnboardingSetupViewModel : ViewModel() {

    private val _state = MutableStateFlow(OnboardingSetupState())
    val state: StateFlow<OnboardingSetupState> = _state.asStateFlow()

    // 알러지 토글
    fun toggleAllergy(allergy: String) {
        val current = _state.value.selectedAllergies
        _state.value = _state.value.copy(
            selectedAllergies = if (allergy in current) current - allergy else current + allergy
        )
    }

    // 선호 음식 스타일 토글
    fun toggleFoodStyle(style: String) {
        val current = _state.value.selectedFoodStyles
        _state.value = _state.value.copy(
            selectedFoodStyles = if (style in current) current - style else current + style
        )
    }

    // 식재료 간편 등록 토글
    fun toggleQuickItem(item: String) {
        val current = _state.value.selectedQuickItems
        _state.value = _state.value.copy(
            selectedQuickItems = if (item in current) current - item else current + item
        )
    }

    // 다음 단계로 이동
    fun nextStep() {
        if (_state.value.currentStep < 2) {
            _state.value = _state.value.copy(currentStep = _state.value.currentStep + 1)
        }
    }

    // 이전 단계로 이동
    fun prevStep() {
        if (_state.value.currentStep > 0) {
            _state.value = _state.value.copy(currentStep = _state.value.currentStep - 1)
        }
    }

    // 현재 단계가 마지막인지
    val isLastStep: Boolean get() = _state.value.currentStep == 2
}