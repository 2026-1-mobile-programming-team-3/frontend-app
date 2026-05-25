package com.example.siheunggagae.data.network

import android.os.Build
import com.example.siheunggagae.data.local.TokenManager
import com.example.siheunggagae.data.model.DeviceRegisterRequest
import com.example.siheunggagae.data.network.api.AuthApiService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * FCM 토큰을 백엔드에 등록하는 헬퍼 (공통-5)
 *
 * 호출 시점 3곳:
 *  1. 앱 부팅 — SiheungGagaeApp.onCreate (이미 로그인된 경우)
 *  2. 로그인 직후 — AuthRepository.login 성공 후
 *  3. FCM 토큰 갱신 콜백 — FcmService.onNewToken
 */
class FcmTokenManager(
    private val api: AuthApiService,
    private val tokenManager: TokenManager,
) {
    /** Firebase에서 현재 토큰을 받아 백엔드에 등록. 로그인 상태가 아니면 무시. */
    suspend fun registerCurrentDevice() {
        if (tokenManager.accessToken == null) return
        val fcmToken = getFirebaseToken() ?: return
        registerWithToken(fcmToken)
    }

    /** onNewToken 콜백처럼 토큰이 이미 알려진 경우 직접 등록. */
    suspend fun registerWithToken(fcmToken: String) {
        if (tokenManager.accessToken == null) return
        val deviceName = Build.MODEL.take(100)
        runCatching {
            api.registerDevice(DeviceRegisterRequest(fcmToken, deviceName))
        }
        // 실패해도 푸시가 안 올 뿐 — 앱 동작에는 영향 없음
    }

    /**
     * 로그아웃 시 호출. Firebase FCM 토큰을 삭제해 이 기기로의 알림을 즉시 차단.
     * 다음 로그인 시 새 토큰이 발급되어 신규 계정에만 등록된다.
     */
    suspend fun deleteDeviceToken() {
        runCatching {
            suspendCancellableCoroutine { cont ->
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }

    private suspend fun getFirebaseToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (cont.isActive) {
                cont.resume(if (task.isSuccessful) task.result else null)
            }
        }
    }
}
