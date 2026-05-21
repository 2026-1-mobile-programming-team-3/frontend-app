package com.example.siheunggagae.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
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
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

// ── TopBar ──
@Composable
private fun MatchingDetailTopBar(onBack: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "뒤로", tint = TextBlackD)
        }
        Text("내 봉사 상세", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextBlackD, modifier = Modifier.align(Alignment.Center))

        Row(modifier = Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 수정/삭제 (질문자님 기능)
            TopBarIconD(imageVector = Icons.Default.Edit, desc = "수정", onClick = onEdit, tint = Brown700D)
            TopBarIconD(imageVector = Icons.Default.Delete, desc = "삭제", onClick = onDelete, tint = Pink500D)
            // 더보기/공유 (main 브랜치 기능)
            TopBarIconD(imageVector = Icons.Default.MoreVert, desc = "더보기")
            TopBarIconD(iconRes = R.drawable.ic_share, desc = "공유")
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

        val dateValue = request.desiredDate?.take(10) ?: "일정 미정"
        val timeValue = if (request.desiredDate != null && request.desiredDate.length > 10) request.desiredDate.substring(11, 16) else "시간 미정"

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