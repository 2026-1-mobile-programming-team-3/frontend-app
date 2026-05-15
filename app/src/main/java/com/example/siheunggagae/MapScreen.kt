package com.example.siheunggagae

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러
private val Brown900Mp   = Color(0xFF614B3A)
private val Brown700Mp   = Color(0xFF8A6E58)
private val Brown400Mp   = Color(0xFFC4A882)
private val BrownBorderP = Color(0xFFE8D3C2)
private val Orange500Mp  = Color(0xFFF7A35B)
private val Pink500Mp    = Color(0xFFF04268)
private val TextBlack    = Color(0xFF1E120A)
private val StarYellow   = Color(0xFFFDC700)
private val MintLight    = Color(0xFFD0FEE1)

// ─── 데이터 ────────────────────────────────────────────────────────────────────

private data class MapPlace(
    val id: Int,
    val name: String,
    val category: String,
    val distance: String,
    val rating: Float,
    val isFavorite: Boolean = false,
)

private val mapCategories = listOf("전체", "카페", "공원", "병원")

private val sampleMapPlaces = listOf(
    MapPlace(1, "댕댕 카페",    "카페", "0.3km", 4.8f, isFavorite = true),
    MapPlace(2, "행복 동물병원", "병원", "0.7km", 4.5f),
    MapPlace(3, "정왕 공원",    "공원", "1.0km", 4.6f),
    MapPlace(4, "강아지 미용실", "미용", "1.2km", 4.3f),
    MapPlace(5, "시흥 펫 카페", "카페", "1.5km", 4.7f),
)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onNavigate: (String) -> Unit = {}) {
    var selectedCategory by remember { mutableStateOf("전체") }
    var showFilterSheet  by remember { mutableStateOf(false) }

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
            sheetPeekHeight = 280.dp,
            sheetContainerColor = Color.White,
            sheetTonalElevation = 0.dp,
            sheetShadowElevation = 8.dp,
            sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            sheetDragHandle = { MapDragHandle() },
            sheetContent = { MapBottomSheetContent() },
            containerColor = Color.Transparent,
        ) { _ ->
            MapContent(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onLayerClick = { showFilterSheet = true },
            )
        }
    }

    if (showFilterSheet) {
        MapFilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApply   = { showFilterSheet = false },
        )
    }
}

// ─── 지도 영역 ──────────────────────────────────────────────────────────────────

@Composable
private fun MapContent(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onLayerClick: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(MintLight, Color(0xFFE8FAF0), Color(0xFFF4FDFB), Color.White)
                    )
                )
        )

        Icon(Icons.Default.LocationOn, null, tint = Pink500Mp,
            modifier = Modifier.offset(x = 90.dp, y = 95.dp).size(30.dp))
        Icon(Icons.Default.LocationOn, null, tint = Pink500Mp,
            modifier = Modifier.offset(x = 215.dp, y = 65.dp).size(26.dp))
        Icon(Icons.Default.LocationOn, null, tint = Orange500Mp,
            modifier = Modifier.offset(x = 155.dp, y = 200.dp).size(28.dp))

        Box(
            modifier = Modifier
                .offset(x = 185.dp, y = 170.dp)
                .size(16.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF2563EB))
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        ) {
            MapSearchCard()
            Spacer(Modifier.height(8.dp))
            MapCategoryChipRow(selected = selectedCategory, onSelect = onCategorySelected)
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapIconFab(icon = Icons.Default.MyLocation, contentDescription = "내 위치")
            MapIconFab(icon = Icons.Default.Layers,     contentDescription = "레이어",  onClick = onLayerClick)
            MapIconFab(icon = Icons.Default.Refresh,    contentDescription = "새로고침")
        }
    }
}

@Composable
private fun MapSearchCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(4.dp, RoundedCornerShape(50.dp))
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .clickable { }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Brown400Mp,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "매장 · 병원 · 공원 검색",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown400Mp,
        )
    }
}

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
                    .shadow(2.dp, RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) Color(0xFF1A1A1A) else Color.White)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = if (isSelected) Color.White else Brown700Mp
                )
            }
        }
    }
}

@Composable
private fun MapIconFab(icon: ImageVector, contentDescription: String, onClick: () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Brown700Mp,
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
                .width(48.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFE8E8E8))
        )
    }
}

// ─── 하단 시트 콘텐츠 ──────────────────────────────────────────────────────────

@Composable
private fun MapBottomSheetContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "주변 매장 24곳",
                fontFamily = PretendardFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlack
            )
            Text(
                text = "↕ 거리순",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp,
                color = Brown700Mp,
                modifier = Modifier.clickable { }
            )
        }
        HorizontalDivider(color = Color(0xFFF3F4F6))

        LazyColumn {
            items(sampleMapPlaces, key = { it.id }) { place ->
                MapPlaceItem(place = place)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFFF3F4F6)
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MapPlaceItem(place: MapPlace) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (place.id == 1) Pink500Mp else Brown900Mp)
        ) {
            Text(
                text = "${place.id}",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = Color.White
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlack
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${place.category} · ${place.distance}",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = Brown700Mp
                )
                Text(
                    text = "·",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Brown700Mp
                )
                Icon(Icons.Default.Star, null, tint = StarYellow, modifier = Modifier.size(12.dp))
                Text(
                    text = "${place.rating}",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = Brown700Mp
                )
            }
        }
        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (place.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "즐겨찾기",
                tint = if (place.isFavorite) Pink500Mp else BrownBorderP,
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
