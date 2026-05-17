package com.example.siheunggagae.data.model

data class ChatMessageCreateRequest(
    val content: String,
)

data class ChatMessageCreatedResponse(
    val id: Int,
    val content: String,
    val senderId: Int?,
    val createdAt: String,
)

data class ChatMessageItem(
    val id: Int,
    val content: String,
    val senderId: Int?,
    val createdAt: String,
)

data class ChatMessageListResponse(
    val items: List<ChatMessageItem>,
    val hasMore: Boolean,
    val opponentLastReadAt: String?,
)

data class ChatThreadApplicant(
    val id: Int,
    val nickname: String?,
)

data class ChatThreadItem(
    val applicationId: Int,
    val applicant: ChatThreadApplicant,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val unreadCount: Int,
    val applicationStatus: String,
)

data class ChatThreadListResponse(
    val items: List<ChatThreadItem>,
)

sealed class WsEvent {
    /** 연결 성공 (최초 연결 또는 재연결) */
    data object Connected : WsEvent()
    /** 서버에서 broadcast된 새 메시지 */
    data class Message(val message: ChatMessageItem) : WsEvent()
    /** 네트워크 끊김 — 재연결 시도 중 */
    data object Disconnected : WsEvent()
    /** 4403: 권한 없음 — UI를 에러 상태로 전환 */
    data object Unauthorized : WsEvent()
}
