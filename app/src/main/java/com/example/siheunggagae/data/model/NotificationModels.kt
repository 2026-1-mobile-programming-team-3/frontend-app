package com.example.siheunggagae.data.model

data class DeviceRegisterRequest(
    val fcmToken: String,
    val deviceName: String? = null,
)

data class DeviceRegisteredResponse(
    val id: Int,
    val registeredAt: String,
)

enum class NotificationCategory {
    VOLUNTEER, MATCH, REVIEW, NEWS, POLICY, SYSTEM
}

data class NotificationItem(
    val id: Int,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val link: String?,
    val createdAt: String,
)

data class NotificationListResponse(
    val items: List<NotificationItem>,
    val total: Int,
    val unreadCount: Int,
    val page: Int,
    val size: Int,
)

data class NotificationReadResponse(
    val id: Int,
    val isRead: Boolean,
    val updated: Boolean,
)

data class NotificationSettingsResponse(
    val settings: Map<String, Boolean>,
)

data class NotificationSettingsUpdateRequest(
    val settings: Map<String, Boolean>,
)

data class UnreadCountResponse(
    val unreadCount: Int,
)

data class MarkAllReadResponse(
    val updatedCount: Int,
)

data class NotificationDeleteRequest(
    @com.google.gson.annotations.SerializedName("notification_ids")
    val notificationIds: List<Int>,
)
