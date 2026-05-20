package com.example.siheunggagae.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.PetGender
import com.example.siheunggagae.data.model.PetSpecies
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.viewmodel.PetAddUiState
import com.example.siheunggagae.ui.viewmodel.PetAddViewModel
import androidx.compose.material3.SnackbarHostState // 추가
import androidx.compose.runtime.rememberCoroutineScope // 추가
import kotlinx.coroutines.launch // 추가
import com.example.siheunggagae.ui.component.SiheungSnackbarHost // 추가

// 스펙 컬러
private val Brown900PA    = Color(0xFF614B3A)
private val Brown700PA    = Color(0xFF8A6E58)
private val Brown400PA    = Color(0xFFC4A882)
private val BrownBorderPA = Color(0xFFE8D3C2)
private val Orange500PA   = Color(0xFFF7A35B)
private val Gray300PA     = Color(0xFFE8E8E8)
private val OrangeSandPA  = Color(0xFFFFEDD4)
private val BackgroundPA  = Color(0xFFFEFEFE)
private val TextBlackPA   = Color(0xFF1E120A)

private val speciesOptions = listOf("강아지", "고양이", "기타")
private val genderOptions  = listOf("수컷", "암컷")
private val ageUnitOptions = listOf("살", "개월")

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun PetAddScreen(
    viewModel: PetAddViewModel, // 뷰모델 추가
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    // 스낵바 상태와 코루틴 스코프 생성
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var nameInput       by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf("강아지") }
    var breedInput      by remember { mutableStateOf("") }
    var age             by remember { mutableIntStateOf(1) }
    var ageUnit         by remember { mutableStateOf("살") }
    var gender          by remember { mutableStateOf("수컷") }
    var isNeutered      by remember { mutableStateOf(false) }
    var noteInput       by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PetAddUiState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("반려동물이 성공적으로 등록되었습니다!")
                }
                viewModel.resetState()
                onBack()
            }
            is PetAddUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar((uiState as PetAddUiState.Error).message)
                }
                viewModel.resetState()
            }
            else -> {}
        }
    }
    Scaffold(
        containerColor = BackgroundPA,
        snackbarHost = { SiheungSnackbarHost(snackbarHostState) },
        topBar = { PetAddTopBar(onBack = onBack) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                // 한글 선택값을 서버 Enum 값으로 변환
                val speciesEnum = when(selectedSpecies) {
                    "강아지" -> PetSpecies.DOG
                    "고양이" -> PetSpecies.CAT
                    else -> PetSpecies.OTHER
                }
                val genderEnum = when(gender) {
                    "수컷" -> PetGender.MALE
                    "암컷" -> PetGender.FEMALE
                    else -> PetGender.UNKNOWN
                }
                val isButtonEnabled = nameInput.isNotBlank() && uiState !is PetAddUiState.Loading

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isButtonEnabled) Brown700PA else Color(0xFFE5E7EB))
                        .clickable(enabled = isButtonEnabled) {
                            // 🔥 뷰모델로 데이터 전송
                            viewModel.addPet(
                                name = nameInput,
                                species = speciesEnum,
                                breed = breedInput,
                                ageStr = age.toString(),
                                weightStr = "", // UI에 몸무게가 없으므로 빈값 처리 (서버에서 null로 받음)
                                isNeutered = isNeutered,
                                gender = genderEnum
                            )
                        },
                ) {
                    if (uiState is PetAddUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "저장하기",
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp,
                            color = if (isButtonEnabled) Color.White else Brown400PA,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            PetPreviewCard(
                name = nameInput,
                species = selectedSpecies,
                age = age,
                ageUnit = ageUnit,
                gender = gender,
            )

            PetSectionLabelPA("기본 정보")
            BasicInfoCard(
                nameInput = nameInput,
                onNameChange = { nameInput = it },
                selectedSpecies = selectedSpecies,
                onSpeciesSelect = { selectedSpecies = it },
                breedInput = breedInput,
                onBreedChange = { breedInput = it },
            )

            PetSectionLabelPA("상세 정보")
            DetailInfoCard(
                age = age,
                ageUnit = ageUnit,
                onDecrement = { if (age > 1) age-- },
                onIncrement = { age++ },
                onAgeUnitSelect = { ageUnit = it },
                gender = gender,
                onGenderSelect = { gender = it },
                isNeutered = isNeutered,
                onNeuteredChange = { isNeutered = it },
            )

            NoteSection(
                note = noteInput,
                onNoteChange = { if (it.length <= 300) noteInput = it },
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun PetAddTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackPA,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "반려동물 추가",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackPA,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 미리보기 Card ─────────────────────────────────────────────────────────────

@Composable
private fun PetPreviewCard(
    name: String,
    species: String,
    age: Int,
    ageUnit: String,
    gender: String,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OrangeSandPA),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pets),
                    contentDescription = null,
                    tint = Orange500PA,
                    modifier = Modifier.size(32.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "미리보기",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp,
                    color = Brown700PA,
                )
                Text(
                    text = name.ifBlank { "이름을 입력하세요" },
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = if (name.isBlank()) Brown400PA else TextBlackPA,
                )
                Text(
                    text = "$species · ${age}$ageUnit · $gender",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Brown700PA,
                )
            }
        }
    }
}

// ─── 섹션 라벨 ─────────────────────────────────────────────────────────────────

