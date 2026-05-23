package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
import com.example.siheunggagae.Screen
import com.example.siheunggagae.data.model.ChatMessageItem
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.viewmodel.ChatUiState
import com.example.siheunggagae.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

private val BgC          = Color(0xFFFFFFFF)
private val TextBlackC   = Color(0xFF1F130B)
private val Brown700C    = Color(0xFF8B6F59)
private val Brown900C    = Color(0xFF614C3B)
private val Pink500C     = Color(0xFFF14369)
private val MintLightC   = Color(0xFFD1FFE2)
private val Green500C    = Color(0xFF00A73F)
private val PinkSurfaceC = Color(0xFFFEE8ED)
private val Gray300C     = Color(0xFFE9E9E9)
private val InputBgC     = Color(0xFFF3F3F3)
private val PlaceholderC = Color(0xFFC1AFA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    matchId: Int,
    applicationId: Int,
    viewModel: ChatViewModel,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var inputText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showReportDialog by remember { mutableStateOf(false) }
    var targetMsgId by remember { mutableStateOf(-1) }
    var targetSenderId by remember { mutableStateOf(-1) }
    var reportReason by remember { mutableStateOf("") }

    var showTopMenuBottomSheet by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showUserReportDialog by remember { mutableStateOf(false) }
    var userReportReason by remember { mutableStateOf("") }

    LaunchedEffect(matchId, applicationId) {
        viewModel.initChatRoom(matchId, applicationId)
    }

    val successState = uiState as? ChatUiState.Success
    if (successState != null && successState.hasMore) {
        val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
        LaunchedEffect(firstVisibleItemIndex) {
            if (firstVisibleItemIndex == 0) {
                viewModel.loadMoreMessages()
            }
        }
    }

    Scaffold(
        containerColor = BgC,
        snackbarHost = { SiheungSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            val state = uiState as? ChatUiState.Success
            val isAlreadyAccepted = state?.matchDetail?.status != null && state.matchDetail.status != "WAITING"

            ChatTopBar(
                title = state?.opponentNickname ?: "대화방",
                showAcceptBtn = state?.isMyRequest ?: false,
                isAlreadyAccepted = isAlreadyAccepted,
                onBack = onBack,
                onAcceptClick = { viewModel.acceptVolunteer { onBack() } },
                onCancelClick = { viewModel.cancelVolunteer { onBack() } },
                onMenuClick = { showTopMenuBottomSheet = true }
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendTextMessage(inputText)
                        inputText = ""
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is ChatUiState.Loading -> {
                    CircularProgressIndicator(color = Pink500C, modifier = Modifier.align(Alignment.Center))
                }
                is ChatUiState.Error -> {
                    Text(text = state.message, color = Pink500C, modifier = Modifier.align(Alignment.Center), fontFamily = PretendardFamily)
                }
                is ChatUiState.Success -> {
                    LaunchedEffect(state.messages.size) {
                        if (state.messages.isNotEmpty()) {
                            listState.animateScrollToItem(state.messages.lastIndex + 1)
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        state.matchDetail?.let { RequestPreviewCard(it) }
                        Spacer(Modifier.height(1.dp))

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item { Spacer(Modifier.height(4.dp)) }

                            itemsIndexed(state.messages, key = { _, msg -> msg.id }) { index, msg ->
                                val isMe = msg.senderId == viewModel.myUserId
                                val currentMsgDate = msg.createdAt.take(10)

                                if (index == 0) {
                                    DateDivider(label = currentMsgDate.replace("-", ". "))
                                    Spacer(Modifier.height(8.dp))
                                } else {
                                    val prevMsgDate = state.messages[index - 1].createdAt.take(10)
                                    if (currentMsgDate != prevMsgDate) {
                                        DateDivider(label = currentMsgDate.replace("-", ". "))
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }

                                if (isMe) {
                                    SentMessageItem(msg = msg)
                                } else {
                                    ReceivedMessageItem(
                                        msg = msg,
                                        name = state.opponentNickname,
                                        onLongClick = {
                                            targetMsgId = msg.id
                                            targetSenderId = msg.senderId ?: -1
                                            showReportDialog = true
                                        }
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }

        if (showTopMenuBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTopMenuBottomSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showTopMenuBottomSheet = false
                            showUserReportDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("상대방 사용자 신고하기", fontFamily = PretendardFamily, fontSize = 16.sp, color = TextBlackC, fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(color = Gray300C)
                    TextButton(
                        onClick = {
                            showTopMenuBottomSheet = false
                            showBlockConfirmDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("상대방 사용자 차단하기", fontFamily = PretendardFamily, fontSize = 16.sp, color = Pink500C, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showBlockConfirmDialog) {
            val state = uiState as? ChatUiState.Success
            val opponentId = state?.messages?.firstOrNull { it.senderId != viewModel.myUserId }?.senderId ?: -1

            AlertDialog(
                onDismissRequest = { showBlockConfirmDialog = false },
                title = { Text("사용자 차단", fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, color = TextBlackC) },
                text = { Text("정말로 이 유저를 차단하시겠습니까?\n차단 이후에는 해당 유저의 글과 메시지가 타임라인에서 영구히 숨김 처리되며, 매칭이 취소됩니다.", fontFamily = PretendardFamily, color = TextBlackC) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.blockUser(opponentId) { success ->
                                showBlockConfirmDialog = false
                                if (success) {
                                    val siheungPrefs = context.getSharedPreferences("siheung_gagae_prefs", android.content.Context.MODE_PRIVATE)
                                    val blockedSet = siheungPrefs.getStringSet("blocked_users", emptySet())?.toMutableSet() ?: mutableSetOf()

                                    state?.opponentNickname?.let { blockedSet.add(it) }
                                    siheungPrefs.edit().putStringSet("blocked_users", blockedSet).apply()

                                    scope.launch {
                                        snackbarHostState.showSnackbar("성공적으로 차단되었습니다.")
                                    }
                                    onBack()
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("차단 처리에 실패했습니다.")
                                    }
                                }
                            }
                        }
                    ) { Text("차단하기", color = Pink500C, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showBlockConfirmDialog = false }) { Text("취소", color = TextBlackC) }
                }
            )
        }

        if (showUserReportDialog) {
            val state = uiState as? ChatUiState.Success
            val opponentId = state?.messages?.firstOrNull { it.senderId != viewModel.myUserId }?.senderId ?: -1

            AlertDialog(
                onDismissRequest = { showUserReportDialog = false },
                title = { Text("사용자 신고하기", fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, color = TextBlackC) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("상대방의 프로필 부정 행위나 매너 위반 사항에 대해 사유를 기술해 주세요.", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700C)
                        OutlinedTextField(
                            value = userReportReason,
                            onValueChange = { userReportReason = it },
                            placeholder = { Text("사유를 기입해 주세요 (욕설/스팸/사기/기타)...", fontFamily = PretendardFamily) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (userReportReason.isNotBlank()) {
                                viewModel.reportUser(opponentId, userReportReason) { success ->
                                    showUserReportDialog = false
                                    userReportReason = ""
                                    val alert = if (success) "유저 신고 접수가 정상 처리되었습니다." else "신고 처리에 실패했습니다."
                                    scope.launch {
                                        snackbarHostState.showSnackbar(alert)
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("신고 사유를 작성해 주세요.")
                                }
                            }
                        }
                    ) { Text("신고 접수", color = Pink500C, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showUserReportDialog = false
                        userReportReason = ""
                    }) { Text("취소", color = TextBlackC) }
                }
            )
        }

        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("메시지 신고하기", fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, color = TextBlackC) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("해당 유저가 비매너 언행이나 부정 행위를 저질렀나요? 신고 사유를 적어주세요.", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700C)
                        OutlinedTextField(
                            value = reportReason,
                            onValueChange = { reportReason = it },
                            placeholder = { Text("신고 사유를 기술해주세요...", fontFamily = PretendardFamily) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (reportReason.isNotBlank()) {
                                viewModel.reportMessage(targetMsgId, targetSenderId, reportReason) { success ->
                                    showReportDialog = false
                                    reportReason = ""
                                    val alert = if (success) "정상적으로 신고 접수되었습니다." else "신고 처리에 실패했습니다."
                                    scope.launch {
                                        snackbarHostState.showSnackbar(alert)
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("사유를 반드시 입력해주세요.")
                                }
                            }
                        }
                    ) { Text("신고 접수", color = Pink500C, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showReportDialog = false
                        reportReason = ""
                    }) { Text("취소", color = TextBlackC) }
                }
            )
        }
    }
}

@Composable
private fun ChatTopBar(
    title: String,
    showAcceptBtn: Boolean,
    isAlreadyAccepted: Boolean,
    onBack: () -> Unit,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(BgC)
            .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(BgC)
                .clickable { onBack() },
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = TextBlackC, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(MintLightC)) {
            Text(text = title.firstOrNull()?.toString() ?: "", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green500C)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackC)
            Text(text = "⭐ 4.9 · 동네 매칭 회원", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700C)
        }
        if (showAcceptBtn) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isAlreadyAccepted) Color.Transparent else Pink500C)
                    .then(
                        if (isAlreadyAccepted) Modifier.border(1.dp, Color(0xFFBA1A1A), RoundedCornerShape(50.dp))
                        else Modifier
                    )
                    .clickable {
                        if (isAlreadyAccepted) onCancelClick() else onAcceptClick()
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = if (isAlreadyAccepted) "매칭 취소" else "수락",
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAlreadyAccepted) Color(0xFFBA1A1A) else Color.White
                )
            }
            Spacer(Modifier.width(4.dp))
        }

        IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "더보기 메뉴", tint = TextBlackC)
        }
    }
}

