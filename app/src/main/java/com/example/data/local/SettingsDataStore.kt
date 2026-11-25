package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.posSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aadi_aarambh_settings"
)

class SettingsDataStore(private val context: Context) {

    companion object {
        const val PLACEHOLDER_URL = "[PASTE_YOUR_WEB_APP_URL_HERE]"
        private val KEY_WEB_APP_URL = stringPreferencesKey("web_app_url")
    }

    val webAppUrlFlow: Flow<String> = context.posSettingsDataStore.data.map { prefs ->
        prefs[KEY_WEB_APP_URL] ?: ""
    }

    suspend fun getWebAppUrl(): String = webAppUrlFlow.first()

    suspend fun saveWebAppUrl(url: String) {
        val cleaned = url.trim()
        context.posSettingsDataStore.edit { prefs ->
            prefs[KEY_WEB_APP_URL] = cleaned
        }
    }
}
