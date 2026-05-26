package com.example.siheunggagae.data.local

import android.content.Context
import com.example.siheunggagae.data.model.NotificationCategory
import com.example.siheunggagae.data.model.NotificationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant

class LocalNotificationStore(context: Context) {

    private val prefs = context.getSharedPreferences("local_notifications", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<NotificationItem>>() {}.type

    // 서버 ID는 양수 → 로컬은 음수로 충돌 방지
    private fun nextId(): Int {
        val current = prefs.getInt("next_id", -1)
        prefs.edit().putInt("next_id", current - 1).apply()
        return current
    }

    fun add(category: NotificationCategory, title: String, body: String) {
        val items = getAll().toMutableList()
        items.add(
            0,
            NotificationItem(
                id = nextId(),
                category = category,
                title = title,
                body = body,
                isRead = false,
                link = null,
                createdAt = Instant.now().toString(),
            ),
        )
        save(items)
    }

    fun getAll(): List<NotificationItem> {
        val json = prefs.getString(KEY_LIST, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<NotificationItem>>(json, listType)
        }.getOrElse { emptyList() }
    }

    fun markRead(id: Int) {
        save(getAll().map { if (it.id == id) it.copy(isRead = true) else it })
    }

    fun markAllRead() {
        save(getAll().map { it.copy(isRead = true) })
    }

    fun clearAll() {
        prefs.edit().remove(KEY_LIST).putInt("next_id", -1).apply()
    }

    private fun save(items: List<NotificationItem>) {
        prefs.edit().putString(KEY_LIST, gson.toJson(items)).apply()
    }

    companion object {
        private const val KEY_LIST = "items"
    }
}
