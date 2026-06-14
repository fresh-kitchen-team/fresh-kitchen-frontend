package com.freshkitchen.app.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.data.auth.AuthTokenStore
import com.freshkitchen.app.data.auth.SettingsDataStore
import com.freshkitchen.app.data.auth.TokenDataStore
import com.freshkitchen.app.network.AppVersionApiService
import com.freshkitchen.app.network.AuthRepository
import com.freshkitchen.app.network.LegalApiService
import com.freshkitchen.app.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
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
    val inquiryAlarmEnabled: Boolean = true, // 문의 답변 알림
    val isWithdrawing: Boolean = false,       // 탈퇴 처리 중
    val withdrawError: String? = null,        // 탈퇴 오류 메시지
    val loginProvider: String? = null,        // "GOOGLE" | "KAKAO"
    val nickname: String? = null,             // 프로필 닉네임
    val appVersion: String? = null,           // 앱 최신 버전 (예: "1.0.0")
    val termsUrl: String? = null,             // 이용약관 URL
    val privacyUrl: String? = null,           // 개인정보처리방침 URL
    val termsAgreedAt: String? = null,        // 이용약관 동의 일시
    val privacyAgreedAt: String? = null       // 개인정보처리방침 동의 일시
)

// ───────────────────────────────────────────
// 설정 ViewModel
// ───────────────────────────────────────────
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val appVersionApi: AppVersionApiService,
    private val legalApi: LegalApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAlarmSettings()
    }

    private fun loadAlarmSettings() {
        viewModelScope.launch {
            val expiryEnabled  = SettingsDataStore.getExpiryAlarmEnabled(appContext).first()
            val inquiryEnabled = SettingsDataStore.getInquiryAlarmEnabled(appContext).first()
            _uiState.value = _uiState.value.copy(
                expiryAlarmEnabled  = expiryEnabled,
                inquiryAlarmEnabled = inquiryEnabled
            )
        }
    }

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

    // ───────────────────────────────────────────
    // 앱 버전 불러오기 — GET /api/v1/app/version
    // ───────────────────────────────────────────
    fun loadAppVersion() {
        viewModelScope.launch {
            try {
                val response = appVersionApi.getAppVersion()
                if (response.data != null) {
                    _uiState.value = _uiState.value.copy(appVersion = response.data.latestVersion)
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "앱 버전 조회 실패: ${e.message}")
            }
        }
    }

    // ───────────────────────────────────────────
    // 약관 URL 불러오기 — GET /api/v1/legal
    // ───────────────────────────────────────────
    fun loadLegalUrls() {
        viewModelScope.launch {
            try {
                val response = legalApi.getLegal()
                response.data?.let { data ->
                    _uiState.value = _uiState.value.copy(
                        termsUrl = data.termsUrl,
                        privacyUrl = data.privacyUrl
                    )
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "약관 URL 조회 실패: ${e.message}")
            }
        }
    }

    // ───────────────────────────────────────────
    // 동의 상태 불러오기 — GET /api/v1/legal/agreement
    // ───────────────────────────────────────────
    fun loadAgreementStatus() {
        viewModelScope.launch {
            try {
                val response = legalApi.getAgreement()
                response.data?.let { data ->
                    _uiState.value = _uiState.value.copy(
                        termsAgreedAt = data.termsAgreedAt,
                        privacyAgreedAt = data.privacyAgreedAt
                    )
                }
            } catch (e: Exception) {
                Log.w("SettingsVM", "동의 상태 조회 실패: ${e.message}")
            }
        }
    }

    fun toggleExpiryAlarm(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(expiryAlarmEnabled = enabled)
        viewModelScope.launch {
            SettingsDataStore.setExpiryAlarmEnabled(appContext, enabled)
        }
    }

    fun toggleInquiryAlarm(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(inquiryAlarmEnabled = enabled)
        viewModelScope.launch {
            SettingsDataStore.setInquiryAlarmEnabled(appContext, enabled)
        }
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
