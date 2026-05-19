package com.example.myfrigelocal.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.network.IngredientRepository
import com.example.myfrigelocal.network.ItemCreateRequest
import com.example.myfrigelocal.network.ProfileEnumMapper
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
    private val userRepository: UserRepository = UserRepository(),
    private val ingredientRepository: IngredientRepository = IngredientRepository()
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

            // ── 1. 프로필 PATCH (실패해도 계속 진행) ──
            try {
                val request = UserProfileUpdateRequest(
                    allergies = ProfileEnumMapper.allergiesToEnum(s.selectedAllergies),
                    foodStyles = ProfileEnumMapper.foodStylesToEnum(s.selectedFoodStyles),
                    preferredIngredients = s.selectedQuickItems.toList()
                )
                val response = userRepository.updateProfile(request)
                Log.d("OnboardingSetupVM", "프로필 PATCH 응답: ${response.code}")
            } catch (e: Exception) {
                Log.w("OnboardingSetupVM", "프로필 PATCH 실패 (무시하고 계속): ${e.message}")
            }

            // ── 2. 선택한 식재료 인벤토리 추가 (storageId=1: 냉장실) ──
            if (s.selectedQuickItems.isNotEmpty()) {
                var addedCount = 0
                s.selectedQuickItems.forEach { item ->
                    // "🧅 양파" → "양파" (이모지 이후 텍스트 추출)
                    val name = item.substringAfter(" ").trim()
                    if (name.isNotBlank()) {
                        try {
                            val success = ingredientRepository.addItem(
                                ItemCreateRequest(name = name, storageId = 1)
                            )
                            if (success) addedCount++
                        } catch (e: Exception) {
                            Log.w("OnboardingSetupVM", "식재료 추가 실패 ($name): ${e.message}")
                        }
                    }
                }
                Log.d("OnboardingSetupVM", "식재료 추가 완료: $addedCount/${s.selectedQuickItems.size}개")
            }

            _state.value = _state.value.copy(isSubmitting = false, submitDone = true)
            onSuccess()
        }
    }
}