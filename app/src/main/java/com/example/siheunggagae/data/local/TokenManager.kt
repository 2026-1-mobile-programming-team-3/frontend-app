package com.example.siheunggagae.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = openPrefs(context)

    companion object {
        private const val PREFS_NAME = "secure_auth_tokens"
        const val KEY_ACCESS    = "access_token"
        const val KEY_REFRESH   = "refresh_token"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_PROFILE_IMAGE = "local_profile_image_uri"

        private fun buildMaster(context: Context) =
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        private fun createPrefs(context: Context): SharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                buildMaster(context),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

        private fun openPrefs(context: Context): SharedPreferences =
            try {
                createPrefs(context)
            } catch (_: Exception) {
                // Keystore 키-파일 불일치(OS 업그레이드·재설치 등) → 파일·키를 모두 지우고 재생성
                context.deleteSharedPreferences(PREFS_NAME)
                runCatching {
                    val ks = java.security.KeyStore.getInstance("AndroidKeyStore")
                    ks.load(null)
                    ks.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                }
                createPrefs(context)
            }
    }

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

}
