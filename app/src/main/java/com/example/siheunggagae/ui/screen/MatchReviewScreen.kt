package com.example.siheunggagae.ui.screen

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.MatchReviewViewModel
import com.example.siheunggagae.ui.viewmodel.ReviewUiState
import kotlinx.coroutines.launch

private val BgC          = Color(0xFFFFFFFF)
private val TextBlackC   = Color(0xFF1F130B)
private val Brown700C    = Color(0xFF8B6F59)
private val Pink500C     = Color(0xFFF14369)
private val Gray300C     = Color(0xFFE9E9E9)
private val InputBgC     = Color(0xFFF8F8F8)
private val PlaceholderC = Color(0xFFC1AFA0)

private val ratingLabels = mapOf(
    1 to "아쉬웠어요 😢",
    2 to "조금 아쉬워요 😕",
    3 to "보통이에요 😐",
    4 to "좋았어요 😊",
    5 to "훌륭했어요! 🤩",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchReviewScreen(
    matchId: Int,
    matchStatus: String,
    isViewOnly: Boolean = false,
    canEdit: Boolean = false,
    viewModel: MatchReviewViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isReadOnly by remember { mutableStateOf(isViewOnly) }
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<String>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris.map { it.toString() }
        }
    }

    LaunchedEffect(matchId, isViewOnly) {
        if (isViewOnly) {
            viewModel.loadReview(matchId)
        }
    }

    LaunchedEffect(matchStatus) {
        if (!isViewOnly && matchStatus != "DONE") {
            snackbarHostState.showSnackbar("완료된 봉사 활동만 후기를 작성할 수 있습니다.")
            onBack()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ReviewUiState.Success -> {
                if (state.review != null) {
                    rating = state.review.rating ?: 5
                    reviewText = state.review.content ?: ""
                } else if (!isViewOnly) {
                    snackbarHostState.showSnackbar("후기가 성공적으로 반영되었습니다!")
                    viewModel.resetState()
                    onBack()
                }

            }
            is ReviewUiState.Error -> {

                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BgC,
        snackbarHost = { SiheungSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (!canEdit) "요청자가 남긴 후기"
                        else if (isReadOnly) "작성한 후기 보기"
                        else "후기",
                        fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlackC,
                        letterSpacing = (-0.36).sp,
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "뒤로",
                            tint = TextBlackC,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                actions = { Spacer(Modifier.size(40.dp).padding(end = 12.dp)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgC)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (!canEdit) "요청자가 남긴 별점" else if (isReadOnly) "내가 남긴 별점" else "활동은 어떠셨나요?",
                        fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackC
                    )
                    Spacer(Modifier.height(12.dp))
                    val starHaptic = LocalHapticFeedback.current
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..5) {
                                val isSelected = i <= rating
                                val starScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.15f else 1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessHigh),
                                    label = "starScale_$i",
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(enabled = !isReadOnly) {
                                            if (rating != i) {
                                                rating = i
                                                starHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$i 점",
                                        tint = if (isSelected) Color(0xFFFFB200) else Color(0xFFE8D3C2),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .scale(starScale),
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(
                            visible = rating > 0,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = ratingLabels[rating] ?: "",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF7A35B),
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (!canEdit) "후기 상세 내용" else if (isReadOnly) "작성했던 후기 내용" else "정성스러운 후기를 남겨주세요",
                        fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackC
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BasicTextField(
                            value = reviewText,
                            onValueChange = { if (it.length <= 500) reviewText = it },
                            enabled = !isReadOnly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isReadOnly) Color(0xFFF4F4F4) else InputBgC)
                                .border(1.dp, Gray300C, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                                .padding(bottom = 16.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackC),
                            decorationBox = { innerTextField ->
                                if (reviewText.isEmpty()) {
                                    Text("작성된 후기 내용이 없습니다.", fontFamily = PretendardFamily, fontSize = 14.sp, color = PlaceholderC)
                                }
                                innerTextField()
                            }
                        )
                        if (!isReadOnly) {
                            Text(
                                text = "${reviewText.length} / 500",
                                fontFamily = PretendardFamily,
                                fontSize = 12.sp,
                                color = Brown700C,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, bottom = 6.dp),
                            )
                        }
                    }
                }

                if (!isReadOnly) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("인증 사진 첨부 (선택, 최대 10장)", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackC)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(InputBgC)
                                .clickable {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "사진 추가", tint = Brown700C, modifier = Modifier.size(24.dp))
                            Text(text = if (selectedImageUris.isEmpty()) "사진 추가하기" else "사진 ${selectedImageUris.size}장 선택됨", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700C)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (isReadOnly) {
                    if (canEdit) {
                        Button(
                            onClick = { isReadOnly = false },
                            modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Brown700C),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text("후기 수정하기", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (rating <= 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("별점을 선택해 주세요.")
                                }
                                return@Button
                            }
                            if (reviewText.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("후기 내용을 입력해 주세요.")
                                }
                                return@Button
                            }
                            if (isViewOnly) {
                                viewModel.modifyReview(matchId, rating, reviewText) {
                                    isReadOnly = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar("후기가 깔끔하게 수정되었습니다!")
                                    }
                                }
                            } else {
                                viewModel.submitReview(matchId, rating, reviewText, selectedImageUris) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Pink500C),
                        shape = RoundedCornerShape(26.dp),
                        enabled = uiState !is ReviewUiState.Loading
                    ) {
                        if (uiState is ReviewUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isViewOnly) "수정 완료하기" else "후기 등록하기",
                                fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}