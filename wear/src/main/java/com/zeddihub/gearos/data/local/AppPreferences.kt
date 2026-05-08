package com.zeddihub.gearos.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("gearos_prefs")

/**
 * Non-sensitive prefs (UI state, last-seen ids).
 * Sensitive data (auth token, TOTP seedy) patří do EncryptedSharedPreferences přes [SecureTokenStorage].
 */
@Singleton
class AppPreferences @Inject constructor(
    private val ctx: Context,
) {
    private object Keys {
        val LAST_USERNAME = stringPreferencesKey("last_username")
        val LAST_ALERT_ID = stringPreferencesKey("last_alert_id")
    }

    val lastUsername: Flow<String?> = ctx.dataStore.data.map { it[Keys.LAST_USERNAME] }
    val lastAlertId: Flow<String?> = ctx.dataStore.data.map { it[Keys.LAST_ALERT_ID] }

    suspend fun setLastUsername(value: String) {
        ctx.dataStore.edit { it[Keys.LAST_USERNAME] = value }
    }

    suspend fun setLastAlertId(value: String) {
        ctx.dataStore.edit { it[Keys.LAST_ALERT_ID] = value }
    }

    suspend fun clear() {
        ctx.dataStore.edit { it.clear() }
    }

    private fun <T> Preferences.maybe(key: Preferences.Key<T>): T? = this[key]
}
