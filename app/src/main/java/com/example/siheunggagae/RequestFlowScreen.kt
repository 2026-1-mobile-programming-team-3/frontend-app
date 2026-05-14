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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
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
import com.example.siheunggagae.ui.theme.Brown40
import com.example.siheunggagae.ui.theme.Brown80
import com.example.siheunggagae.ui.theme.Brown90
import com.example.siheunggagae.ui.theme.Gray10
import com.example.siheunggagae.ui.theme.Gray40
import com.example.siheunggagae.ui.theme.Gray80
import com.example.siheunggagae.ui.theme.Gray90
import com.example.siheunggagae.ui.theme.Orange40
import com.example.siheunggagae.ui.theme.Orange90
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import java.time.DayOfWeek
import java.time.YearMonth

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
fun RequestFlowScreen(onBack: () -> Unit = {}, onComplete: () -> Unit = {}) {
    var currentStep by remember { mutableStateOf(1) }

    // Step 1 state
    var selectedPetId by remember { mutableStateOf<Int?>(null) }

    // Step 2 state
    var currentMonth by remember { mutableStateOf(YearMonth.of(2026, 10)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var timeInput by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    // Step 3 state
    var title by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    val buttonText = when (currentStep) {
        1 -> "→ 일정 선택"
        2 -> "→ 요청 내용 작성"
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
            HorizontalDivider(color = Gray90)

            when (currentStep) {
                1 -> Step1Content(
                    selectedPetId = selectedPetId,
                    onSelectPet = { selectedPetId = it }
                )
                2 -> Step2Content(
                    currentMonth = currentMonth,
                    onMonthChange = { currentMonth = it },
                    selectedDay = selectedDay,
                    onSelectDay = { selectedDay = it },
                    timeInput = timeInput,
                    onTimeChange = { timeInput = it },
                    selectedTime = selectedTime,
                    onSelectTime = { selectedTime = it }
                )
                3 -> Step3Content(
                    title = title, onTitleChange = { title = it },
                    destination = destination, onDestinationChange = { destination = it },
                    memo = memo, onMemoChange = { if (it.length <= 500) memo = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun RequestFlowTopBar(step: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 14.dp)
    ) {
        Text(
            text = "< 뒤로",
            fontSize = 14.sp,
            color = Gray40,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onBack() }
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Text(
            text = "도움 요청하기",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Gray10,
            modifier = Modifier.align(Alignment.Center)
        )
        Text(
            text = "$step/3",
            fontSize = 14.sp,
            color = Gray40,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(horizontal = 16.dp)
        )
    }
}

// ─── 스텝 인디케이터 ─────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int) {
    val line1Color = if (currentStep > 1) Orange40 else Brown90
    val line2Color = if (currentStep > 2) Orange40 else Brown90

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepCircle(stepNumber = 1, currentStep = currentStep)
            Box(modifier = Modifier.weight(1f).height(2.dp).background(line1Color))
            StepCircle(stepNumber = 2, currentStep = currentStep)
            Box(modifier = Modifier.weight(1f).height(2.dp).background(line2Color))
            StepCircle(stepNumber = 3, currentStep = currentStep)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "반려동물",
                fontSize = 11.sp,
                color = if (currentStep == 1) Orange40 else Brown80
            )
            Text(
                text = "일정",
                fontSize = 11.sp,
                color = if (currentStep == 2) Orange40 else Brown80
            )
            Text(
                text = "요청 내용",
                fontSize = 11.sp,
                color = if (currentStep == 3) Orange40 else Brown80
            )
        }
    }
}

@Composable
private fun StepCircle(stepNumber: Int, currentStep: Int) {
    val isCompleted = stepNumber < currentStep
    val isActive = stepNumber == currentStep

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isCompleted || isActive) Orange40 else Color.Transparent)
            .then(
                if (!isCompleted && !isActive) Modifier.border(2.dp, Brown80, CircleShape)
                else Modifier
            )
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = "$stepNumber",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color.White else Brown80
            )
        }
    }
}

// ─── Step 1: 반려동물 선택 ──────────────────────────────────────────────────────

