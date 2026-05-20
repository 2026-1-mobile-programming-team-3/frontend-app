package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
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
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.MyRequestsUiState
import com.example.siheunggagae.ui.viewmodel.MyRequestsViewModel

private val Background95 = Color(0xFFFEFEFE)
private val TextBlack = Color(0xFF1E120A)
private val Brown700M = Color(0xFF8A6E58)
private val Orange500M = Color(0xFFF7A35B)
private val Pink500M = Color(0xFFF04268)

@Composable
fun MyRequestsScreen(
    viewModel: MyRequestsViewModel,
    onBack: () -> Unit = {},
    onCardClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // 화면 진입 시 데이터를 갱신합니다.
    LaunchedEffect(Unit) {
        viewModel.fetchMyRequests()
    }

    Scaffold(
        containerColor = Background95,
        topBar = { MyRequestsTopBar(onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
private fun MyRequestsTopBar(onBack: () -> Unit) {
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
    }
}

@Composable
private fun MyRequestCard(request: MatchListItem, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = request.title ?: "제목 없음",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = request.address ?: "주소 없음",
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                color = Brown700M
            )
        }
    }
}