package com.example.siheunggagae

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import java.time.DayOfWeek
import java.time.YearMonth

// 스펙 컬러
private val Brown900F    = Color(0xFF614B3A)
private val Brown700F    = Color(0xFF8A6E58)
private val Brown400F    = Color(0xFFC4A882)
private val BrownBorderF = Color(0xFFE8D3C2)
private val Orange500F   = Color(0xFFF7A35B)
private val Orange100F   = Color(0xFFE8D3C2)
private val Pink500F     = Color(0xFFF04268)
private val Blue400F     = Color(0xFF388AF5)
private val GrayBg       = Color(0xFFF4F4F4)
private val GrayText     = Color(0xFF6B7280)
private val TextBlack    = Color(0xFF1E120A)
private val OrangeSand   = Color(0xFFFFEDD4)

// ─── 데이터 ────────────────────────────────────────────────────────────────────

data class Pet(
    val id: Int,
    val name: String,
    val species: String,
    val age: String,
    val weight: String,
)

private val samplePets = listOf(
    Pet(1, "초코", "강아지", "3세", "8kg"),
    Pet(2, "나비", "고양이", "2세", "4kg"),
)

private val quickTimes = listOf(
    "09:00", "10:00", "11:00", "12:00",
    "13:00", "14:00", "15:00", "16:00",
    "17:00", "18:00", "19:00", "20:00",
)

