package com.example.siheunggagae.ui.util

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.siheunggagae.R

/**
 * 카테고리(영문 코드) → emoji, drawable, color, gradient, 한국어 라벨 단일 소스.
 *
 * 사용 예:
 * ```
 * val v = CategoryVisual.forCategory("CAFE")
 * Box(background = Brush.linearGradient(v.gradient)) { Icon(v.drawableRes, tint = v.color) }
 * ```
 */
data class CategoryVisual(
    val code: String,
    val koreanLabel: String,
    val emoji: String,
    @DrawableRes val drawableRes: Int,
    val colorInt: Int,
    val gradient: List<Color>,
) {
    val color: Color get() = Color(colorInt)

    companion object {
        val DEFAULT = CategoryVisual(
            code = "DEFAULT",
            koreanLabel = "매장",
            emoji = "★",
            drawableRes = R.drawable.ic_star,
            colorInt = 0xFF614B3A.toInt(),
            gradient = listOf(Color(0xFFE8D3C2), Color(0xFFFFFAF7)),
        )

        private val MAP: Map<String, CategoryVisual> = listOf(
            CategoryVisual("CAFE",       "카페",     "☕", R.drawable.ic_coffee,       0xFF8A6E58.toInt(),
                gradient = listOf(Color(0xFFFFEDD4), Color(0xFFF7A35B))),
            CategoryVisual("PARK",       "공원",     "🌳", R.drawable.ic_forest,       0xFF4CAF50.toInt(),
                gradient = listOf(Color(0xFFD0FEE1), Color(0xFFB2DFBF))),
            CategoryVisual("HOSPITAL",   "동물병원", "🏥", R.drawable.ic_health_cross, 0xFFF04268.toInt(),
                gradient = listOf(Color(0xFFFEE7EC), Color(0xFFFFC1CC))),
            CategoryVisual("GROOMING",   "미용",     "✂",  R.drawable.ic_content_cut,  0xFF9C27B0.toInt(),
                gradient = listOf(Color(0xFFEBD4F5), Color(0xFFD1B0E5))),
            CategoryVisual("RESTAURANT", "음식점",   "🍽", R.drawable.ic_utensils,     0xFFF7A35B.toInt(),
                gradient = listOf(Color(0xFFFFEDD4), Color(0xFFFFCB91))),
            CategoryVisual("PET_HOTEL",  "펫호텔",   "🏨", R.drawable.ic_hotel,        0xFF614B3A.toInt(),
                gradient = listOf(Color(0xFFE8D3C2), Color(0xFFC4A882))),
        ).associateBy { it.code }

        fun forCategory(category: String?): CategoryVisual =
            category?.trim()?.uppercase()?.let { MAP[it] } ?: DEFAULT
    }
}
