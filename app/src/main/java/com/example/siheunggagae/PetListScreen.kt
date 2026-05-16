package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러
private val Brown900PL    = Color(0xFF614B3A)
private val Brown700PL    = Color(0xFF8A6E58)
private val Orange500PL   = Color(0xFFF7A35B)
private val Pink500PL     = Color(0xFFF04268)
private val Gray300PL     = Color(0xFFE8E8E8)
private val OrangeSandPL  = Color(0xFFFFEDD4)
private val PinkSurfacePL = Color(0xFFFEE7EC)
private val BackgroundPL  = Color(0xFFFEFEFE)
private val TextBlackPL   = Color(0xFF1E120A)

// ─── 데이터 ────────────────────────────────────────────────────────────────────

private enum class PetKind { DOG, CAT }

private data class PetEntry(
    val id: Int,
    val name: String,
    val kind: PetKind,
    val breed: String,
    val age: Int,
    val weightKg: Double,
)

private val samplePets = listOf(
    PetEntry(1, "파댕이", PetKind.DOG, "말티즈", 3, 3.2),
    PetEntry(2, "냥이",   PetKind.CAT, "코숏",   5, 4.1),
)

private val PetKind.iconBg   get() = if (this == PetKind.DOG) OrangeSandPL  else PinkSurfacePL
private val PetKind.iconTint get() = if (this == PetKind.DOG) Orange500PL   else Pink500PL
private val PetKind.label    get() = if (this == PetKind.DOG) "강아지"       else "고양이"

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun PetListScreen(
    onBack: () -> Unit = {},
    onAddPet: () -> Unit = {},
) {
    Scaffold(
        containerColor = BackgroundPL,
        topBar = { PetListTopBar(onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPet,
                shape = RoundedCornerShape(16.dp),
                containerColor = Brown900PL,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "반려동물 추가", modifier = Modifier.size(24.dp))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "내 반려동물 ${samplePets.size}마리",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                color = Brown700PL,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
            ) {
                samplePets.forEachIndexed { index, pet ->
                    PetRow(pet = pet)
                    if (index < samplePets.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = Gray300PL,
                        )
                    }
                }
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun PetListTopBar(onBack: () -> Unit) {
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
                tint = TextBlackPL,
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            text = "내 반려동물",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackPL,
            modifier = Modifier.align(Alignment.Center),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = "더보기",
                tint = TextBlackPL,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── 동물 리스트 아이템 ─────────────────────────────────────────────────────────

@Composable
private fun PetRow(pet: PetEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(pet.kind.iconBg),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pets),
                contentDescription = null,
                tint = pet.kind.iconTint,
                modifier = Modifier.size(28.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pet.name,
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp,
                color = TextBlackPL,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${pet.kind.label} · ${pet.breed} · ${pet.age}살 · ${pet.weightKg}kg",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700PL,
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = "옵션",
            tint = Brown700PL,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PetListScreenPreview() {
    SiheungGagaeTheme { PetListScreen() }
}
