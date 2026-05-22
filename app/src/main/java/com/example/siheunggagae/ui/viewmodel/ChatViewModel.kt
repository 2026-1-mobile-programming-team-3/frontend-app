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
        val isMyRequest: Boolean
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

                    currentMessages.clear()
                    currentMessages.addAll(msgResponse.body()?.items?.reversed() ?: emptyList())

                    _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest)

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

    private fun observeWebSocket() {
        viewModelScope.launch {
            wsManager.events.collect { event ->
                when (event) {
                    is WsEvent.Message -> {
                        if (currentMessages.none { it.id == event.message.id }) {
                            currentMessages.add(event.message)
                            _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest)
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

                if (response.isSuccessful && response.body() != null) {
                    val resBody = response.body()!!
                    val newMessage = ChatMessageItem(
                        id = resBody.id,
                        content = resBody.content,
                        senderId = resBody.senderId,
                        createdAt = resBody.createdAt
                    )
                    if (currentMessages.none { it.id == newMessage.id }) {
                        currentMessages.add(newMessage)
                        _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    // [수락 피드]
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

    // ─── 🌟 [3단계 수정 완료] 새롭게 정의된 매칭 중도 취소 전용 엔드포인트 연동 ───
    fun cancelVolunteer(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // 422 에러를 유발하던 respondToApplication 대신 신규 개방된 cancelMatching API 직접 호출
                val response = api.cancelMatching(mId, appId)

                if (response.isSuccessful) {
                    onComplete()
                } else {
                    // 예외 발생 시 디버깅을 위한 상태 추적 콘솔로그 확보
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