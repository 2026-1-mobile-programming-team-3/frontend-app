package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.model.StoreRequestType
import com.example.siheunggagae.ui.viewmodel.MyStoreRequestsUiState
import com.example.siheunggagae.ui.viewmodel.MyStoreRequestsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

private val BackgroundQ = Color(0xFFFEFEFE)
private val TextBlackQ = Color(0xFF1E120A)
private val Brown700Q = Color(0xFF8A6E58)
private val Brown900Q = Color(0xFF614B3A)
private val BorderBeigeQ = Color(0xFFE8D3C2)
private val OrangeSandQ = Color(0xFFFFEDD4)
private val Orange500Q = Color(0xFFF7A35B)
private val FABBrownQ = Color(0xFF9A7B5E)
private val TagPendingBgQ = Color(0xFFFEF3C7)
private val TagPendingTextQ = Color(0xFFCA8A04)
private val TagApprovedBgQ = Color(0xFFF0FDF4)
private val TagApprovedTextQ = Color(0xFF16A34A)
private val TagRejectedBgQ = Color(0xFFFEE7EC)
private val TagRejectedTextQ = Color(0xFFE84B6A)
private val PlaceholderQ = Color(0xFFC1AEA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoreRequestsScreen(
    viewModel: MyStoreRequestsViewModel? = null,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val state by (viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(MyStoreRequestsUiState.Loading) })
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { last ->
                val s = state as? MyStoreRequestsUiState.Success ?: return@collect
                if (last != null && last >= s.items.size - 3 && s.hasMore) {
                    viewModel?.loadMore()
                }
            }
    }

    Scaffold(
        topBar = { TopBarStoreRequests(title = "내 매장 요청", onBack = onBack) },
        containerColor = BackgroundQ,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigate(Screen.StoreRequestForm.createRoute(type = StoreRequestType.ADD))
                },
                containerColor = FABBrownQ,
                contentColor = Color.White,
            ) {
                Icon(painter = painterResource(R.drawable.ic_plus), contentDescription = "매장 추가 요청")
            }
        },
    ) { padding ->
        when (val s = state) {
            MyStoreRequestsUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Orange500Q) }

            is MyStoreRequestsUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = Brown700Q, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel?.refresh() }) { Text("다시 시도") }
                }
            }

            is MyStoreRequestsUiState.Success -> Column(
                Modifier.fillMaxSize().padding(padding),
            ) {
                Text(
                    "전체 ${s.total}건",
                    color = Brown700Q,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 18.dp, top = 4.dp, bottom = 12.dp),
                )
                FilterChipsRow(selected = s.filter) { viewModel?.setFilter(it) }
                if (s.items.isEmpty()) {
                    EmptyState(onAddClick = {
                        onNavigate(Screen.StoreRequestForm.createRoute(type = StoreRequestType.ADD))
                    })
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                    ) {
                        items(s.items, key = { it.requestId }) { item ->
                            StoreRequestCard(item) {
                                onNavigate(Screen.StoreRequestDetail.createRoute(item.requestId))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selected: StoreRequestStatus?,
    onSelect: (StoreRequestStatus?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp),
    ) {
        item { ChipFilter("전체", selected == null) { onSelect(null) } }
        item { ChipFilter("대기", selected == StoreRequestStatus.PENDING) { onSelect(StoreRequestStatus.PENDING) } }
        item { ChipFilter("승인", selected == StoreRequestStatus.APPROVED) { onSelect(StoreRequestStatus.APPROVED) } }
        item { ChipFilter("반려", selected == StoreRequestStatus.REJECTED) { onSelect(StoreRequestStatus.REJECTED) } }
    }
}

@Composable
private fun ChipFilter(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) Color(0xFF1A1A1A) else Color.White
    val fg = if (isSelected) Color.White else Brown700Q
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(if (!isSelected) Modifier.border(1.dp, BorderBeigeQ, RoundedCornerShape(50.dp)) else Modifier)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) { Text(label, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun StoreRequestCard(item: StoreRequestItem, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .clickable { onClick() },
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(categoryIcon(item.payload.category)),
                contentDescription = null,
                tint = Brown900Q,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypePill(item.type)
                    Spacer(Modifier.width(6.dp))
                    StatusTag(item.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    item.payload.name ?: "매장명 미입력",
                    color = TextBlackQ,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(formatSubLine(item), color = Brown700Q, fontSize = 12.sp)
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = PlaceholderQ,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TypePill(type: StoreRequestType) {
    val label = when (type) {
        StoreRequestType.ADD -> "추가"
        StoreRequestType.UPDATE -> "수정"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(OrangeSandQ)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) { Text(label, color = Brown900Q, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun StatusTag(status: StoreRequestStatus) {
    val (bg, fg, label) = when (status) {
        StoreRequestStatus.PENDING -> Triple(TagPendingBgQ, TagPendingTextQ, "검토중")
        StoreRequestStatus.APPROVED -> Triple(TagApprovedBgQ, TagApprovedTextQ, "승인")
        StoreRequestStatus.REJECTED -> Triple(TagRejectedBgQ, TagRejectedTextQ, "반려")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) { Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
}

private fun formatSubLine(item: StoreRequestItem): String {
    val date = (item.processedAt ?: item.createdAt).take(10)
    val cat = item.payload.category ?: "-"
    val word = if (item.status == StoreRequestStatus.PENDING) "신청" else "처리"
    return "$cat · $date $word"
}

private fun categoryIcon(category: String?): Int = when (category) {
    "CAFE" -> R.drawable.ic_coffee
    "RESTAURANT" -> R.drawable.ic_utensils
    "PARK" -> R.drawable.ic_trees
    "PET_HOTEL" -> R.drawable.ic_hotel
    else -> R.drawable.ic_store
}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(OrangeSandQ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_store),
                    contentDescription = null,
                    tint = Orange500Q,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("아직 요청이 없어요", color = TextBlackQ, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("매장을 지도에 추가해보세요", color = Brown700Q, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Brown900Q),
                shape = RoundedCornerShape(12.dp),
            ) { Text("+ 매장 추가 요청", color = Color.White) }
        }
    }
}

@Composable
private fun TopBarStoreRequests(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(40.dp).clickable { onBack() },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    tint = TextBlackQ,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(title, color = TextBlackQ, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun MyStoreRequestsScreenPreview() {
    MyStoreRequestsScreen()
}
