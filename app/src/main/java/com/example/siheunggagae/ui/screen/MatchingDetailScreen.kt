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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.siheunggagae.ui.component.AppAsyncImage
import com.example.siheunggagae.ui.component.SiheungAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.MatchDetailUiState
import com.example.siheunggagae.ui.viewmodel.MatchDetailViewModel
import com.example.siheunggagae.ui.util.matchStatusToKorean
import kotlinx.coroutines.launch

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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs = remember { context.getSharedPreferences("siheung_gagae_prefs", android.content.Context.MODE_PRIVATE) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var appIdToCancel by remember { mutableStateOf(-1) }
    var acceptingAppId by remember { mutableStateOf(-1) }
    var pendingAcceptAppId by remember { mutableStateOf(-1) }
    var pendingRejectAppId by remember { mutableStateOf(-1) }

    LaunchedEffect(requestId) { viewModel.fetchDetail(requestId) }
    val requestData = (uiState as? MatchDetailUiState.Success)?.detail

    LaunchedEffect(requestData) {
        requestData?.let { detail ->
            if (detail.status?.trim()?.uppercase() == "DONE" &&
                viewModel.currentUserId == detail.author?.userId &&
                detail.isReviewed != true
            ) {
                showReviewDialog = true
            } else {
                showReviewDialog = false
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is MatchDetailUiState.DeleteSuccess) {
            snackbarHostState.showSnackbar("성공적으로 삭제되었습니다.")
            viewModel.resetState()
            onBack()
        }
    }

    if (showDeleteDialog) {
        SiheungAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "요청 글 삭제",
            text = "정말로 이 이동 지원 요청을 삭제하시겠습니까?",
            confirmText = "삭제",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteMatch(requestId)
            },
            dismissText = "취소",
            onDismiss = { showDeleteDialog = false },
            confirmColor = Pink500D,
            dismissColor = TextBlackD,
        )
    }

    if (pendingAcceptAppId != -1) {
        val appId = pendingAcceptAppId
        SiheungAlertDialog(
            onDismissRequest = { pendingAcceptAppId = -1 },
            title = "이 봉사자를 수락하시겠어요?",
            text = "수락 후에는 다른 지원자를 선택할 수 없으며 채팅이 열려요.",
            confirmText = "수락",
            confirmColor = Pink500D,
            onConfirm = {
                pendingAcceptAppId = -1
                acceptingAppId = appId
                viewModel.acceptApplication(requestId, appId) {
                    acceptingAppId = -1
                    scope.launch {
                        snackbarHostState.showSnackbar("수락했어요. 채팅을 시작해 보세요.")
                    }
                }
            },
            dismissText = "취소",
            onDismiss = { pendingAcceptAppId = -1 },
        )
    }

    if (pendingRejectAppId != -1) {
        val appId = pendingRejectAppId
        SiheungAlertDialog(
            onDismissRequest = { pendingRejectAppId = -1 },
            title = "이 신청을 거절하시겠어요?",
            text = "거절하면 해당 봉사자의 신청이 목록에서 사라져요.",
            confirmText = "거절",
            confirmColor = Pink500D,
            onConfirm = {
                pendingRejectAppId = -1
                viewModel.rejectApplication(requestId, appId)
            },
            dismissText = "취소",
            onDismiss = { pendingRejectAppId = -1 },
        )
    }

    if (showCancelDialog) {
        SiheungAlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = "매칭 취소",
            text = "정말로 이 봉사자와의 매칭을 취소하시겠습니까?\n취소 시 목록에서 제외되며 다시 새로운 지원을 받을 수 있습니다.",
            confirmText = "매칭 취소",
            onConfirm = {
                showCancelDialog = false
                if (appIdToCancel != -1) {
                    viewModel.cancelAcceptedMatching(requestId, appIdToCancel) {
                        scope.launch {
                            snackbarHostState.showSnackbar("매칭이 취소되었습니다.")
                        }
                    }
                }
            },
            dismissText = "유지하기",
            onDismiss = { showCancelDialog = false },
            confirmColor = Pink500D,
            dismissColor = TextBlackD,
        )
    }

    if (showReviewDialog) {
        SiheungAlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = "봉사 완료 확인",
            text = "봉사가 무사히 완료되었습니다!\n함께 이동한 지원자에게 따뜻한 후기를 남기시겠어요?",
            confirmText = "후기 작성하기",
            onConfirm = {
                showReviewDialog = false
                viewModel.resetState()
                onNavigate(Screen.MatchReview.createRoute(requestId, "DONE", isViewOnly = false, canEdit = true))
            },
            dismissText = "나중에 하기",
            onDismiss = {
                showReviewDialog = false
                onBack()
            },
            confirmColor = Pink500D,
            dismissColor = TextBlackD,
        )
    }

    Scaffold(
        containerColor = BackgroundD,
        snackbarHost = { SiheungSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            val isMenuOwner = requestData?.author?.userId == viewModel.currentUserId
            val currentStatus = requestData?.status?.trim()?.uppercase() ?: ""
            MatchingDetailTopBar(
                onBack = onBack,
                onDelete = { showDeleteDialog = true },
                onEdit = { onNavigate("request_flow?requestId=$requestId") },
                isMyRequest = isMenuOwner,
                currentStatus = currentStatus
            )
        },
        bottomBar = {
            val successState = uiState as? MatchDetailUiState.Success
            successState?.let {
                val currentStatus = it.detail.status?.trim()?.uppercase() ?: ""
                val isMenuOwner = viewModel.currentUserId == it.detail.author?.userId

                if (isMenuOwner && (currentStatus == "WAITING" || currentStatus == "MATCHING")) {
                    return@let
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundD)
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    if (isMenuOwner) {
                        if (currentStatus == "PROGRESS") {
                            Button(
                                onClick = {
                                    viewModel.updateMatchStatus(requestId, "DONE") {
                                        showReviewDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = Pink500D),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Text(
                                    text = "완료 처리하기",
                                    fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                            }
                        } else if (currentStatus == "DONE") {
                            Button(
                                onClick = {
                                    if (!viewModel.isReviewWritten) {
                                        onNavigate(Screen.MatchReview.createRoute(requestId, "DONE", isViewOnly = false, canEdit = true))
                                    } else {
                                        onNavigate(Screen.MatchReview.createRoute(requestId, "DONE", isViewOnly = true, canEdit = true))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!viewModel.isReviewWritten) Pink500D else Brown700D
                                ),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Text(
                                    text = if (!viewModel.isReviewWritten) "봉사 후기 작성하기" else "내가 작성한 후기 보기",
                                    fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                            }
                        }
                    } else {
                        if (currentStatus == "DONE") {
                            Button(
                                onClick = {
                                    onNavigate(Screen.MatchReview.createRoute(requestId, "DONE", isViewOnly = true, canEdit = false))
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = Brown700D),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Text(
                                    text = "요청자가 작성한 후기 보기",
                                    fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                            }
                        } else {
                            val cleanAppStatus = viewModel.myApplicationStatus?.trim()?.uppercase() ?: ""
                            when (cleanAppStatus) {
                                "ACCEPTED" -> {
                                    Button(
                                        onClick = {
                                            val appId = viewModel.myApplicationId ?: 0
                                            onNavigate(Screen.Chat.createRoute(requestId, appId))
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                                        colors = ButtonDefaults.buttonColors(containerColor = Pink500D),
                                        shape = RoundedCornerShape(26.dp)
                                    ) {
                                        Text("요청자와 채팅하기", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                "PENDING" -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Gray300D, disabledContainerColor = Gray300D),
                                        shape = RoundedCornerShape(26.dp)
                                    ) {
                                        Text("수락 대기 중입니다", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Brown700D)
                                    }
                                }
                                "REJECTED" -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2), disabledContainerColor = Color(0xFFF2F2F2)),
                                        shape = RoundedCornerShape(26.dp)
                                    ) {
                                        Text("매칭이 무산되었습니다", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                }
                                "CANCELED" -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2), disabledContainerColor = Color(0xFFF2F2F2)),
                                        shape = RoundedCornerShape(26.dp)
                                    ) {
                                        Text("신청이 취소되었습니다", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { onNavigate("apply_flow?requestId=$requestId") },
                                        modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                                        colors = ButtonDefaults.buttonColors(containerColor = Pink500D),
                                        shape = RoundedCornerShape(26.dp)
                                    ) {
                                        Text("봉사 신청하기", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val detailState = uiState
            when (detailState) {
                is MatchDetailUiState.Loading -> CircularProgressIndicator(color = Orange500D, modifier = Modifier.align(Alignment.Center))
                is MatchDetailUiState.Error -> Text(text = detailState.message, color = Pink500D, fontFamily = PretendardFamily, modifier = Modifier.align(Alignment.Center))
                is MatchDetailUiState.Success -> {
                    val request = detailState.detail
                    val blockedUsers = prefs.getStringSet("blocked_users", emptySet()) ?: emptySet()

                    val displayList = remember(request.status, viewModel.applicantList, blockedUsers) {
                        val status = request.status?.trim()?.uppercase()
                        val base = if (status == "DONE") {
                            viewModel.applicantList.filter { it.status?.trim()?.uppercase() == "ACCEPTED" }
                        } else {
                            viewModel.applicantList.filter {
                                val s = it.status?.trim()?.uppercase()
                                s != "CANCELED" && s != "REJECTED"
                            }
                        }
                        base.filter { it.applicant?.nickname !in blockedUsers }
                    }

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatusBannerD(statusText = matchStatusToKorean(request.status))
                            RequestInfoCardD(request = request)
                            PublicMapCard(latitude = request.latitude, longitude = request.longitude)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (request.status?.trim()?.uppercase() == "DONE") "함께 이동한 봉사자" else "지원자 현황 (${displayList.size}명)",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlackD
                            )

                            if (displayList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp)).padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "아직 신청한 지원자가 없습니다.", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700D)
                                }
                            } else {
                                displayList.forEach { appItem ->
                                    val applicantName = appItem.applicant?.nickname ?: "지원자"
                                    val isCardAccepted = appItem.status?.trim()?.uppercase() == "ACCEPTED"

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
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(MintLightD)
                                        ) {
                                            Text(text = applicantName.firstOrNull()?.toString() ?: "", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green600D)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = applicantName, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackD)
                                            Spacer(Modifier.height(2.dp))
                                            Text(text = appItem.message ?: "신청 메시지가 없습니다.", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700D, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }

                                        if (request.status?.trim()?.uppercase() == "DONE") {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Gray300D, RoundedCornerShape(8.dp))
                                                    .background(Color.White.copy(alpha = 0.5f))
                                                    .clickable {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("완료된 매칭의 채팅은 더 이상 이용할 수 없어요.")
                                                        }
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("채팅", fontFamily = PretendardFamily, fontSize = 12.sp, color = Gray300D)
                                            }
                                        } else if (isCardAccepted) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .border(1.dp, Gray300D, RoundedCornerShape(8.dp))
                                                        .background(Color.White)
                                                        .clickable {
                                                            val appId = appItem.applicationId ?: 0
                                                            onNavigate(Screen.Chat.createRoute(requestId, appId))
                                                        }.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text("채팅", fontFamily = PretendardFamily, fontSize = 12.sp, color = TextBlackD)
                                                }

                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .border(1.dp, Pink500D, RoundedCornerShape(8.dp))
                                                        .background(Color.White)
                                                        .clickable {
                                                            appIdToCancel = appItem.applicationId ?: -1
                                                            showCancelDialog = true
                                                        }.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text("취소", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Pink500D)
                                                }
                                            }
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, Gray300D, RoundedCornerShape(8.dp)).background(Color.White).clickable {
                                                        val appId = appItem.applicationId ?: 0
                                                        onNavigate(Screen.Chat.createRoute(requestId, appId))
                                                    }.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text("채팅", fontFamily = PretendardFamily, fontSize = 12.sp, color = TextBlackD)
                                                }

                                                val appId = appItem.applicationId ?: 0
                                                val isAccepting = acceptingAppId == appId
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(
                                                        if (isAccepting) Pink500D.copy(alpha = 0.6f) else Pink500D
                                                    ).clickable(enabled = !isAccepting) {
                                                        pendingAcceptAppId = appId
                                                    }.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    if (isAccepting) {
                                                        CircularProgressIndicator(
                                                            color = Color.White,
                                                            strokeWidth = 2.dp,
                                                            modifier = Modifier.size(14.dp),
                                                        )
                                                    } else {
                                                        Text("수락", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }

                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, Gray300D, RoundedCornerShape(8.dp)).background(Color.White).clickable {
                                                        pendingRejectAppId = appItem.applicationId ?: 0
                                                    }.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text("거절", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700D)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MatchingDetailTopBar(
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    isMyRequest: Boolean,
    currentStatus: String
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
            text = if (isMyRequest) "내 봉사 상세" else "참여 봉사 상세",
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
            if (isMyRequest && currentStatus != "DONE") {
                TopBarIconD(imageVector = Icons.Default.Edit, desc = "수정", onClick = onEdit, tint = Brown700D)
                TopBarIconD(imageVector = Icons.Default.Delete, desc = "삭제", onClick = onDelete, tint = Pink500D)
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

        val images = request.imageUrls.orEmpty()
        if (images.isNotEmpty()) {
            HorizontalDivider(color = Gray300D)
            Text(text = "첨부 사진", fontFamily = PretendardFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextBlackD)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images, key = { it }) { url ->
                    AppAsyncImage(
                        model = url,
                        contentDescription = "첨부 사진",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
        }
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