package com.example.siheunggagae.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.siheunggagae.data.model.UserMeResponse

/**
 * 매칭 화면 등에서 즉시 필요한 "본인 식별" 데이터를 캐시.
 *
 * - id: 본인 글 식별 (MatchListItem.authorUserId 와 비교)
 * - nickname: id 가 응답에 없는 매칭/스크린 대비 폴백 비교
 * - role: 봉사자 전용 카테고리 권한 안내
 *
 * `UserRepository.getMe()` 호출 결과를 `save()` 로 저장. 로그아웃 시 `clear()`.
 */
class CurrentUserStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(me: UserMeResponse) {
        prefs.edit()
            .putInt(KEY_ID, me.id)
            .putString(KEY_NICKNAME, me.nickname)
            .putString(KEY_ROLE, me.role.name)
            .apply()
    }

    fun userId(): Int? =
        if (prefs.contains(KEY_ID)) prefs.getInt(KEY_ID, -1).takeIf { it >= 0 } else null

    fun nickname(): String? = prefs.getString(KEY_NICKNAME, null)

    /** 봉사자 역할 여부. 미캐시면 false. */
    fun isVolunteer(): Boolean {
        val role = prefs.getString(KEY_ROLE, null) ?: return false
        // UserRole.VOLUNTEER — UserRole.name 으로 저장되므로 대소문자 무관 비교
        return role.equals("VOLUNTEER", ignoreCase = true)
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_ID)
            .remove(KEY_NICKNAME)
            .remove(KEY_ROLE)
            .apply()
    }

    companion object {
        private const val PREFS = "current_user_store"
        private const val KEY_ID = "id"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_ROLE = "role"
    }
}
