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
    matchStatus: String, // [태은-8.1] 매칭 상태 진입 가드용
    viewModel: MatchReviewViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var rating by remember { mutableStateOf(5) } // [태은-8.2] 기본 별점 5점
    var reviewText by remember { mutableStateOf("") } // [태은-8.3] 후기 텍스트
    var selectedImageUris by remember { mutableStateOf<List<String>>(emptyList()) } // [태은-8.4] 이미지 URI 목록

    // [태은-8.4] 이미지 픽커 라운처 설정 (최대 10장)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris.map { it.toString() }
        }
    }

    // [태은-8.1] 진입 가드: DONE 상태가 아니면 차단 후 퇴출
    LaunchedEffect(matchStatus) {
        if (matchStatus != "DONE") {
            Toast.makeText(context, "완료된 봉사 활동만 후기를 작성할 수 있습니다.", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    // 응답 결과 구독 파이프라인
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ReviewUiState.Success -> {
                Toast.makeText(context, "후기가 성공적으로 등록되었습니다! ⭐", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onBack()
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
                title = { Text("봉사 후기 작성", fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlackC) },
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
                // ── [태은-8.2] 점수 선택 ──
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("활동은 어떠셨나요?", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackC)
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
                                    .clickable { rating = i }
                            )
                        }
                    }
                }

                // ── [태은-8.3] 후기 본문 입력 구역 ──
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("정성스러운 후기를 남겨주세요", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackC)
                    BasicTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputBgC)
                            .border(1.dp, Gray300C, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackC),
                        decorationBox = { innerTextField ->
                            if (reviewText.isEmpty()) {
                                Text("여기에 내용을 입력해주세요. 작성해주신 후기는 상대방 프로필에 반영됩니다.", fontFamily = PretendardFamily, fontSize = 14.sp, color = PlaceholderC)
                            }
                            innerTextField()
                        }
                    )
                }

                // ── [태은-8.4] 인증 사진 첨부 구역 ──
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("인증 사진 첨부 (선택, 최대 10장)", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackC)

                    // 👈 [버그 수정 완료] Row를 Column으로 변경하여 정렬 옵션 충돌을 해결했습니다!
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
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "사진 추가",
                            tint = Brown700C,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (selectedImageUris.isEmpty()) "사진 추가하기" else "사진 ${selectedImageUris.size}장 선택됨",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            color = Brown700C
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // ── [태은-8.5] 후기 등록 버튼 ──
                Button(
                    onClick = {
                        if (reviewText.isBlank()) {
                            Toast.makeText(context, "후기 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.submitReview(matchId, rating, reviewText, selectedImageUris) {
                            // 등록 액션 성공 콜백
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(2.dp, RoundedCornerShape(26.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink500C),
                    shape = RoundedCornerShape(26.dp),
                    enabled = uiState !is ReviewUiState.Loading
                ) {
                    if (uiState is ReviewUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("후기 등록하기", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}