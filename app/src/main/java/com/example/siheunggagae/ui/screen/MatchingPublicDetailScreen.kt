package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.local.CurrentUserStore
import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.data.model.requiresVolunteerRole
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.viewmodel.MatchDetailUiState
import com.example.siheunggagae.ui.viewmodel.MatchDetailViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.MapView as KakaoNativeMapView
import com.example.siheunggagae.MapViewWrapper
import androidx.compose.runtime.DisposableEffect
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import com.example.siheunggagae.ui.util.matchStatusToKorean
import kotlinx.coroutines.launch

private val Brown700P     = Color(0xFF8A6E58)
private val BrownBorderP  = Color(0xFFE8D3C2)
private val Orange500P    = Color(0xFFF7A35B)
private val Pink500P      = Color(0xFFF04268)
private val MintLightP    = Color(0xFFD0FEE1)
private val OrangeSandP   = Color(0xFFFFEDD4)
private val PinkSurfaceP  = Color(0xFFFEE7EC)
private val BackgroundP   = Color(0xFFFEFEFE)
private val Gray300P      = Color(0xFFE8E8E8)
private val TextBlackP    = Color(0xFF1E120A)
private val Green600P     = Color(0xFF00A63E)
private val Brown900C     = Color(0xFF614C3B)
private val StarYellowP   = Color(0xFFFDC700)

