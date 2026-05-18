package com.example.siheunggagae.data.network

import com.example.siheunggagae.data.local.TokenManager
import com.example.siheunggagae.data.model.ChatMessageItem
import com.example.siheunggagae.data.model.WsEvent
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private const val WS_BASE = "wss://backend-production-f6c0.up.railway.app/api/v1/ws/applications"
private const val CLOSE_NORMAL = 1000
private const val CLOSE_TOKEN_EXPIRED = 4401
private const val CLOSE_FORBIDDEN = 4403
private const val MAX_RECONNECT_DELAY_MS = 30_000L

/**
 * WebSocket 매니저 (공통-4)
 *
 * 연결 방법:
 * ```
 * val manager = ChatWebSocketManager(tokenManager) { authRepository.refresh() }
 * manager.connect(applicationId)
 * lifecycleScope.launch { manager.events.collect { event -> ... } }
 * // 화면 종료 시
 * manager.disconnect()
 * ```
 *
 * 서버 동작:
 * - 메시지 전송은 REST(POST /messages)로만 해야 함 — WS로 쏘면 서버가 무시함
 * - REST POST 성공 후 서버가 WS로 양쪽에 broadcast
 */
class ChatWebSocketManager(
    private val tokenManager: TokenManager,
    private val refreshToken: suspend () -> Boolean,
) {
    private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WsEvent> = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var applicationId: Int = -1

    private val shouldReconnect = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    private val reconnectAttempts = AtomicInteger(0)

    fun connect(applicationId: Int) {
        this.applicationId = applicationId
        shouldReconnect.set(true)
        reconnectAttempts.set(0)
        openConnection()
    }

    fun disconnect() {
        shouldReconnect.set(false)
        webSocket?.close(CLOSE_NORMAL, "User closed")
        webSocket = null
        scope.cancel()
    }

    private fun openConnection() {
        if (!isConnecting.compareAndSet(false, true)) return

        val token = tokenManager.accessToken
        if (token == null) {
            isConnecting.set(false)
            return
        }

        val url = "$WS_BASE/$applicationId?token=$token"
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, Listener())
    }

    private inner class Listener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            isConnecting.set(false)
            reconnectAttempts.set(0)
            emit(WsEvent.Connected)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            runCatching {
                gson.fromJson(text, ChatMessageItem::class.java)
            }.onSuccess { message ->
                emit(WsEvent.Message(message))
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            isConnecting.set(false)
            when (code) {
                CLOSE_TOKEN_EXPIRED -> handleTokenExpired()
                CLOSE_FORBIDDEN -> {
                    shouldReconnect.set(false)
                    emit(WsEvent.Unauthorized)
                }
                else -> scheduleReconnectIfNeeded()
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            isConnecting.set(false)
            scheduleReconnectIfNeeded()
        }
    }

    private fun handleTokenExpired() {
        scope.launch {
            val refreshed = refreshToken()
            if (refreshed && shouldReconnect.get()) {
                openConnection()
            } else {
                shouldReconnect.set(false)
                emit(WsEvent.Unauthorized)
            }
        }
    }

    private fun scheduleReconnectIfNeeded() {
        if (!shouldReconnect.get()) return
        emit(WsEvent.Disconnected)
        scope.launch {
            val attempt = reconnectAttempts.getAndIncrement()
            // 지수 백오프: 1s, 2s, 4s, 8s, … 최대 30s
            val delayMs = minOf(1_000L shl attempt.coerceAtMost(5), MAX_RECONNECT_DELAY_MS)
            delay(delayMs)
            if (shouldReconnect.get()) openConnection()
        }
    }

    private fun emit(event: WsEvent) {
        scope.launch { _events.emit(event) }
    }
}