@Composable
private fun RequestPreviewCard(detail: MatchDetailResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 0.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(BgC)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(PinkSurfaceC)) {
            Icon(painter = painterResource(R.drawable.ic_handshake), contentDescription = null, tint = Pink500C, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = detail.title ?: "요청 정보", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackC, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "${detail.desiredDate?.take(10)} · ${detail.address}", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700C)
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(PinkSurfaceC).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(text = detail.status ?: "검토 중", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Pink500C)
        }
    }
}

@Composable
private fun DateDivider(label: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300C, thickness = 1.dp)
        Text(text = label, fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700C)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300C, thickness = 1.dp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReceivedMessageItem(msg: ChatMessageItem, name: String, onLongClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Bottom) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).clip(CircleShape).background(MintLightC)) {
            Text(text = name.firstOrNull()?.toString() ?: "", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Green500C)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(BgC)
                    .border(1.dp, Gray300C, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { onLongClick() }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(text = msg.content, fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackC)
            }
            Text(text = msg.createdAt.take(16).replace("T", " "), fontFamily = PretendardFamily, fontSize = 10.sp, color = Brown700C)
        }
    }
}

@Composable
private fun SentMessageItem(msg: ChatMessageItem) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(Brown700C)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(text = msg.content, fontFamily = PretendardFamily, fontSize = 14.sp, color = Color.White)
            }
            Text(text = msg.createdAt.take(16).replace("T", " "), fontFamily = PretendardFamily, fontSize = 10.sp, color = Brown700C)
        }
    }
}

@Composable
private fun ChatInputBar(inputText: String, onTextChange: (String) -> Unit, onSendClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgC)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp))
                .background(InputBgC)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackC),
            decorationBox = { innerTextField ->
                if (inputText.isEmpty()) {
                    Text(text = "메시지를 입력하세요...", fontFamily = PretendardFamily, fontSize = 14.sp, color = PlaceholderC)
                }
                innerTextField()
            },
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brown900C)
                .clickable { onSendClick() },
        ) {
            Icon(painter = painterResource(R.drawable.ic_send), contentDescription = "전송", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}