package com.zeddihub.gearos.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth token + TOTP seedy v Android Keystore-backed encrypted prefs.
 * M1 vyplní pairing flow (zápis), M2+ čte při API callech, M4 uloží TOTP seedy.
 */
@Singleton
class SecureTokenStorage @Inject constructor(
    private val ctx: Context,
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            ctx,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun setAuthToken(token: String?) {
        prefs.edit().apply {
            if (token == null) remove(KEY_AUTH_TOKEN) else putString(KEY_AUTH_TOKEN, token)
        }.apply()
    }

    fun getTotpSeed(label: String): String? = prefs.getString(KEY_TOTP_PREFIX + label, null)

    fun setTotpSeed(label: String, seedBase32: String) {
        prefs.edit().putString(KEY_TOTP_PREFIX + label, seedBase32).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val FILE_NAME = "gearos_secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_TOTP_PREFIX = "totp_seed_"
    }
}