private val dayHeaders = listOf("일", "월", "화", "수", "목", "금", "토")

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun RequestFlowScreen(onBack: () -> Unit = {}, onComplete: () -> Unit = {}, onAddPet: () -> Unit = {}) {
    var currentStep by remember { mutableStateOf(1) }

    var selectedPetId by remember { mutableStateOf<Int?>(null) }

    var currentMonth by remember { mutableStateOf(YearMonth.of(2026, 10)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var timeInput by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    var titleInput by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    val buttonText = when (currentStep) {
        1    -> "→ 일정 선택"
        2    -> "→ 요청 내용 작성"
        else -> "요청 등록하기"
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            RequestFlowTopBar(
                step = currentStep,
                onBack = { if (currentStep > 1) currentStep-- else onBack() }
            )
        },
        bottomBar = {
            FlowBottomButton(text = buttonText) {
                if (currentStep < 3) currentStep++ else onComplete()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            StepIndicator(currentStep = currentStep)

            when (currentStep) {
                1 -> Step1Content(selectedPetId = selectedPetId, onSelectPet = { selectedPetId = it }, onAddPet = onAddPet)
                2 -> Step2Content(
                    currentMonth = currentMonth, onMonthChange = { currentMonth = it },
                    selectedDay = selectedDay, onSelectDay = { selectedDay = it },
                    timeInput = timeInput, onTimeChange = { timeInput = it },
                    selectedTime = selectedTime, onSelectTime = { selectedTime = it }
                )
                3 -> Step3Content(
                    title = titleInput, onTitleChange = { titleInput = it },
                    destination = destination, onDestinationChange = { destination = it },
                    memo = memo, onMemoChange = { if (it.length <= 500) memo = it }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun RequestFlowTopBar(step: Int, onBack: () -> Unit) {
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
            text = "도움 요청하기",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlack,
            modifier = Modifier.align(Alignment.Center)
        )
        Text(
            text = "$step / 3",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp,
            color = Brown700F,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        )
    }
}

// ─── 스텝 인디케이터 ─────────────────────────────────────────────────────────────

private val stepLabels = listOf("반려동물", "일정", "요청 내용")

@Composable
private fun StepIndicator(currentStep: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { idx ->
                val segStep = idx + 1
                val color = if (segStep <= currentStep) Orange500F
                            else Brown900F.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            stepLabels.forEachIndexed { idx, label ->
                val segStep = idx + 1
                Text(
                    text = label,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = if (segStep == currentStep) FontWeight.Bold else FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = if (segStep == currentStep) Orange500F else Brown700F,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── Step 1: 반려동물 선택 ──────────────────────────────────────────────────────

@Composable
private fun Step1Content(selectedPetId: Int?, onSelectPet: (Int) -> Unit, onAddPet: () -> Unit = {}) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("어떤 반려동물과\n함께 이동하나요?")
        Spacer(Modifier.height(8.dp))
        SubText("도움이 필요한 반려동물을 선택해 주세요.")
        Spacer(Modifier.height(24.dp))

        val totalItems = samplePets.size + 1
        val rowCount = (totalItems + 1) / 2

        repeat(rowCount) { rowIdx ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) { colIdx ->
                    val itemIdx = rowIdx * 2 + colIdx
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            itemIdx < samplePets.size -> {
                                val pet = samplePets[itemIdx]
                                PetCard(
                                    pet = pet,
                                    isSelected = pet.id == selectedPetId,
                                    onClick = { onSelectPet(pet.id) }
                                )
                            }
                            itemIdx == samplePets.size -> AddPetCard(onAddPet)
                            else -> Spacer(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            if (rowIdx < rowCount - 1) Spacer(Modifier.height(12.dp))
        }
    }
}

private fun petIconBg(species: String) = when (species) {
    "강아지" -> OrangeSand
    "고양이" -> Color(0xFFFFE4E6)
    else     -> GrayBg
}

private fun petIconTint(species: String) = when (species) {
    "강아지" -> Orange500F
    "고양이" -> Pink500F
    else     -> GrayText
}

@Composable
private fun PetCard(pet: Pet, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Orange500F else BrownBorderF,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(petIconBg(pet.species))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pets),
                    contentDescription = null,
                    tint = petIconTint(pet.species),
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = pet.name,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlack
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${pet.species} · ${pet.age} · ${pet.weight}",
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Brown700F,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Orange500F)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "선택됨",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPetCard(onAddPet: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GrayBg)
            .dashedBorder(color = GrayText.copy(alpha = 0.4f), cornerRadius = 16.dp)
            .clickable { onAddPet() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GrayText.copy(alpha = 0.08f))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "반려동물 추가",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = GrayText
        )
    }
}

// ─── Step 2: 일정 선택 ─────────────────────────────────────────────────────────

@Composable
private fun Step2Content(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
    timeInput: String,
    onTimeChange: (String) -> Unit,
    selectedTime: String?,
    onSelectTime: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("언제 도움이\n필요한가요?")
        Spacer(Modifier.height(24.dp))

        CalendarSection(
            yearMonth = currentMonth,
            onMonthChange = onMonthChange,
            selectedDay = selectedDay,
            onSelectDay = onSelectDay
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "희망 시간",
            fontFamily = PretendardFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 27.sp,
            color = TextBlack
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = timeInput,
            onValueChange = onTimeChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "예: 오전 9:30",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Brown400F
                )
            },
            leadingIcon = {
                Icon(painter = painterResource(R.drawable.ic_schedule), contentDescription = null, tint = Orange500F)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange500F,
                unfocusedBorderColor = BrownBorderF,
                focusedLeadingIconColor = Orange500F,
                unfocusedLeadingIconColor = Orange500F,
                cursorColor = Orange500F,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(14.dp))
        Text(
            text = "빠른 선택",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            color = Brown700F
        )
        Spacer(Modifier.height(8.dp))

        quickTimes.chunked(4).forEach { rowTimes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTimes.forEach { time ->
                    val isSel = time == selectedTime
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Orange100F else Color.White)
                            .border(
                                width = if (isSel) 1.5.dp else 1.dp,
                                color = if (isSel) Orange500F else BrownBorderF,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTime(time) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            color = if (isSel) Orange500F else Brown700F,
                        )
                    }
                }
                repeat(4 - rowTimes.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CalendarSection(
    yearMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
) {
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.let { if (it == DayOfWeek.SUNDAY) 0 else it.value }
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalRows = (startOffset + daysInMonth + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(yearMonth.minusMonths(1)) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달", tint = Brown700F)
            }
            Text(
                text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp,
                color = TextBlack
            )
            IconButton(onClick = { onMonthChange(yearMonth.plusMonths(1)) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달", tint = Brown700F)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEachIndexed { idx, day ->
                val dayColor = when (idx) {
                    0    -> Pink500F
                    6    -> Blue400F
                    else -> Brown900F
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        color = dayColor
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        repeat(totalRows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = if (cellIndex < startOffset || cellIndex >= startOffset + daysInMonth) null
                              else cellIndex - startOffset + 1
                    Box(
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val isSelected = day == selectedDay
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Orange100F else Color.Transparent)
                                    .clickable { onSelectDay(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    fontFamily = PretendardFamily,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = when {
                                        isSelected -> Orange500F
                                        col == 0   -> Pink500F
                                        else       -> Brown900F
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Step 3: 요청 내용 ─────────────────────────────────────────────────────────

@Composable
private fun Step3Content(
    title: String, onTitleChange: (String) -> Unit,
    destination: String, onDestinationChange: (String) -> Unit,
    memo: String, onMemoChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("요청 내용이\n무엇인가요?")
        Spacer(Modifier.height(28.dp))

        RequiredFieldLabel("제목")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "예: 정왕동 실외견 이동 부탁드립니다.",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    color = Brown400F
                )
            },
            singleLine = true,
            colors = flowFieldColors(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(20.dp))

        RequiredFieldLabel("목적지")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = destination,
            onValueChange = onDestinationChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "예: 정왕 동물병원",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    color = Brown400F
                )
            },
            leadingIcon = {
                Icon(painter = painterResource(R.drawable.ic_location_on), contentDescription = null, tint = Orange500F)
            },
            singleLine = true,
            colors = flowFieldColors(leadingAlwaysOrange = true),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(20.dp))

        RequiredFieldLabel("메모")
        Spacer(Modifier.height(6.dp))
        Box {
            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        text = "봉사자에게 전달할 내용을 입력하세요.",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        color = Brown400F
                    )
                },
                maxLines = Int.MAX_VALUE,
                colors = flowFieldColors(),
                shape = RoundedCornerShape(16.dp)
            )
            Text(
                text = "${memo.length} / 500",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                color = Brown400F,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun RequiredFieldLabel(text: String) {
    Text(
        text = buildAnnotatedString {
            append(text)
            withStyle(SpanStyle(color = Orange500F, fontFamily = PretendardFamily)) { append(" *") }
        },
        fontFamily = PretendardFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        color = Brown700F
    )
}

@Composable
private fun flowFieldColors(leadingAlwaysOrange: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Orange500F,
    unfocusedBorderColor = BrownBorderF,
    focusedLeadingIconColor = Orange500F,
    unfocusedLeadingIconColor = if (leadingAlwaysOrange) Orange500F else BrownBorderF,
    cursorColor = Orange500F,
)

// ─── 공통 컴포넌트 ─────────────────────────────────────────────────────────────

@Composable
private fun QuestionText(text: String) {
    Text(
        text = text,
        fontFamily = PretendardFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 33.sp,
        color = TextBlack,
    )
}

@Composable
private fun SubText(text: String) {
    Text(
        text = text,
        fontFamily = PretendardFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        color = Brown700F
    )
}

@Composable
private fun FlowBottomButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brown700F)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = Color.White
            )
        }
    }
}

// ─── 점선 테두리 Modifier ──────────────────────────────────────────────────────

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp = 12.dp, strokeWidth: Dp = 1.5.dp): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
        )
    }

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RequestFlowScreenPreview() {
    SiheungGagaeTheme { RequestFlowScreen() }
}
