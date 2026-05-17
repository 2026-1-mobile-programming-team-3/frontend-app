package com.example.siheunggagae.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.data.network.ApiErrorType
import com.example.siheunggagae.data.network.ApiResult
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

private val RateLimitBg   = Color(0xFFFFF3E0)
private val RateLimitText = Color(0xFF614B3A)

/**
 * ApiResult.Error를 받아 Snackbar로 에러 메시지를 표시.
 * 429(RateLimited)는 Snackbar 대신 onRateLimited 콜백으로 위임.
 *
 * 사용 예:
 * ```
 * val snackbarHostState = remember { SnackbarHostState() }
 * ApiErrorEffect(result = lastError, snackbarHostState) { isRateLimited = true }
 * Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { ... }
 * ```
 */
@Composable
fun ApiErrorEffect(
    result: ApiResult<*>?,
    snackbarHostState: SnackbarHostState,
    onRateLimited: () -> Unit = {},
) {
    LaunchedEffect(result) {
        if (result is ApiResult.Error) {
            when (result.type) {
                ApiErrorType.RateLimited -> onRateLimited()
                else -> snackbarHostState.showSnackbar(result.message)
            }
        }
    }
}

/**
 * 429 전용 인라인 배너. visible=true일 때 상단에 표시.
 * 타이머로 해제하는 예:
 * ```
 * var rateLimited by remember { mutableStateOf(false) }
 * RateLimitBanner(visible = rateLimited)
 * LaunchedEffect(rateLimited) {
 *     if (rateLimited) { delay(4000); rateLimited = false }
 * }
 * ```
 */
@Composable
fun RateLimitBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RateLimitBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = RateLimitText,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                color = RateLimitText,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RateLimitBannerPreview() {
    SiheungGagaeTheme {
        Box(Modifier.padding(16.dp)) { RateLimitBanner(visible = true) }
    }
}
