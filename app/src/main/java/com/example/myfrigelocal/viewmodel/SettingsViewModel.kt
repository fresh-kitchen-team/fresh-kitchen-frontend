package com.example.myfrigelocal.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.data.auth.AuthTokenStore
import com.example.myfrigelocal.data.auth.TokenDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 설정 UI 상태
// ───────────────────────────────────────────
data class SettingsUiState(
    val expiryAlarmEnabled: Boolean = true,   // 유통기한 알림
    val photoAlarmEnabled: Boolean = false    // 사진 등록 알림
)

// ───────────────────────────────────────────
// 설정 ViewModel
// ───────────────────────────────────────────
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleExpiryAlarm(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(expiryAlarmEnabled = enabled)
    }

    fun togglePhotoAlarm(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(photoAlarmEnabled = enabled)
    }

    // ───────────────────────────────────────────
    // 로그아웃 — 토큰 전체 삭제
    // ───────────────────────────────────────────
    fun logout(context: Context, onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            TokenDataStore.clearTokens(context)
            AuthTokenStore.clear()
            onLogoutComplete()
        }
    }
}