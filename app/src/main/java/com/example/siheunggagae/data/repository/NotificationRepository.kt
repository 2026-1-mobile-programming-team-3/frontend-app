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
        // page 1에서만 로컬 알림을 앞에 붙임
        if (page == 1 && localStore != null && serverResp.isSuccessful) {
            val body = serverResp.body() ?: return serverResp
            val local = localStore.getAll()
            if (local.isEmpty()) return serverResp
            val merged = local + body.items
            return Response.success(
                body.copy(
                    items = merged,
                    total = body.total + local.size,
                    unreadCount = body.unreadCount + local.count { !it.isRead },
                ),
            )
        }
        return serverResp
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
}
