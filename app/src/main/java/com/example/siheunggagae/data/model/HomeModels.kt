package com.example.siheunggagae.data.model

data class DashboardResponse(
    val user: DashboardUser?,
    val walkScore: Int?,
    val weather: DashboardWeather?,
    val nearbyStoreCount: Int?,
    val myMatchSummary: DashboardMatchSummary?,
    val unreadNotificationCount: Int?,
)

data class DashboardUser(
    val nickname: String?,
    val role: UserRole?,
    val regionSi: String?,
    val regionDong: String?,
)

data class DashboardWeather(
    val condition: String?,
    val tempC: Double?,
    val dustGrade: String?,
)

data class DashboardMatchSummary(
    val asAuthor: DashboardMatchEntry?,
    val asApplicant: DashboardMatchEntry?,
)

data class DashboardMatchEntry(
    val matchId: Int? = null,
    val title: String? = null,
    val desiredDate: String? = null,
    val status: String? = null,
    val applicationsCount: Int? = null,
)

// ── 뉴스 (News) ────────────────────────────────────────────────────────────────

data class NewsItem(
    val newsId: String? = null,
    val title: String? = null,
    val summary: String? = null,
    val publishedDate: String? = null,
    val link: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val publisher: String? = null,
)

data class NewsListResponse(
    val news: List<NewsItem>? = null,
)

data class NewsDetailResponse(
    val newsId: String? = null,
    val title: String? = null,
    val content: String? = null,
    val officialLink: String? = null,
    val publishedDate: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val publisher: String? = null,
)

data class CalendarEvent(
    val eventId: Int? = null,
    val title: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
)

data class CalendarResponse(
    val events: List<CalendarEvent>? = null,
)

data class DailyEvent(
    val eventId: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val time: String? = null,
)

data class DailyCalendarResponse(
    val dailyEvents: List<DailyEvent>? = null,
)
