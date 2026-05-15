package com.example.myfrigelocal.data.auth

import java.util.concurrent.atomic.AtomicReference

/**
 * In-memory access token holder for API calls (e.g. Chat Retrofit [com.example.myfrigelocal.data.remote.AuthInterceptor]).
 *
 * **Current project state:** There is no persisted token yet. [com.example.myfrigelocal.ui.screens.LoginScreen]
 * still uses `TODO: 구글/카카오 OAuth` and navigates home **without** calling any login API or this store.
 *
 * **What to do when auth exists:**
 * - After your login / refresh API returns `accessToken`, call [setAccessToken].
 * - Prefer persisting the token (DataStore / EncryptedSharedPreferences) and hydrating this store on app start.
 *
 * **Temporary local testing (no hardcoded tokens in source):**
 * - Use a debug menu, `adb` test hook, or Android Studio debugger to invoke [setAccessToken] with a token issued by your backend.
 * - Do not commit real tokens into the repo.
 */
object AuthTokenStore {

    private val accessTokenRef = AtomicReference<String?>(null)

    fun getAccessToken(): String? = accessTokenRef.get()?.trim()?.takeIf { it.isNotEmpty() }

    fun setAccessToken(token: String?) {
        val trimmed = token?.trim()?.takeIf { it.isNotEmpty() }
        accessTokenRef.set(trimmed)
    }

    fun clear() {
        accessTokenRef.set(null)
    }
}
