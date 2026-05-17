package com.example.siheunggagae.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

private val OverlayBg       = Color.Black.copy(alpha = 0.35f)
private val IndicatorColor  = Color(0xFFF7A35B) // Orange500

/**
 * 전체 화면 로딩 오버레이.
 * Scaffold content 위에 Box로 겹쳐 사용:
 * ```
 * Box {
 *     Scaffold { ... }
 *     LoadingOverlay(isLoading = isLoading)
 * }
 * ```
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = IndicatorColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingOverlayPreview() {
    SiheungGagaeTheme {
        Box(Modifier.fillMaxSize()) {
            LoadingOverlay(isLoading = true)
        }
    }
}
