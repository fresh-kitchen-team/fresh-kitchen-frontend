package com.example.myfrigelocal.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.data.auth.AuthTokenStore
import com.example.myfrigelocal.data.auth.TokenDataStore
import com.example.myfrigelocal.network.AuthRepository
import com.example.myfrigelocal.network.UserProfileUpdateRequest
import com.example.myfrigelocal.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ───────────────────────────────────────────
// 로그인 상태
// ───────────────────────────────────────────
sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val isNewUser: Boolean) : LoginState()
    data class Error(val message: String)      : LoginState()
}

// ───────────────────────────────────────────
// LoginViewModel
// ───────────────────────────────────────────
class LoginViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    /**
     * @param displayName  Google 계정 표시 이름 (닉네임 초기값)
     * @param email        Google 이메일 (displayName 없을 때 대체)
     * @param photoUrl     Google 프로필 사진 URL
     */
    fun loginWithGoogle(
        idToken: String,
        context: Context,
        displayName: String? = null,
        email: String? = null,
        photoUrl: String? = null
    ) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            Log.d("LoginViewModel", "idToken 수신: ${idToken.take(20)}...")
            try {
                val response = repository.loginWithGoogle(idToken)
                Log.d("LoginViewModel", "응답 code: ${response.code}, data: ${response.data}")
                if (response.code == "COMMON-200" && response.data != null) {
                    val data = response.data
                    TokenDataStore.saveTokens(context, data.accessToken, data.refreshToken)
                    AuthTokenStore.setAccessToken(data.accessToken)
                    Log.d("LoginViewModel", "로그인 성공 - newUser: ${data.newUser}")

                    // 신규 유저: Google 프로필 정보로 초기 프로필 설정
                    if (data.newUser) {
                        val nickname = displayName?.takeIf { it.isNotBlank() }
                            ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                        try {
                            userRepository.updateProfile(
                                UserProfileUpdateRequest(
                                    nickname = nickname,
                                    profileImageUrl = photoUrl
                                )
                            )
                            Log.d("LoginViewModel", "신규 유저 프로필 초기화 완료 - nickname=$nickname")
                        } catch (e: Exception) {
                            Log.w("LoginViewModel", "프로필 초기화 실패 (무시): ${e.message}")
                        }
                    }

                    _loginState.value = LoginState.Success(isNewUser = data.newUser)
                } else {
                    Log.e("LoginViewModel", "로그인 실패 - code: ${response.code}")
                    _loginState.value = LoginState.Error("로그인에 실패했어요.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "예외 발생: ${e.message}")
                _loginState.value = LoginState.Error(e.message ?: "알 수 없는 오류가 발생했어요.")
            }
        }
    }

    /**
     * 카카오 로그인
     * @param accessToken  카카오 SDK에서 받은 액세스 토큰
     * @param nickname     카카오 프로필 닉네임 (신규 유저 초기값)
     * @param profileImageUrl 카카오 프로필 사진 URL
     */
    fun loginWithKakao(
        idToken: String,
        context: Context,
        nickname: String? = null,
        profileImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            Log.d("LoginViewModel", "Kakao idToken 수신: ${idToken.take(20)}...")
            try {
                val response = repository.loginWithKakao(idToken)
                Log.d("LoginViewModel", "카카오 응답 code: ${response.code}, data: ${response.data}")
                if (response.code == "COMMON-200" && response.data != null) {
                    val data = response.data
                    TokenDataStore.saveTokens(context, data.accessToken, data.refreshToken)
                    AuthTokenStore.setAccessToken(data.accessToken)
                    Log.d("LoginViewModel", "카카오 로그인 성공 - newUser: ${data.newUser}")

                    // 신규 유저: 카카오 프로필 정보로 초기 프로필 설정
                    if (data.newUser) {
                        try {
                            userRepository.updateProfile(
                                com.example.myfrigelocal.network.UserProfileUpdateRequest(
                                    nickname = nickname,
                                    profileImageUrl = profileImageUrl
                                )
                            )
                            Log.d("LoginViewModel", "카카오 신규 유저 프로필 초기화 완료")
                        } catch (e: Exception) {
                            Log.w("LoginViewModel", "카카오 프로필 초기화 실패 (무시): ${e.message}")
                        }
                    }

                    _loginState.value = LoginState.Success(isNewUser = data.newUser)
                } else {
                    Log.e("LoginViewModel", "카카오 로그인 실패 - code: ${response.code}")
                    _loginState.value = LoginState.Error("카카오 로그인에 실패했어요.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "카카오 예외: ${e.message}")
                _loginState.value = LoginState.Error(e.message ?: "알 수 없는 오류가 발생했어요.")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}