package com.example.siheunggagae.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.MatchReviewViewModel
import com.example.siheunggagae.ui.viewmodel.ReviewUiState

private val BgC          = Color(0xFFFFFFFF)
private val TextBlackC   = Color(0xFF1F130B)
private val Brown700C    = Color(0xFF8B6F59)
private val Pink500C     = Color(0xFFF14369)
private val Gray300C     = Color(0xFFE9E9E9)
private val InputBgC     = Color(0xFFF8F8F8)
private val PlaceholderC = Color(0xFFC1AFA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchReviewScreen(
    matchId: Int,
    matchStatus: String,
    isViewOnly: Boolean = false, // 🌟 [신규] 처음부터 조회 모드로 들어왔는지 여부 필터
    viewModel: MatchReviewViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // 화면 제어용 로컬 상태 변수들
    var isReadOnly by remember { mutableStateOf(isViewOnly) }
    var rating by remember { mutableStateOf(5) }
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
            viewModel.loadReview(matchId) // 뷰모델에 새로 추가할 조회 메서드
        }
    }

    // [태은-8.1] 진입 가드 (새로 작성할 때만 작동하도록 방어)
    LaunchedEffect(matchStatus) {
        if (!isViewOnly && matchStatus != "DONE") {
            Toast.makeText(context, "완료된 봉사 활동만 후기를 작성할 수 있습니다.", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    // 🌟 [신규 매핑] 서버에서 후기 조회 성공 시 로컬 필드에 데이터 바인딩 시키기
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ReviewUiState.Success -> {
                // 만약 불러오기 성공 데이터가 존재한다면 (조회/수정 모드 대응)
                if (state.review != null) {
                    rating = state.review.rating ?:5
                    reviewText = state.review.content ?: ""
                    // 이미지는 필요시 추가 매핑
                } else {
                    // 순수 새 글 등록 성공 시의 기존 흐름
                    Toast.makeText(context, "후기가 성공적으로 반영되었습니다! ⭐", Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                    onBack()
                }
            }
            is ReviewUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BgC,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isReadOnly) "작성한 후기 보기" else if (isViewOnly) "후기 수정하기" else "봉사 후기 작성",
                        fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlackC
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = TextBlackC)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgC)
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
                // ── [태은-8.2] 점수 선택 구역 ──
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isReadOnly) "내가 남긴 별점" else "활동은 어떠셨나요?",
                        fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackC
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..5) {
                            val isSelected = i <= rating
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i 점",
                                tint = if (isSelected) Color(0xFFFFB200) else Color(0xFFE8E8E8),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable(enabled = !isReadOnly) { rating = i } // 🌟 조회 모드일 땐 터치 비활성화
                            )
                        }
                    }
                }

                // ── [태은-8.3] 후기 본문 입력 및 출력 구역 ──
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isReadOnly) "작성했던 후기 내용" else "정성스러운 후기를 남겨주세요",
                        fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackC
                    )
                    BasicTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        enabled = !isReadOnly, // 🌟 조회 모드일 땐 읽기 전용 가드
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isReadOnly) Color(0xFFF4F4F4) else InputBgC)
                            .border(1.dp, Gray300C, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackC),
                        decorationBox = { innerTextField ->
                            if (reviewText.isEmpty()) {
                                Text("작성된 후기 내용이 없습니다.", fontFamily = PretendardFamily, fontSize = 14.sp, color = PlaceholderC)
                            }
                            innerTextField()
                        }
                    )
                }

                // ── [태은-8.4] 인증 사진 첨부 구역 (조회 모드일 땐 가시성 숨김) ──
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

                // ── 제어 버튼 액션 구역 ──
                if (isReadOnly) {
                    // 1️⃣ 조회 모드일 때: [후기 수정하기] 버튼 활성화
                    Button(
                        onClick = { isReadOnly = false }, // 터치 시 입력 모드로 탈탈 개조
                        modifier = Modifier.fillMaxWidth().height(52.dp).shadow(2.dp, RoundedCornerShape(26.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Brown700C),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text("후기 수정하기", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    // 2️⃣ 입력/편집 모드일 때: 상황에 따라 POST(등록) 또는 PATCH(수정) 분기 처리
                    Button(
                        onClick = {
                            if (reviewText.isBlank()) {
                                Toast.makeText(context, "후기 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isViewOnly) {
                                viewModel.modifyReview(matchId, rating, reviewText) {
                                    isReadOnly = true // 완료 후 다시 조회 모드로 잠금
                                    Toast.makeText(context, "후기가 깔끔하게 수정되었습니다! ✏️", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // 기존의 오리지널 POST 후기 등록 트리거
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