@Composable
fun MatchingPublicDetailScreen(
    requestId: Int = 0,
    viewModel: MatchDetailViewModel? = null,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(MatchDetailUiState.Loading)
    }.collectAsStateWithLifecycle()

    var showApplyDialog by remember { mutableStateOf(false) }
    var applyMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isVolunteer = remember { CurrentUserStore(context).isVolunteer() }

    LaunchedEffect(requestId) {
        viewModel?.fetchDetail(requestId)
    }

    // 채팅/후기 진입 가드 — BottomBar와 RequesterCard에서 동일 로직을 공유.
    // ACCEPTED 일 때만 실제 채팅 진입. PENDING은 수락 대기 안내, 미신청은 신청 유도, DONE은 후기로.
    val handleChatClick: () -> Unit = {
        val state = uiState as? MatchDetailUiState.Success
        val currentStatus = state?.detail?.status?.trim()?.uppercase() ?: ""
        when {
            viewModel?.isAccepted == true -> {
                val applicationId = viewModel.myApplicationId ?: 0
                onNavigate(Screen.Chat.createRoute(requestId, applicationId))
            }
            currentStatus == "DONE" -> {
                onNavigate(Screen.MatchReview.createRoute(requestId, "DONE", isViewOnly = true))
            }
            viewModel?.isApplied == true -> {
                scope.launch {
                    snackbarHostState.showSnackbar("요청자가 수락한 뒤에 채팅할 수 있어요.")
                }
            }
            else -> {
                scope.launch {
                    snackbarHostState.showSnackbar("봉사 신청 후 채팅이 가능합니다!")
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundP,
        snackbarHost = { SiheungSnackbarHost(hostState = snackbarHostState) },
        topBar = { PublicDetailTopBar(onBack = onBack) },
        bottomBar = {
            if (uiState is MatchDetailUiState.Success) {
                val state = uiState as MatchDetailUiState.Success
                val currentStatus = state.detail.status?.trim()?.uppercase() ?: ""
                val authorId = state.detail.author?.userId
                val myUserId = viewModel?.currentUserId
                val isMyRequest = authorId != null && authorId == myUserId

                PublicDetailBottomBar(
                    currentStatus = currentStatus,
                    isMyRequest = isMyRequest,
                    isApplied = viewModel?.isApplied ?: false,
                    isAccepted = viewModel?.isAccepted ?: false,
                    myApplicationStatus = viewModel?.myApplicationStatus ?: "",
                    category = state.detail.category,
                    isVolunteer = isVolunteer,
                    onApply = { showApplyDialog = true },
                    onChat = handleChatClick,
                    onManageRequest = {
                        onNavigate(Screen.MatchingDetail.createRoute(requestId))
                    },
                    onVolunteerApply = {
                        onNavigate(Screen.VolunteerApply.route)
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is MatchDetailUiState.Loading -> {
                    CircularProgressIndicator(color = Orange500P, modifier = Modifier.align(Alignment.Center))
                }
                is MatchDetailUiState.Error -> {
                    Text(text = state.message, color = Pink500P, fontFamily = PretendardFamily, modifier = Modifier.align(Alignment.Center))
                }
                is MatchDetailUiState.Success -> {
                    val request = state.detail

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PublicStatusBanner(statusText = matchStatusToKorean(request.status))
                            PublicMapCard(latitude = request.latitude, longitude = request.longitude)
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        ) {
                            PublicRequestInfoCard(request = request)
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = "요청자 정보", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700P)
                            RequesterCard(
                                authorNickname = request.author?.nickname ?: "요청자",
                                onChat = handleChatClick,
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        if (showApplyDialog) {
            AlertDialog(
                onDismissRequest = {
                    showApplyDialog = false
                    applyMessage = ""
                },
                title = { Text("봉사 신청하기", fontFamily = PretendardFamily, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = applyMessage,
                        onValueChange = { applyMessage = it },
                        placeholder = { Text("신청 메시지(선택)", fontFamily = PretendardFamily) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val message = applyMessage
                        viewModel?.applyForMatch(requestId, message) { _, msg ->
                            showApplyDialog = false
                            applyMessage = ""
                            scope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    }) { Text("신청하기", fontFamily = PretendardFamily) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showApplyDialog = false
                        applyMessage = ""
                    }) { Text("취소", fontFamily = PretendardFamily) }
                }
            )
        }
    }
}

@Composable
private fun PublicDetailTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterStart).size(40.dp).shadow(2.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { onBack() },
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = TextBlackP, modifier = Modifier.size(22.dp))
        }
        Text(
            text = "이동 지원 요청",
            fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp, color = TextBlackP,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun PublicStatusBanner(statusText: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp)).background(PinkSurfaceP).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = statusText, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextBlackP)
    }
}

@Composable
private fun PublicRequestInfoCard(request: MatchDetailResponse) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = request.title ?: "제목 없음", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextBlackP)

        val dateValue = request.desiredDate ?: "일정 미정"
        val timeValue = request.desiredTime?.take(5) ?: "시간 미정"

        PublicInfoRow(iconBg = PinkSurfaceP, iconRes = R.drawable.ic_calendar_today, iconTint = Pink500P, label = "일정", value = dateValue, valueFontWeight = FontWeight.Bold)
        HorizontalDivider(color = Gray300P)
        PublicInfoRow(iconBg = OrangeSandP, iconRes = R.drawable.ic_schedule, iconTint = Orange500P, label = "시간", value = timeValue)
        HorizontalDivider(color = Gray300P)
        PublicInfoRow(iconBg = MintLightP, iconRes = R.drawable.ic_location_on, iconTint = Green600P, label = "목적지", value = request.address ?: "미정")
        HorizontalDivider(color = Gray300P)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PublicIconBox(bg = BrownBorderP) {
                Icon(painter = painterResource(R.drawable.ic_pets), null, tint = Brown700P, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "반려동물", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700P)
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(OrangeSandP).padding(horizontal = 12.dp, vertical = 3.dp)) {
                    val petName = request.pet?.name ?: "이름 없음"
                    val petSpecies = request.pet?.species ?: "종 미정"
                    Text(text = "$petName · $petSpecies", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Brown700P)
                }
            }
        }
        HorizontalDivider(color = Gray300P)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PublicIconBox(bg = Gray300P) {
                Icon(painter = painterResource(R.drawable.ic_chat_bubble), null, tint = TextBlackP, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "요청 메모", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700P)
                Spacer(Modifier.height(4.dp))
                Text(text = request.content ?: "메모 없음", fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackP)
            }
        }
    }
}

@Composable
private fun PublicInfoRow(iconBg: Color, iconRes: Int, iconTint: Color, label: String, value: String, valueFontWeight: FontWeight = FontWeight.Medium) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PublicIconBox(bg = iconBg) {
            Icon(painter = painterResource(iconRes), null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700P)
            Spacer(Modifier.height(2.dp))
            Text(text = value, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = valueFontWeight, color = TextBlackP)
        }
    }
}

@Composable
private fun PublicIconBox(bg: Color, content: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(bg)) { content() }
}

