package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.Gray40
import com.example.siheunggagae.ui.theme.Gray80
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

@Composable
fun StartScreen(
    onLogin: () -> Unit = {},
    onSignup: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "🐾", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "시흥가개",
                fontFamily = PretendardFamily,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 48.sp,
                color = Color(0xFF8A6E58),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "우리 동네 반려동물을 위한 따뜻한 발걸음",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Gray40,
            )
            Spacer(Modifier.height(64.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF8A6E58))
                    .clickable(onClick = onLogin)
                    .padding(vertical = 14.dp),
            ) {
                Text(
                    text = "로그인하기",
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE8D3C2), RoundedCornerShape(16.dp))
                    .background(Color(0xFFFEFEFE))
                    .clickable(onClick = onSignup)
                    .padding(vertical = 14.dp),
            ) {
                Text(
                    text = "회원가입하기",
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    color = Color(0xFF1E120A),
                )
            }
        }

        Text(
            text = "v3.0.0",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Gray80,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartScreenPreview() {
    SiheungGagaeTheme { StartScreen() }
}
