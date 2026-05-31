package com.freshkitchen.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.network.ProfileEnumMapper
import com.freshkitchen.app.network.UserProfileUpdateRequest
import com.freshkitchen.app.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 프로필 UI 상태
// ───────────────────────────────────────────
data class ProfileUiState(
    val nickname: String = "",
    val profileImageUrl: String? = null,       // 서버에서 불러온 or 로컬 선택 URI
    val selectedAllergies: Set<String> = emptySet(),
    val selectedIngredients: Set<String> = emptySet(),
    val selectedFoodStyles: Set<String> = emptySet(),
    val selectedUtensils: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

// ───────────────────────────────────────────
// 프로필 ViewModel
// ───────────────────────────────────────────
class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    // ───────────────────────────────────────────
    // 프로필 불러오기 — GET /api/v1/users/me/profile
    // ───────────────────────────────────────────
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = userRepository.getProfile()
                Log.d("ProfileVM", "GET 응답: ${response.code}, data: ${response.data}")
                if (response.code == "COMMON-200" && response.data != null) {
                    val dto = response.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nickname = dto.nickname ?: "",
                        profileImageUrl = dto.profileImageUrl,
                        selectedAllergies = ProfileEnumMapper.allergiesFromEnum(dto.allergies),
                        selectedIngredients = dto.preferredIngredients?.toSet() ?: emptySet(),
                        selectedFoodStyles = ProfileEnumMapper.foodStylesFromEnum(dto.foodStyles),
                        selectedUtensils = ProfileEnumMapper.cookingToolsFromEnum(dto.cookingTools)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "GET 예외: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun onNicknameChange(value: String) {
        _uiState.value = _uiState.value.copy(nickname = value, isSaved = false)
    }

    fun onImageSelected(uriString: String) {
        _uiState.value = _uiState.value.copy(profileImageUrl = uriString, isSaved = false)
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

    fun toggleUtensil(utensil: String) {
        val current = _uiState.value.selectedUtensils
        _uiState.value = _uiState.value.copy(
            selectedUtensils = if (utensil in current) current - utensil else current + utensil,
            isSaved = false
        )
    }

    // ───────────────────────────────────────────
    // 프로필 저장 — PATCH /api/v1/users/me/profile
    // ───────────────────────────────────────────
    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true, isSaved = false, errorMessage = null)
            try {
                val request = UserProfileUpdateRequest(
                    nickname = s.nickname.ifBlank { null },
                    profileImageUrl = s.profileImageUrl,
                    allergies = ProfileEnumMapper.allergiesToEnum(s.selectedAllergies),
                    preferredIngredients = s.selectedIngredients.toList(),
                    foodStyles = ProfileEnumMapper.foodStylesToEnum(s.selectedFoodStyles),
                    cookingTools = ProfileEnumMapper.cookingToolsToEnum(s.selectedUtensils)
                )
                val response = userRepository.updateProfile(request)
                Log.d("ProfileVM", "PATCH 응답: ${response.code}")
                if (response.code == "COMMON-200") {
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "저장에 실패했어요 (${response.code})"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "PATCH 예외: ${e.message}")
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
            }
        }
    }
}