package com.example.siheunggagae.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen // 👈 채팅 라우팅 경로 추적을 위한 스크린 임포트 추가
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.MatchDetailUiState
import com.example.siheunggagae.ui.viewmodel.MatchDetailViewModel

// 스펙 컬러
private val Brown700D = Color(0xFF8A6E58)
private val Brown400D = Color(0xFFC4A882)
private val Orange500D = Color(0xFFF7A35B)
private val Pink500D = Color(0xFFF04268)
private val MintLightD = Color(0xFFD0FEE1)
private val OrangeSandD = Color(0xFFFFEDD4)
private val PinkSurfaceD = Color(0xFFFEE7EC)
private val BackgroundD = Color(0xFFFEFEFE)
private val TextBlackD = Color(0xFF1E120A)
private val Green600D = Color(0xFF00A63E)
private val Gray300D = Color(0xFFE8E8E8)

@Composable
fun MatchingDetailScreen(
    requestId: Int,
    viewModel: MatchDetailViewModel,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(requestId) { viewModel.fetchDetail(requestId) }
    LaunchedEffect(uiState) {
        if (uiState is MatchDetailUiState.DeleteSuccess) {
            Toast.makeText(context, "성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onBack()
        }
    }

    Scaffold(
        containerColor = BackgroundD,
        topBar = {
            MatchingDetailTopBar(
                onBack = onBack,
                onDelete = { viewModel.deleteMatch(requestId) },
                onEdit = { onNavigate("request_flow?requestId=$requestId") }
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is MatchDetailUiState.Loading -> CircularProgressIndicator(color = Orange500D, modifier = Modifier.align(Alignment.Center))
                is MatchDetailUiState.Error -> Text(text = state.message, color = Pink500D, fontFamily = PretendardFamily, modifier = Modifier.align(Alignment.Center))
                is MatchDetailUiState.Success -> {
                    val request = state.detail
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatusBannerD(statusText = request.status ?: "상태 없음")
                            RequestInfoCardD(request = request)

                            // ─── 2단계: 지원자 현황 리스트 추가 ───
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "지원자 현황 (${viewModel.applicantList.size}명)",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlackD
                            )

                            if (viewModel.applicantList.isEmpty()) {
                                // 대기 중인 지원자가 아예 없을 때의 빈 레이아웃 예외 처리
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp))
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "아직 신청한 지원자가 없습니다.",
                                        fontFamily = PretendardFamily,
                                        fontSize = 14.sp,
                                        color = Brown700D
                                    )
                                }
                            } else {
                                // 지원자 리스트 루프 돌며 카드 컴포넌트 렌더링
                                viewModel.applicantList.forEach { appItem ->
                                    val applicantName = appItem.applicant?.nickname ?: "지원자"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(1.dp, RoundedCornerShape(16.dp))
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // 초록 프로필 이니셜 아바타
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(MintLightD)
                                        ) {
                                            Text(
                                                text = applicantName.firstOrNull()?.toString() ?: "",
                                                fontFamily = PretendardFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Green600D
                                            )
                                        }

                                        // 지원자 이름 및 한줄 요약 메시지 구역
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = applicantName,
                                                fontFamily = PretendardFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextBlackD
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = appItem.message ?: "신청 메시지가 없습니다.",
                                                fontFamily = PretendardFamily,
                                                fontSize = 12.sp,
                                                color = Brown700D,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // 1:1 웹소켓 채팅방 연동 버튼 (매칭 글 ID와 해당 봉사자의 신청 고유 ID 바인딩)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(Pink500D)
                                                .clickable {
                                                    val appId = appItem.applicationId ?: 0
                                                    onNavigate(Screen.Chat.createRoute(requestId, appId))
                                                }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "채팅하기",
                                                fontFamily = PretendardFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp)) // 하단 마감 패딩
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

// ── TopBar 수정본 ──
@Composable
private fun MatchingDetailTopBar(
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    isMyRequest: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "뒤로", tint = TextBlackD)
        }

        Text(
            text = "내 봉사 상세",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextBlackD,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            TopBarIconD(imageVector = Icons.Default.Edit, desc = "수정", onClick = onEdit, tint = Brown700D)
            TopBarIconD(imageVector = Icons.Default.Delete, desc = "삭제", onClick = onDelete, tint = Pink500D)

            if (!isMyRequest) {
                TopBarIconD(imageVector = Icons.Default.MoreVert, desc = "더보기")
                TopBarIconD(iconRes = R.drawable.ic_share, desc = "공유")
            }
        }
    }
}

@Composable
private fun TopBarIconD(iconRes: Int? = null, imageVector: ImageVector? = null, desc: String, onClick: () -> Unit = {}, tint: Color = TextBlackD) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp).shadow(2.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { onClick() },
    ) {
        if (imageVector != null) Icon(imageVector, desc, tint = tint, modifier = Modifier.size(20.dp))
        else if (iconRes != null) Icon(painterResource(iconRes), desc, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ── 컴포넌트 ──
@Composable
private fun StatusBannerD(statusText: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp)).background(PinkSurfaceD).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = statusText, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextBlackD)
    }
}

@Composable
private fun RequestInfoCardD(request: MatchDetailResponse) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = request.title ?: "제목 없음", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextBlackD)

        val dateValue = request.desiredDate ?: "일정 미정"
        val timeValue = request.desiredTime?.take(5) ?: "시간 미정"

        InfoRowD(PinkSurfaceD, R.drawable.ic_calendar_today, Pink500D, "일정", dateValue, FontWeight.Bold)
        HorizontalDivider(color = Gray300D)
        InfoRowD(OrangeSandD, R.drawable.ic_schedule, Orange500D, "시간", timeValue)
        HorizontalDivider(color = Gray300D)
        InfoRowD(MintLightD, R.drawable.ic_location_on, Green600D, "목적지", request.address ?: "미정")
        HorizontalDivider(color = Gray300D)
        InfoRowD(Gray300D, R.drawable.ic_chat_bubble, TextBlackD, "요청 메모", request.content ?: "메모 없음")
    }
}

@Composable
private fun InfoRowD(iconBg: Color, iconRes: Int, iconTint: Color, label: String, value: String, valueFontWeight: FontWeight = FontWeight.Medium) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconBg)) {
            Icon(painterResource(iconRes), null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700D)
            Spacer(Modifier.height(2.dp))
            Text(value, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = valueFontWeight, color = TextBlackD)
        }
    }
}