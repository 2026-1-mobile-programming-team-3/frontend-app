package com.example.siheunggagae.map

import com.example.siheunggagae.data.model.StoreViewportItem

/** lat/lng -> 화면 픽셀 좌표 변환. null = 화면 밖 또는 변환 실패. */
interface MarkerProjector {
    fun toScreen(lat: Double, lng: Double): Pair<Int, Int>?
}

sealed interface MarkerSpec {
    val id: String
    val lat: Double
    val lng: Double
    /** equals/hashCode 비교 시 비주얼 식별. onTap 등 비-비주얼 필드는 제외. */
    val visualKey: Any

    data class Single(
        override val id: String,
        override val lat: Double,
        override val lng: Double,
        val category: String,
        val name: String,
        val color: Int,
        val isSelected: Boolean,
        val onTap: (() -> Unit)? = null,
    ) : MarkerSpec {
        override val visualKey: Any = SingleVisual(id, lat, lng, category, name, color, isSelected)
        data class SingleVisual(
            val id: String, val lat: Double, val lng: Double,
            val category: String, val name: String, val color: Int, val isSelected: Boolean,
        )
    }

    data class Cluster(
        override val id: String,
        override val lat: Double,
        override val lng: Double,
        /** 가장 많은 순으로 정렬된 상위 3 카테고리 (혹은 더 적게). */
        val topCategories: List<String>,
        val count: Int,
        /** 탭 시 bbox 계산용 — (lat, lng) 쌍. */
        val memberCoords: List<Pair<Double, Double>>,
        val onTap: (() -> Unit)? = null,
    ) : MarkerSpec {
        override val visualKey: Any = ClusterVisual(id, lat, lng, topCategories, count)
        data class ClusterVisual(
            val id: String, val lat: Double, val lng: Double,
            val topCategories: List<String>, val count: Int,
        )
    }
}

internal data class ClusterAccumulator(
    var sx: Double,
    var sy: Double,
    var sumLat: Double,
    var sumLng: Double,
    val members: MutableList<StoreViewportItem>,
) {
    fun centroidLat(): Double = sumLat / members.size
    fun centroidLng(): Double = sumLng / members.size
}
