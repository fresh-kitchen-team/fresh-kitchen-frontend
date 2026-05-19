package com.example.myfrigelocal.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

object TokenDataStore {

    private val ACCESS_TOKEN  = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    fun getAccessToken(context: Context): Flow<String?> =
        context.dataStore.data.map { it[ACCESS_TOKEN] }

    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN]  = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearTokens(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}