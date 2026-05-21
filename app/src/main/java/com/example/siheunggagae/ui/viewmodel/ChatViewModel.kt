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

    // 👈 [실 서비스 스펙] 임의의 숫자 31을 지우고, 무효한 ID 값인 -1을 초기값으로 설정합니다.
    var myUserId by mutableStateOf(-1)
        private set

    fun initChatRoom(matchId: Int, applicationId: Int) {
        this.mId = matchId
        this.appId = applicationId

        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            try {
                // 1. [태은-6.3] REST API로 과거 메시지 및 매칭 상세 정보 동시 조회
                val msgResponse = api.getChatMessages(matchId, applicationId)
                val detailResponse = api.getMatchDetail(matchId)
                val meResponse = api.getMe()

                if (msgResponse.isSuccessful && detailResponse.isSuccessful && meResponse.isSuccessful) {
                    matchDetail = detailResponse.body()

                    // 👈 [실 서비스 스펙] 내 고유 ID가 정상적으로 조회되지 않으면 예외 에러 처리 후 차단합니다.
                    val myId = meResponse.body()?.id
                    if (myId == null) {
                        _uiState.value = ChatUiState.Error("인증 정보가 만료되었습니다. 다시 로그인 해주세요.")
                        return@launch
                    }

                    myUserId = myId // 👈 안전하게 확인된 내 실제 유저 ID만 동적으로 저장합니다.
                    isMyRequest = matchDetail?.author?.userId == myId

                    // 상대방 닉네임 판별 (내가 요청자면 지원자 이름, 내가 지원자면 요청자 이름)
                    opponentNickname = if (isMyRequest) {
                        val apps = api.getApplications(matchId).body()
                        apps?.items?.find { it.applicationId == applicationId }?.applicant?.nickname ?: "지원자"
                    } else {
                        matchDetail?.author?.nickname ?: "요청자"
                    }

                    currentMessages.clear()
                    currentMessages.addAll(msgResponse.body()?.items?.reversed() ?: emptyList()) // 오래된 순 정렬

                    _uiState.value = ChatUiState.Success(currentMessages.toList(), matchDetail, opponentNickname, isMyRequest)

                    // 2. [태은-6.3] 웹소켓 실시간 파이프라인 가동
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
            // [태은-6.6] 웹소켓으로 수신된 메시지를 실시간 리스트 맨 아래에 추가(append)
            wsManager.events.collect { event ->
                when (event) {
                    is WsEvent.Message -> {
                        // 중복 수신 방어 처리 후 추가
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

    // [태은-6.5] 메시지 전송은 웹소켓이 아닌 항상 REST (POST) API 호출
    fun sendTextMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ChatMessageCreateRequest(content)
                val response = api.sendChatMessage(mId, appId, body)

                // 👈 [실 서비스 스펙] 네트워크 지연 시 말풍선이 안 뜨는 현상을 방지하기 위해,
                // POST 성공 시 응답 메시지를 내 화면 리스트에 즉시 반영(낙관적 업데이트)합니다.
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
                // 전송 실패 에러 대응
            }
        }
    }

    // [태은-6.9] 헤더의 수락 버튼 액션 연동
    fun acceptVolunteer(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ApplicationActionRequest(action = "ACCEPT")
                val response = api.respondToApplication(mId, appId, body)
                if (response.isSuccessful) {
                    onComplete()
                }
            } catch (e: Exception) {
                // 수락 실패 예외 처리
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsManager.disconnect() // [태은-6.8] 화면 종료 시 안전하게 웹소켓 해제
    }

    class Factory(private val api: AuthApiService, private val ws: ChatWebSocketManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatViewModel(api, ws) as T
    }
}