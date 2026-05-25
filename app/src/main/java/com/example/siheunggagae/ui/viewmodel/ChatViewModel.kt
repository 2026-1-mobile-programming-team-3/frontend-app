package com.example.siheunggagae.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.ChatMessageItem
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.data.model.WsEvent
import com.example.siheunggagae.data.network.ChatWebSocketManager
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(
        val messages: List<ChatMessageItem>,
        val matchDetail: MatchDetailResponse?,
        val opponentNickname: String,
        val isMyRequest: Boolean,
        val hasMore: Boolean = false
    ) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(
    private val api: AuthApiService,
    private val wsManager: ChatWebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState

    private val currentMessages = mutableListOf<ChatMessageItem>()
    private var matchDetail: MatchDetailResponse? = null
    private var opponentNickname: String = "상대방"
    private var isMyRequest: Boolean = false

    private var hasMore: Boolean = false
    private var isLoadingMore: Boolean = false

    private var mId: Int = -1
    private var appId: Int = -1

    var myUserId by mutableStateOf(-1)
        private set

    fun initChatRoom(matchId: Int, applicationId: Int) {
        this.mId = matchId
        this.appId = applicationId

        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            try {
                val msgResponse = api.getChatMessages(matchId, applicationId)
                val detailResponse = api.getMatchDetail(matchId)
                val meResponse = api.getMe()

                if (msgResponse.isSuccessful && detailResponse.isSuccessful && meResponse.isSuccessful) {
                    matchDetail = detailResponse.body()

                    val myId = meResponse.body()?.id
                    if (myId == null) {
                        _uiState.value = ChatUiState.Error("인증 정보가 만료되었습니다. 다시 로그인 해주세요.")
                        return@launch
                    }

                    myUserId = myId
                    isMyRequest = matchDetail?.author?.userId == myId

                    opponentNickname = if (isMyRequest) {
                        val apps = api.getApplications(matchId).body()
                        apps?.items?.find { it.applicationId == applicationId }?.applicant?.nickname ?: "지원자"
                    } else {
                        matchDetail?.author?.nickname ?: "요청자"
                    }

                    hasMore = msgResponse.body()?.hasMore ?: false

                    currentMessages.clear()
                    currentMessages.addAll(msgResponse.body()?.items?.reversed() ?: emptyList())

                    _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest, hasMore)

                    wsManager.connect(applicationId)
                    observeWebSocket()
                } else {
                    _uiState.value = ChatUiState.Error("채팅방 데이터를 불러오지 못했습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    /**
     * ─── 🌟 [태은-6.7 수정 완료] 확장된 오버로딩 파라미터를 사용하여 정밀 과거 페이징 수행 ───
     */
    fun loadMoreMessages() {
        if (isLoadingMore || !hasMore || currentMessages.isEmpty()) return
        isLoadingMore = true

        viewModelScope.launch {
            try {
                val oldestMessageId = currentMessages.first().id
                val response = api.getChatMessages(mId, appId, beforeId = oldestMessageId)

                val pageData = response.body()
                if (response.isSuccessful && pageData != null) {
                    hasMore = pageData.hasMore

                    val oldMessages = pageData.items.reversed()
                    currentMessages.addAll(0, oldMessages)

                    _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest, hasMore)
                }
            } catch (e: Exception) {
                println("과거 메시지 페이징 로드 실패: ${e.message}")
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            wsManager.events.collect { event ->
                when (event) {
                    is WsEvent.Message -> {
                        if (currentMessages.none { it.id == event.message.id }) {
                            currentMessages.add(event.message)
                            _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest, hasMore)
                        }
                    }
                    is WsEvent.Unauthorized -> {
                        _uiState.value = ChatUiState.Error("채팅 권한이 없습니다. (4403)")
                    }
                    else -> {}
                }
            }
        }
    }

    fun sendTextMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ChatMessageCreateRequest(content)
                val response = api.sendChatMessage(mId, appId, body)

                val resBody = response.body()
                if (response.isSuccessful && resBody != null) {
                    val newMessage = ChatMessageItem(
                        id = resBody.id,
                        content = resBody.content,
                        senderId = resBody.senderId,
                        createdAt = resBody.createdAt
                    )
                    if (currentMessages.none { it.id == newMessage.id }) {
                        currentMessages.add(newMessage)
                        _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest, hasMore)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun reportMessage(messageId: Int, targetUserId: Int, reason: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ChatReportCreateRequest(
                    applicationId = this@ChatViewModel.appId,
                    targetUserId = targetUserId,
                    messageId = messageId,
                    reason = reason
                )
                val response = api.reportChatMessage(body)
                onComplete(response.isSuccessful)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    /**
     * ─── 🌟 [태은-10.4] 상대방 유저 영구 차단 (POST /users/me/blocks) ───
     */
    fun blockUser(targetUserId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // TODO: 나중에 강문님이 백엔드 차단 엔드포인트 뚫어주시면 레포지토리 연동할 구역
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    /**
     * ─── 🌟 [태은-10.2] 유저 자체 전역 신고하기 (POST /reports) ───
     */
    fun reportUser(targetUserId: Int, reason: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // TODO: 나중에 강문님이 백엔드 유저 신고 엔드포인트 뚫어주시면 레포지토리 연동할 구역
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun acceptVolunteer(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ApplicationActionRequest(action = "ACCEPT")
                val response = api.respondToApplication(mId, appId, body)
                if (response.isSuccessful) {
                    onComplete()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun cancelVolunteer(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.cancelMatching(mId, appId)
                if (response.isSuccessful) {
                    onComplete()
                } else {
                    println("매칭 취소 실패: 코드 ${response.code()}, 메시지: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("매칭 취소 통신 중 네트워크 에러 발생: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsManager.disconnect()
    }

    class Factory(private val api: AuthApiService, private val ws: ChatWebSocketManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatViewModel(api, ws) as T
    }
}