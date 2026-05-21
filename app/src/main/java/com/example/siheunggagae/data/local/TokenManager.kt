package com.example.siheunggagae.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_auth_tokens",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)

    val expiresAt: Long
        get() = prefs.getLong(KEY_EXPIRES_AT, 0L)

    private val _localProfileImageUri = MutableStateFlow(prefs.getString(KEY_PROFILE_IMAGE, null))
    val localProfileImageUri: StateFlow<String?> = _localProfileImageUri

    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Int = 3600) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + expiresIn * 1000L)
            .apply()
    }

    fun setLocalProfileImageUri(uri: String?) {
        prefs.edit().apply {
            if (uri != null) putString(KEY_PROFILE_IMAGE, uri) else remove(KEY_PROFILE_IMAGE)
        }.apply()
        _localProfileImageUri.value = uri
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
        _localProfileImageUri.value = null
    }

    // 만료 30초 전부터 갱신이 필요하다고 판단
    fun isAccessTokenExpired(): Boolean {
        val exp = expiresAt
        return exp == 0L || System.currentTimeMillis() > exp - 30_000L
    }

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_PROFILE_IMAGE = "local_profile_image_uri"
    }
}
