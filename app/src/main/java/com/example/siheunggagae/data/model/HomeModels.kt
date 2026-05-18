package com.example.siheunggagae.data.model

data class DashboardResponse(
    val nickname: String,
    val role: UserRole,
    val regionDong: String?,
    val walkScore: Int,
    val weather: String?,
    val temperatureCelsius: Int?,
    val airQuality: String?,
    val pendingMatchCount: Int,
    val nearestDDay: Int?,
    val nearbyStoreCount: Int,
    val stores: List<StoreResponse>,
    val news: List<NewsItem>,
)

data class NewsItem(
    val id: Int,
    val title: String,
    val category: String,
    val publishedAt: String,
    val thumbnailUrl: String?,
    val source: String?,
)
