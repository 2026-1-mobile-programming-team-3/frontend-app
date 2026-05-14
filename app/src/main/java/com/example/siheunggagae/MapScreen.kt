package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.Brown40
import com.example.siheunggagae.ui.theme.Gray10
import com.example.siheunggagae.ui.theme.Gray40
import com.example.siheunggagae.ui.theme.Gray80
import com.example.siheunggagae.ui.theme.Gray90
import com.example.siheunggagae.ui.theme.MapDeep
import com.example.siheunggagae.ui.theme.MapMint
import com.example.siheunggagae.ui.theme.MapSky
import com.example.siheunggagae.ui.theme.Pink60
import com.example.siheunggagae.ui.theme.Pink90
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ─── 데이터 ────────────────────────────────────────────────────────────────────

private data class MapPlace(
    val id: Int,
    val name: String,
    val category: String,
    val distance: String,
    val rating: Float,
)

private val mapCategories = listOf("전체", "카페", "공원", "병원", "미용")

private val sampleMapPlaces = listOf(
    MapPlace(1, "댕댕 카페", "카페", "0.3km", 4.8f),
    MapPlace(2, "행복 동물병원", "병원", "0.7km", 4.5f),
    MapPlace(3, "정왕 공원", "공원", "1.0km", 4.6f),
    MapPlace(4, "강아지 미용실", "미용", "1.2km", 4.3f),
    MapPlace(5, "시흥 펫 카페", "카페", "1.5km", 4.7f),
)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onNavigate: (String) -> Unit = {}) {
    var selectedCategory by remember { mutableStateOf("전체") }

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { AppBottomBar(currentRoute = Screen.Map.route, onNavigate = onNavigate) },
    ) { navPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(navPadding),
            scaffoldState = sheetState,
            sheetPeekHeight = 260.dp,
            sheetContainerColor = Color.White,
            sheetTonalElevation = 0.dp,
            sheetShadowElevation = 8.dp,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetDragHandle = { MapDragHandle() },
            sheetContent = { MapBottomSheetContent() },
            containerColor = Color.Transparent,
        ) { _ ->
            MapContent(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
            )
        }
    }
}

// ─── 지도 영역 (placeholder + 오버레이) ─────────────────────────────────────────

@Composable
private fun MapContent(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 지도 placeholder (민트/하늘 그라디언트)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(listOf(MapSky, MapMint, MapDeep, MapSky))
                )
        )

        // 핑크 위치 핀 4개
        PinkLocationPin(x = 90.dp,  y = 95.dp,  size = 30.dp)
        PinkLocationPin(x = 215.dp, y = 65.dp,  size = 26.dp)
        PinkLocationPin(x = 300.dp, y = 185.dp, size = 28.dp)
        PinkLocationPin(x = 155.dp, y = 240.dp, size = 24.dp)

        // 파란 원형 dot (내 위치)
        Box(
            modifier = Modifier
                .offset(x = 185.dp, y = 170.dp)
                .size(18.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB))
            )
        }

        // 상단 오버레이: 검색창 + 카테고리 칩
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        ) {
            MapSearchCard()
            Spacer(modifier = Modifier.height(10.dp))
            MapCategoryChipRow(selected = selectedCategory, onSelect = onCategorySelected)
        }

        // 우측 세로 FAB 버튼 3개
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapIconFab(icon = Icons.Default.MyLocation, contentDescription = "내 위치")
            MapIconFab(icon = Icons.Default.Layers, contentDescription = "레이어")
            MapIconFab(icon = Icons.Default.Refresh, contentDescription = "새로고침")
        }
    }
}

// 핑크 핀
@Composable
private fun PinkLocationPin(x: Dp, y: Dp, size: Dp) {
    Icon(
        imageVector = Icons.Default.LocationOn,
        contentDescription = null,
        tint = Pink60,
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
    )
}

// 검색창 카드
@Composable
private fun MapSearchCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Gray40,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "매장 · 병원 · 공원 검색",
            fontSize = 14.sp,
            color = Gray40,
        )
    }
}

// 카테고리 칩 Row
@Composable
private fun MapCategoryChipRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        mapCategories.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Gray10 else Color.White)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Gray10
                )
            }
        }
    }
}

// 우측 FAB 버튼
@Composable
private fun MapIconFab(icon: ImageVector, contentDescription: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Gray40,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─── 드래그 핸들 ───────────────────────────────────────────────────────────────

@Composable
private fun MapDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Gray80)
        )
    }
}

// ─── 하단 시트 콘텐츠 ──────────────────────────────────────────────────────────

@Composable
private fun MapBottomSheetContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "주변 매장 24곳",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Gray10
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Gray80, RoundedCornerShape(6.dp))
                    .clickable { }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "↕ 거리순", fontSize = 13.sp, color = Gray40)
            }
        }
        HorizontalDivider(color = Gray90)

        // 장소 리스트
        LazyColumn {
            items(sampleMapPlaces, key = { it.id }) { place ->
                MapPlaceItem(place = place)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Gray90
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MapPlaceItem(place: MapPlace) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 번호 circle (핑크)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Pink90)
        ) {
            Text(
                text = "${place.id}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE91E63)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray10
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "${place.category} · ${place.distance}", fontSize = 12.sp, color = Gray40)
                Text(text = "·", fontSize = 12.sp, color = Gray40)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(12.dp)
                )
                Text(text = "${place.rating}", fontSize = 12.sp, color = Gray40)
            }
        }

        // 즐겨찾기
        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "즐겨찾기",
                tint = Gray80,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapScreenPreview() {
    SiheungGagaeTheme { MapScreen() }
}