@Composable
private fun Step1Content(selectedPetId: Int?, onSelectPet: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        QuestionText("어떤 반려동물과\n함께 이동하나요?")
        Spacer(modifier = Modifier.height(8.dp))
        SubText("도움이 필요한 반려동물을 선택해 주세요.")
        Spacer(modifier = Modifier.height(24.dp))

        // 2-column pet card grid (pets + add card)
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
                            itemIdx == samplePets.size -> AddPetCard()
                            else -> Spacer(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            if (rowIdx < rowCount - 1) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PetCard(pet: Pet, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Orange90 else Gray90)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Orange40 else Gray80,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Orange40.copy(alpha = 0.15f) else Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = if (isSelected) Orange40 else Brown40,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = pet.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Gray10
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${pet.species} · ${pet.age} · ${pet.weight}",
                fontSize = 12.sp,
                color = Gray40,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 선택 체크 뱃지
        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Orange40)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "선택됨",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPetCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .dashedBorder(color = Gray80, cornerRadius = 14.dp)
            .clickable { }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Gray90)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Gray40,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "반려동물 추가", fontSize = 13.sp, color = Gray40)
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
        Spacer(modifier = Modifier.height(24.dp))
        QuestionText("언제 도움이\n필요한가요?")
        Spacer(modifier = Modifier.height(24.dp))

        // 달력
        CalendarSection(
            yearMonth = currentMonth,
            onMonthChange = onMonthChange,
            selectedDay = selectedDay,
            onSelectDay = onSelectDay
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Gray90)
        Spacer(modifier = Modifier.height(20.dp))

        // 희망 시간
        Text(text = "희망 시간", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray10)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = timeInput,
            onValueChange = onTimeChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("예: 오전 9:30", color = Gray40, fontSize = 14.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Gray40)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange40,
                unfocusedBorderColor = Gray80,
                focusedLeadingIconColor = Orange40,
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "빠른 선택",
            fontSize = 13.sp,
            color = Gray40,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        quickTimes.chunked(4).forEach { rowTimes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTimes.forEach { time ->
                    val isSelected = time == selectedTime
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Orange90 else Color.White)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Orange40 else Gray80,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTime(time) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            fontSize = 13.sp,
                            color = if (isSelected) Orange40 else Gray10,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                repeat(4 - rowTimes.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
        // 월 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(yearMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달", tint = Gray40)
            }
            Text(
                text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Gray10
            )
            IconButton(onClick = { onMonthChange(yearMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달", tint = Gray40)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 요일 헤더
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEachIndexed { idx, day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (idx == 0) Color(0xFFEF4444) else Gray40
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜 그리드
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Orange40.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { onSelectDay(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    fontSize = 14.sp,
                                    color = when {
                                        isSelected -> Orange40
                                        col == 0 -> Color(0xFFEF4444)
                                        else -> Gray10
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
        Spacer(modifier = Modifier.height(24.dp))
        QuestionText("요청 내용이\n무엇인가요?")
        Spacer(modifier = Modifier.height(28.dp))

        // 제목
        RequiredFieldLabel("제목")
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("예: 정왕동 실외견 이동 부탁드립니다.", color = Gray40, fontSize = 14.sp) },
            singleLine = true,
            colors = flowTextFieldColors(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 목적지
        RequiredFieldLabel("목적지")
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = destination,
            onValueChange = onDestinationChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("예: 정왕 동물병원", color = Gray40, fontSize = 14.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Gray40)
            },
            singleLine = true,
            colors = flowTextFieldColors(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 메모
        RequiredFieldLabel("메모")
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = memo,
            onValueChange = onMemoChange,
            modifier = Modifier.fillMaxWidth().height(160.dp),
            placeholder = { Text("봉사자에게 전달할 내용을 입력하세요.", color = Gray40, fontSize = 14.sp) },
            maxLines = Int.MAX_VALUE,
            supportingText = {
                Text(
                    text = "${memo.length}/500",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 12.sp,
                    color = Gray40
                )
            },
            colors = flowTextFieldColors(),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
private fun RequiredFieldLabel(text: String) {
    Text(
        text = buildAnnotatedString {
            append(text)
            withStyle(SpanStyle(color = Orange40)) { append(" *") }
        },
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Gray10
    )
}

@Composable
private fun flowTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Orange40,
    unfocusedBorderColor = Gray80,
    focusedLeadingIconColor = Orange40,
    cursorColor = Orange40,
)

// ─── 공통 컴포넌트 ─────────────────────────────────────────────────────────────

@Composable
private fun QuestionText(text: String) {
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Gray10,
        lineHeight = 32.sp
    )
}

@Composable
private fun SubText(text: String) {
    Text(text = text, fontSize = 14.sp, color = Gray40)
}

@Composable
private fun FlowBottomButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brown40)
                .clickable { onClick() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ─── 점선 테두리 Modifier ──────────────────────────────────────────────────────

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 1.5.dp,
): Modifier = this.drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )
    )
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RequestFlowScreenPreview() {
    SiheungGagaeTheme { RequestFlowScreen() }
}
