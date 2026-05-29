package com.example.myfrigelocal.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.data.auth.AuthTokenStore
import com.example.myfrigelocal.data.auth.TokenDataStore
import com.example.myfrigelocal.network.AuthRepository
import com.example.myfrigelocal.network.RetrofitClient
import com.example.myfrigelocal.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 설정 UI 상태
// ───────────────────────────────────────────
data class SettingsUiState(
    val expiryAlarmEnabled: Boolean = true,   // 유통기한 알림 (FCM 서버 관리)
    val photoAlarmEnabled: Boolean = false,   // 사진 등록 알림
    val isWithdrawing: Boolean = false,       // 탈퇴 처리 중
    val withdrawError: String? = null,        // 탈퇴 오류 메시지
    val loginProvider: String? = null,        // "GOOGLE" | "KAKAO"
    val nickname: String? = null              // 프로필 닉네임
)

// ───────────────────────────────────────────
// 설정 ViewModel
// ───────────────────────────────────────────
class SettingsViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ───────────────────────────────────────────
    // 계정 정보 불러오기
    // ───────────────────────────────────────────
    fun loadAccountInfo(context: Context) {
        viewModelScope.launch {
            // 로그인 provider (DataStore)
            val provider = TokenDataStore.getLoginProvider(context).first()
            _uiState.value = _uiState.value.copy(loginProvider = provider)

            // 닉네임 (서버 프로필)
            try {
                val response = userRepository.getProfile()
                if (response.code == "COMMON-200" && response.data != null) {
                    _uiState.value = _uiState.value.copy(nickname = response.data.nickname)
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "프로필 조회 실패: ${e.message}")
            }
        }
    }

    fun toggleExpiryAlarm(enabled: Boolean) {
        // FCM 알림은 서버에서 관리 → UI 상태만 반영
        _uiState.value = _uiState.value.copy(expiryAlarmEnabled = enabled)
    }

    fun togglePhotoAlarm(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(photoAlarmEnabled = enabled)
    }

    // ───────────────────────────────────────────
    // 로그아웃 — 서버 토큰 무효화 + 로컬 토큰 삭제
    // ───────────────────────────────────────────
    fun logout(context: Context, onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.logout()
                Log.d("SettingsVM", "서버 로그아웃 완료")
            } catch (e: Exception) {
                Log.w("SettingsVM", "서버 로그아웃 실패 (로컬 토큰은 삭제): ${e.message}")
            }
            TokenDataStore.clearTokens(context)
            AuthTokenStore.clear()
            onLogoutComplete()
        }
    }

    // ───────────────────────────────────────────
    // 회원 탈퇴 — DELETE /api/v1/users/me
    // ───────────────────────────────────────────
    fun withdraw(context: Context, onWithdrawComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWithdrawing = true, withdrawError = null)
            try {
                val response = userRepository.deleteAccount()
                Log.d("SettingsVM", "회원 탈퇴 응답: ${response.code}")
                if (response.code == "COMMON-200") {
                    TokenDataStore.clearTokens(context)
                    AuthTokenStore.clear()
                    _uiState.value = _uiState.value.copy(isWithdrawing = false)
                    onWithdrawComplete()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isWithdrawing = false,
                        withdrawError = "탈퇴에 실패했어요 (${response.code})"
                    )
                }
            } catch (e: Exception) {
                Log.e("SettingsVM", "회원 탈퇴 예외: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isWithdrawing = false,
                    withdrawError = "탈퇴 중 오류가 발생했어요"
                )
            }
        }
    }

    fun clearWithdrawError() {
        _uiState.value = _uiState.value.copy(withdrawError = null)
    }

}
