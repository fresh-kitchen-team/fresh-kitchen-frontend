package com.example.myfrigelocal.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.network.UserProfileUpdateRequest
import com.example.myfrigelocal.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 온보딩 설정 UI 상태
// ───────────────────────────────────────────
data class OnboardingSetupState(
    val currentStep: Int = 0,                         // 0: 알러지, 1: 음식 스타일, 2: 식재료 등록
    val selectedAllergies: Set<String> = emptySet(),
    val selectedFoodStyles: Set<String> = emptySet(),
    val selectedQuickItems: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val submitDone: Boolean = false,
    val submitError: String? = null
)

// ───────────────────────────────────────────
// 온보딩 설정 ViewModel
// ───────────────────────────────────────────
class OnboardingSetupViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

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

    // ───────────────────────────────────────────
    // 온보딩 완료 — 프로필 PATCH
    // ───────────────────────────────────────────
    fun submitProfile(onSuccess: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true, submitError = null)
            try {
                val request = UserProfileUpdateRequest(
                    allergies = s.selectedAllergies.toList(),
                    foodStyles = s.selectedFoodStyles.toList(),
                    preferredIngredients = s.selectedQuickItems.toList()
                )
                val response = userRepository.updateProfile(request)
                Log.d("OnboardingSetupVM", "PATCH 응답: ${response.code}")
                if (response.code == "COMMON-200") {
                    _state.value = _state.value.copy(isSubmitting = false, submitDone = true)
                    onSuccess()
                } else {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        submitError = "프로필 저장에 실패했어요 (${response.code})"
                    )
                    // 저장 실패해도 다음으로 넘기기 (UX)
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("OnboardingSetupVM", "PATCH 예외: ${e.message}")
                _state.value = _state.value.copy(isSubmitting = false, submitError = e.message)
                // 네트워크 오류도 다음으로 넘기기 (UX)
                onSuccess()
            }
        }
    }
}