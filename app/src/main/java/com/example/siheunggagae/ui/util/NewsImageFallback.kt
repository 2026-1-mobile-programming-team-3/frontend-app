package com.example.siheunggagae.ui.util

import com.example.siheunggagae.R

// imageUrl이 비어있을 때 뉴스 카드/상세 헤더에 표시할 기본 이미지 풀.
// res/drawable에 미리 번들된 반려동물/일러스트 사진 8장 중 seed 기반으로 1장 선택.
private val NEWS_FALLBACK_IMAGES = intArrayOf(
    R.drawable.img_news_thumb_1,
    R.drawable.img_news_thumb_2,
    R.drawable.img_news_thumb_3,
    R.drawable.img_news_thumb_4,
    R.drawable.img_news_thumb_5,
    R.drawable.img_news_banner,
    R.drawable.img_home_news_thumb,
    R.drawable.img_news_detail_body,
)

fun newsFallbackDrawable(seed: String?): Int {
    val key = seed?.takeIf { it.isNotEmpty() } ?: "default"
    val size = NEWS_FALLBACK_IMAGES.size
    val idx = ((key.hashCode() % size) + size) % size
    return NEWS_FALLBACK_IMAGES[idx]
}
