package com.example.siheunggagae.map

import com.example.siheunggagae.data.model.StoreViewportItem

private const val CLUSTER_RADIUS_PX = 80
private const val CLUSTER_RADIUS_SQ = CLUSTER_RADIUS_PX * CLUSTER_RADIUS_PX

/**
 * Viewport 매장 → MarkerSpec 리스트 (개별 + 클러스터).
 *
 * - zoom < 11: empty
 * - zoom >= 16: 항상 개별
 * - 그 외: 픽셀 거리 80px 그리디 클러스터
 * - 화면 밖(projector null) 매장 제외
 */
fun computeMarkerSpecs(
    stores: List<StoreViewportItem>,
    projector: MarkerProjector,
    zoom: Int,
    selectedId: Int?,
): List<MarkerSpec> {
    if (zoom < 11) return emptyList()

    if (zoom >= 16) {
        return stores.mapNotNull { s ->
            projector.toScreen(s.latitude, s.longitude) ?: return@mapNotNull null
            MarkerSpec.Single(
                id = "store_${s.storeId}",
                lat = s.latitude,
                lng = s.longitude,
                category = s.category,
                name = s.name,
                color = colorFor(s.category),
                isSelected = (s.storeId == selectedId),
            )
        }
    }

    val accumulators = mutableListOf<ClusterAccumulator>()
    for (s in stores) {
        val (sx, sy) = projector.toScreen(s.latitude, s.longitude) ?: continue
        val host = accumulators.firstOrNull { acc ->
            val dx = acc.sx - sx
            val dy = acc.sy - sy
            (dx * dx + dy * dy) < CLUSTER_RADIUS_SQ
        }
        if (host != null) {
            host.members += s
            host.sumLat += s.latitude
            host.sumLng += s.longitude
            // 평균 픽셀 위치 갱신 (centroid drift)
            val n = host.members.size
            host.sx = ((host.sx * (n - 1)) + sx) / n
            host.sy = ((host.sy * (n - 1)) + sy) / n
        } else {
            accumulators += ClusterAccumulator(
                sx = sx.toDouble(),
                sy = sy.toDouble(),
                sumLat = s.latitude,
                sumLng = s.longitude,
                members = mutableListOf(s),
            )
        }
    }

    return accumulators.map { acc ->
        if (acc.members.size == 1) {
            val s = acc.members.first()
            MarkerSpec.Single(
                id = "store_${s.storeId}",
                lat = s.latitude,
                lng = s.longitude,
                category = s.category,
                name = s.name,
                color = colorFor(s.category),
                isSelected = (s.storeId == selectedId),
            )
        } else {
            val tops = acc.members
                .groupingBy { it.category }
                .eachCount()
                .entries.sortedByDescending { it.value }
                .take(3)
                .map { it.key }
            val sortedIds = acc.members.map { it.storeId }.sorted()
            val id = "cluster_" + sortedIds.joinToString("-")
            MarkerSpec.Cluster(
                id = id,
                lat = acc.centroidLat(),
                lng = acc.centroidLng(),
                topCategories = tops,
                count = acc.members.size,
                memberCoords = acc.members.map { it.latitude to it.longitude },
            )
        }
    }
}

private fun colorFor(category: String): Int = when (category.uppercase()) {
    "CAFE"       -> 0xFF8A6E58.toInt()
    "PARK"       -> 0xFF4CAF50.toInt()
    "HOSPITAL"   -> 0xFFF04268.toInt()
    "GROOMING"   -> 0xFF9C27B0.toInt()
    "RESTAURANT" -> 0xFFF7A35B.toInt()
    "PET_HOTEL"  -> 0xFF614B3A.toInt()
    else         -> 0xFF614B3A.toInt()
}
