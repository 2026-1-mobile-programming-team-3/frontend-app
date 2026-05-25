package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.MatchStatus
import com.example.siheunggagae.ui.theme.PretendardFamily
import androidx.compose.ui.res.painterResource
import com.example.siheunggagae.R
import com.example.siheunggagae.ui.util.bgColor
import com.example.siheunggagae.ui.util.textColor
import com.example.siheunggagae.ui.viewmodel.MyRequestsUiState
import com.example.siheunggagae.ui.viewmodel.MyRequestsViewModel

private val Background95 = Color(0xFFFEFEFE)
private val TextBlack = Color(0xFF1E120A)
private val Brown700M = Color(0xFF8A6E58)
private val Orange500M = Color(0xFFF7A35B)
private val Pink500M = Color(0xFFF04268)
private val Gray300M = Color(0xFFE8E8E8)
private val InputBgM = Color(0xFFF8F8F8)

@Composable
fun MyRequestsScreen(
    viewModel: MyRequestsViewModel,
    onBack: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onCardClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("전체", "매칭전", "매칭됨", "종료됨")

    LaunchedEffect(viewModel.selectedTab) {
        viewModel.fetchMyRequests()
    }

    Scaffold(
        containerColor = Background95,
        topBar = {
            MyRequestsTopBar(
                onBack = onBack,
                onNavigateToCreate = onNavigateToCreate
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            TabRow(
                selectedTabIndex = viewModel.selectedTab,
                containerColor = Color.White,
                contentColor = Pink500M,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[viewModel.selectedTab]),
                        color = Pink500M
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = viewModel.selectedTab == index,
                        onClick = { viewModel.selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = if (viewModel.selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (viewModel.selectedTab == index) Pink500M else Brown700M
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is MyRequestsUiState.Loading -> {
                        CircularProgressIndicator(
                            color = Orange500M,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is MyRequestsUiState.Error -> {
                        Text(
                            text = state.message,
                            color = Pink500M,
                            fontFamily = PretendardFamily,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is MyRequestsUiState.Success -> {
                        if (state.matches.isEmpty()) {
                            Text(
                                text = "작성한 봉사 요청이 없습니다.",
                                color = Brown700M,
                                fontFamily = PretendardFamily,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.matches) { request ->
                                    MyRequestCard(
                                        request = request,
                                        onCardClick = { request.matchId?.let { onCardClick(it) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyRequestsTopBar(onBack: () -> Unit, onNavigateToCreate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlack,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = "내 봉사 요청 목록",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextBlack,
            modifier = Modifier.align(Alignment.Center)
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .shadow(1.dp, RoundedCornerShape(50.dp))
                .clip(RoundedCornerShape(50.dp))
                .background(Pink500M)
                .clickable { onNavigateToCreate() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text("새 요청", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun MyRequestCard(request: MatchListItem, onCardClick: () -> Unit) {
    val matchStatus = MatchStatus.entries.find { it.name == request.status } ?: MatchStatus.WAITING

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(matchStatus.bgColor).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(text = matchStatus.label, fontFamily = PretendardFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = matchStatus.textColor)
                }

                val hasUnreadChat = false
                if (hasUnreadChat) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Pink500M).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("새 메시지", fontFamily = PretendardFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (request.createdAt != null) {
                    Text(text = request.createdAt.take(10), fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700M)
                }
            }
            Spacer(Modifier.height(10.dp))

            Text(
                text = request.title ?: "제목 없음",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))

            // 👈 [버그 수정 구역] MatchListItem 스펙에 맞춰 request.pet 필드 제거 후 주소 정보 결합 가공 처리
            val datePart = request.desiredDate?.take(10) ?: "날짜 미정"
            val timePart = request.desiredTime?.take(5) ?: "시간 미정"
            val townPart = request.address?.split(" ")?.getOrNull(2) ?: request.address ?: "동네 미정"

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_map_pin),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Brown700M,
                )
                Text(
                    text = "$townPart · ",
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    color = Brown700M,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_today),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Brown700M,
                )
                Text(
                    text = " $datePart $timePart",
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    color = Brown700M,
                )
            }
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(InputBgM).padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text(text = "목적지", fontFamily = PretendardFamily, fontSize = 11.sp, color = Brown700M, fontWeight = FontWeight.Medium)
                }
                Text(
                    text = request.address ?: "지정 주소 없음",
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    color = TextBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (matchStatus == MatchStatus.DONE) {
                HorizontalDivider(color = Gray300M, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB200), modifier = Modifier.size(16.dp))
                        // [동적 연동] 하드코딩 "5.0" 대신 서버에서 내려온 후기 별점 데이터(receivedRating) 연동
                        val displayRating = request.receivedRating?.toDouble() ?: 5.0
                        Text(text = displayRating.toString(), fontFamily = PretendardFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                    }
                    Text(
                        text = "후기 보기 >",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Pink500M
                    )
                }
            }
        }
    }
}