@Composable
fun PublicMapCard(
    latitude: Double?,
    longitude: Double?,
    placeName: String? = "목적지"
) {
    val lat = latitude ?: 37.3801
    val lng = longitude ?: 126.8029
    val context = LocalContext.current

    val nativeMapView = remember { KakaoNativeMapView(context) }
    val mapWrapper = remember(nativeMapView) { MapViewWrapper(nativeMapView) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF4F4F4)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { nativeMapView },
            modifier = Modifier.fillMaxSize(),
            update = { _ ->
                if (!mapWrapper.hasBeenInitialized) {
                    mapWrapper.init { _ ->
                        mapWrapper.moveCamera(lat, lng, zoomLevel = 16)
                        mapWrapper.clearMarkers()
                        mapWrapper.addMarker(
                            id = "destination_marker",
                            lat = lat,
                            lng = lng,
                            markerColor = 0xFFF04268.toInt(),
                            category = "HOSPITAL",
                            name = placeName ?: "목적지"
                        )
                    }
                } else {
                    mapWrapper.moveCamera(lat, lng, zoomLevel = 16)
                    mapWrapper.clearMarkers()
                    mapWrapper.addMarker(
                        id = "destination_marker",
                        lat = lat,
                        lng = lng,
                        markerColor = 0xFFF04268.toInt(),
                        category = "HOSPITAL",
                        name = placeName ?: "목적지"
                    )
                }
            }
        )

        DisposableEffect(nativeMapView) {
            onDispose {
                runCatching { mapWrapper.pause() }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_map), null, tint = TextBlackP, modifier = Modifier.size(14.dp))
            Text(text = "지도에서 보기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextBlackP)
        }
    }
}

@Composable
private fun RequesterCard(authorNickname: String, onChat: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(CircleShape).background(MintLightP)) {
            Text(text = authorNickname.firstOrNull()?.toString() ?: "요", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackP)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = authorNickname, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackP)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, modifier = Modifier.size(11.dp), tint = StarYellowP)
                Text(text = "4.9 · 시흥개개 신뢰 회원", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700P)
            }
        }
        Row(
            modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Color.White).border(1.dp, BrownBorderP, RoundedCornerShape(50.dp)).clickable { onChat() }.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_chat_bubble), null, tint = Brown700P, modifier = Modifier.size(14.dp))
            Text(text = "채팅 / 후기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Brown700P)
        }
    }
}

@Composable
private fun PublicDetailBottomBar(
    currentStatus: String,
    isMyRequest: Boolean,
    isApplied: Boolean,
    isAccepted: Boolean,
    myApplicationStatus: String,
    category: MatchCategory?,
    isVolunteer: Boolean,
    onApply: () -> Unit,
    onChat: () -> Unit,
    onManageRequest: () -> Unit,
    onVolunteerApply: () -> Unit,
) {
    // 봉사자 자격이 필요한 카테고리인데 본인이 봉사자가 아니면 신청을 막는다.
    // 이미 신청한 상태(isApplied)나 본인 요청(isMyRequest)은 가드 대상 아님.
    val isBlockedByVolunteerRequirement =
        category?.requiresVolunteerRole() == true && !isVolunteer && !isMyRequest && !isApplied

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            isMyRequest -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50.dp)).background(Brown900C).clickable { onManageRequest() },
                ) {
                    Text(text = "지원 현황 및 목록 보기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            isBlockedByVolunteerRequirement && currentStatus != "DONE" -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFFF2F2F2)),
                ) {
                    Text(
                        text = "봉사자 자격이 필요한 요청이에요",
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Green600P)
                        .clickable { onVolunteerApply() }
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = "자격 신청",
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            currentStatus == "DONE" -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50.dp)).background(Brown700P).clickable { onChat() },
                ) {
                    Text(text = "요청자가 작성한 후기 보기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            myApplicationStatus == "REJECTED" -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xFFF2F2F2)),
                ) {
                    Text(text = "매칭이 무산되었습니다", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }

            myApplicationStatus == "CANCELED" -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xFFF2F2F2)),
                ) {
                    Text(text = "신청이 취소되었습니다", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }

            !isApplied -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White).border(1.dp, BrownBorderP, CircleShape).clickable { onChat() },
                ) {
                    Icon(painter = painterResource(R.drawable.ic_chat_bubble), contentDescription = "채팅", tint = Brown700P, modifier = Modifier.size(20.dp))
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(50.dp)).background(Pink500P).clickable { onApply() },
                ) {
                    Text(text = "봉사 신청하기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            // 신청은 했으나 아직 수락 전(PENDING) — 채팅 진입 차단, 안내만 노출
            isApplied && !isAccepted -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xFFF2F2F2)),
                ) {
                    Text(text = "요청자 수락 대기 중", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }

            // ACCEPTED — 채팅 진입 가능
            else -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(50.dp)).background(Pink500P).clickable { onChat() },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_chat_bubble), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(text = "요청자와 채팅하기", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchingPublicDetailScreenPreview() {
    SiheungGagaeTheme { MatchingPublicDetailScreen() }
}