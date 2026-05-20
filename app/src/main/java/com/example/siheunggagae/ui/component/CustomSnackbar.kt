package com.example.siheunggagae.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily

// 앱에서 공통으로 사용할 스낵바 호스트
@Composable
fun SiheungSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) { data ->
        Snackbar(
            modifier = Modifier.padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = Color(0xFF1E120A), // TextBlackPA
            contentColor = Color.White,
            actionContentColor = Color(0xFFF7A35B) // 🔥 actionColor -> actionContentColor 로 수정됨!
        ) {
            Text(
                text = data.visuals.message,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}