package com.example.siheunggagae.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.siheunggagae.R

/**
 * Coil AsyncImage 표준 wrapper.
 *
 * - 200ms crossfade
 * - placeholder 가 명시되면 그 drawable, 없으면 ShimmerBox 로 로드 전 표시
 * - error 시 fallback drawable + 회색 배경
 * - URL 이 null/빈 문자열이면 즉시 fallback 표시
 *
 * @param model 이미지 URL (null 가능)
 * @param contentDescription 접근성용 설명
 * @param placeholderRes 로드 중 표시할 drawable (선택)
 * @param errorRes 로드 실패 시 표시할 drawable (선택, 기본 ic_image)
 * @param showShimmer placeholder 가 없으면 ShimmerBox 표시 (기본 true)
 */
@Composable
fun AppAsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    @DrawableRes placeholderRes: Int? = null,
    @DrawableRes errorRes: Int? = R.drawable.ic_image,
    contentScale: ContentScale = ContentScale.Crop,
    showShimmer: Boolean = true,
) {
    val context = LocalContext.current
    if (model.isNullOrBlank()) {
        Box(
            modifier = modifier
                .background(Color(0xFFF4F4F4))
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (errorRes != null) {
                Icon(
                    painter = painterResource(errorRes),
                    contentDescription = null,
                    tint = Color(0xFFC1AEA0),
                )
            }
        }
        return
    }
    Box(modifier = modifier) {
        if (showShimmer && placeholderRes == null) {
            ShimmerBox(modifier = Modifier.fillMaxSize())
        }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .crossfade(200)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            placeholder = placeholderRes?.let { painterResource(it) },
            error = errorRes?.let { painterResource(it) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
