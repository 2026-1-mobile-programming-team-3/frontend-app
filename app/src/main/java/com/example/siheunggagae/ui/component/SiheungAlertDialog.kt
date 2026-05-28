package com.example.siheunggagae.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily

/**
 * 시흥가개 표준 AlertDialog — 모든 텍스트에 Pretendard 폰트 강제 적용.
 * 기본 Material AlertDialog 는 fontFamily 지정이 누락되면 Roboto 로 fallback.
 */
@Composable
fun SiheungAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmText: String = "확인",
    onConfirm: () -> Unit,
    dismissText: String? = "취소",
    onDismiss: (() -> Unit)? = onDismissRequest,
    confirmColor: Color = Color(0xFF614B3A),
    dismissColor: Color = Color(0xFF8A6E58),
    containerColor: Color = Color.White,
    shape: Shape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = containerColor,
        shape = shape,
        title = {
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E120A),
            )
        },
        text = {
            Text(
                text = text,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Color(0xFF1E120A),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = confirmColor,
                )
            }
        },
        dismissButton = if (dismissText != null && onDismiss != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = dismissText,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = dismissColor,
                    )
                }
            }
        } else null,
    )
}
