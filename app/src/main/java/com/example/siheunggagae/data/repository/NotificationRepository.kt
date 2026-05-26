package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.local.LocalNotificationStore
import com.example.siheunggagae.data.model.MarkAllReadResponse
import com.example.siheunggagae.data.model.NotificationListResponse
import com.example.siheunggagae.data.model.NotificationReadResponse
import com.example.siheunggagae.data.model.UnreadCountResponse
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class NotificationRepository(
    private val localStore: LocalNotificationStore? = null,
) {
    private val api = RetrofitClient.api

    suspend fun getNotifications(
        page: Int = 1,
        size: Int = 20,
    ): Response<NotificationListResponse> {
        val serverResp = api.getNotifications(
            category = null,
            isRead = null,
            page = page,
            size = size,
        )
        if (!serverResp.isSuccessful || localStore == null) return serverResp

        val body = serverResp.body() ?: return serverResp
        val hiddenIds = localStore.getHiddenServerIds()

        // 클라이언트에서 삭제 처리된 서버 알림 필터링
        val filteredServer = if (hiddenIds.isEmpty()) body.items
                             else body.items.filter { it.id !in hiddenIds }
        val removedUnread = body.items.count { it.id in hiddenIds && !it.isRead }

        return if (page == 1) {
            val local = localStore.getAll()
            Response.success(
                body.copy(
                    items = local + filteredServer,
                    total = maxOf(0, body.total - hiddenIds.size) + local.size,
                    unreadCount = maxOf(0, body.unreadCount - removedUnread) + local.count { !it.isRead },
                ),
            )
        } else {
            Response.success(
                body.copy(
                    items = filteredServer,
                    total = maxOf(0, body.total - hiddenIds.size),
                ),
            )
        }
    }

    suspend fun getUnreadCount(): Response<UnreadCountResponse> =
        api.getUnreadCount()

    suspend fun markRead(id: Int): Response<NotificationReadResponse> =
        api.markNotificationRead(id)

    suspend fun markAllRead(): Response<MarkAllReadResponse> =
        api.markAllNotificationsRead()

    // 로컬 알림 전용 (id < 0)
    fun markLocalRead(id: Int) = localStore?.markRead(id)
    fun markAllLocalRead() = localStore?.markAllRead()
    fun deleteLocalNotifications(ids: List<Int>) = localStore?.deleteItems(ids.toSet())

    // 서버 알림 클라이언트 측 영구 숨김 (삭제 API 없음)
    fun addHiddenServerIds(ids: List<Int>) = localStore?.addHiddenServerIds(ids.toSet())
}