@Composable
private fun PetSectionLabelPA(label: String, isBlack: Boolean = false) {
    Text(
        text = label,
        fontFamily = PretendardFamily,
        fontSize = if (isBlack) 13.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = if (isBlack) 20.sp else 16.sp,
        color = if (isBlack) TextBlackPA else Brown700PA,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

// ─── 기본 정보 Card ────────────────────────────────────────────────────────────

@Composable
private fun BasicInfoCard(
    nameInput: String,
    onNameChange: (String) -> Unit,
    selectedSpecies: String,
    onSpeciesSelect: (String) -> Unit,
    breedInput: String,
    onBreedChange: (String) -> Unit,
) {
    PetInfoCard {
        // 이름
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "이름",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackPA,
            )
            Spacer(Modifier.weight(1f))
            BasicTextField(
                value = nameInput,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = TextBlackPA,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = SolidColor(Orange500PA),
                modifier = Modifier.width(160.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                        if (nameInput.isEmpty()) {
                            Text(
                                text = "예: 파댕이",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Brown400PA,
                                textAlign = TextAlign.End,
                            )
                        }
                        inner()
                    }
                },
            )
        }

        PetCardDivider()

        // 종류 칩
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "종류",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackPA,
            )
            Spacer(Modifier.weight(1f))
            speciesOptions.forEach { option ->
                PetSelectChip(
                    label = option,
                    selected = option == selectedSpecies,
                    onClick = { onSpeciesSelect(option) },
                )
            }
        }

        PetCardDivider()

        // 품종
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "품종(선택)",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackPA,
            )
            Spacer(Modifier.weight(1f))
            BasicTextField(
                value = breedInput,
                onValueChange = onBreedChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = TextBlackPA,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = SolidColor(Orange500PA),
                modifier = Modifier.width(160.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                        if (breedInput.isEmpty()) {
                            Text(
                                text = "예: 말티즈",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Brown400PA,
                                textAlign = TextAlign.End,
                            )
                        }
                        inner()
                    }
                },
            )
        }
    }
}

// ─── 상세 정보 Card ────────────────────────────────────────────────────────────

@Composable
private fun DetailInfoCard(
    age: Int,
    ageUnit: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onAgeUnitSelect: (String) -> Unit,
    gender: String,
    onGenderSelect: (String) -> Unit,
    isNeutered: Boolean,
    onNeuteredChange: (Boolean) -> Unit,
) {
    PetInfoCard {
        // 나이
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "나이",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackPA,
            )
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AgeControlButton(icon = { Text("−", color = Brown700PA, fontSize = 16.sp, fontFamily = PretendardFamily, fontWeight = FontWeight.Bold) }, onClick = onDecrement)
                Text(
                    text = "$age",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp,
                    color = TextBlackPA,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center,
                )
                AgeControlButton(icon = { Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, tint = Brown700PA, modifier = Modifier.size(16.dp)) }, onClick = onIncrement)

                Spacer(Modifier.width(4.dp))

                ageUnitOptions.forEach { unit ->
                    PetSelectChip(
                        label = unit,
                        selected = unit == ageUnit,
                        onClick = { onAgeUnitSelect(unit) },
                    )
                }
            }
        }

        PetCardDivider()

        // 성별
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "성별",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackPA,
            )
            Spacer(Modifier.weight(1f))
            genderOptions.forEach { option ->
                PetSelectChip(
                    label = option,
                    selected = option == gender,
                    onClick = { onGenderSelect(option) },
                )
            }
        }

        PetCardDivider()

        // 중성화
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "중성화",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackPA,
            )
            Spacer(Modifier.weight(1f))
            Switch(
                checked = isNeutered,
                onCheckedChange = onNeuteredChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Brown900PA,
                    checkedBorderColor = Brown900PA,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Gray300PA,
                    uncheckedBorderColor = Gray300PA,
                ),
            )
        }
    }
}

// ─── 특징 및 주의사항 (서버 전송 X, UI 유지용) ──────────────────────────────────

@Composable
private fun NoteSection(note: String, onNoteChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PetSectionLabelPA("특징 및 주의사항", isBlack = true)
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                textStyle = TextStyle(
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = TextBlackPA,
                ),
                cursorBrush = SolidColor(Orange500PA),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .border(1.dp, BrownBorderPA, RoundedCornerShape(12.dp))
                    .padding(top = 13.dp, start = 17.dp, end = 17.dp, bottom = 1.dp)
                    .padding(bottom = 24.dp),
                decorationBox = { inner ->
                    if (note.isEmpty()) {
                        Text(
                            text = "알러지, 질환, 성격 등 참고할 만한 내용을 자유롭게 적어주세요.",
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 24.sp,
                            color = Brown400PA,
                        )
                    }
                    inner()
                },
            )
            Text(
                text = "${note.length} / 300",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp,
                color = Brown700PA,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 8.dp),
            )
        }
    }
}

// ─── 공통 컴포넌트 ──────────────────────────────────────────────────────────────

@Composable
private fun PetInfoCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        content()
    }
}

@Composable
private fun PetCardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFF3F4F6),
    )
}

@Composable
private fun PetSelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) Brown900PA else Color.White)
            .then(
                if (!selected) Modifier.border(1.dp, BrownBorderPA, RoundedCornerShape(50.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            color = if (selected) Color.White else Brown700PA,
        )
    }
}

@Composable
private fun AgeControlButton(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BrownBorderPA, RoundedCornerShape(8.dp))
            .clickable { onClick() },
    ) {
        icon()
    }
}