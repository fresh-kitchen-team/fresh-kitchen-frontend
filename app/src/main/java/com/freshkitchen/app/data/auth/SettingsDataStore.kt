package com.freshkitchen.app.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object SettingsDataStore {

    private val EXPIRY_ALARM_ENABLED  = booleanPreferencesKey("expiry_alarm_enabled")
    private val INQUIRY_ALARM_ENABLED = booleanPreferencesKey("inquiry_alarm_enabled")

    fun getExpiryAlarmEnabled(context: Context): Flow<Boolean> =
        context.settingsDataStore.data.map { it[EXPIRY_ALARM_ENABLED] ?: true }

    fun getInquiryAlarmEnabled(context: Context): Flow<Boolean> =
        context.settingsDataStore.data.map { it[INQUIRY_ALARM_ENABLED] ?: true }

    suspend fun setExpiryAlarmEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[EXPIRY_ALARM_ENABLED] = enabled }
    }

    suspend fun setInquiryAlarmEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[INQUIRY_ALARM_ENABLED] = enabled }
    }
}
