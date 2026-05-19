package com.example.siheunggagae.ui.screen

import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 개인정보 처리 방침 URL — 변경 시 여기만 수정
private const val PRIVACY_URL = "https://backend-production-f6c0.up.railway.app/privacy"

private val TextBlackPv = Color(0xFF1E120A)
private val Brown700Pv  = Color(0xFF8A6E58)
private val Orange500Pv = Color(0xFFF7A35B)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit = {}) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError  by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = { PrivacyTopBar(onBack = onBack) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                if (request?.isForMainFrame == true) hasError = true
                            }
                        }
                        loadUrl(PRIVACY_URL)
                    }
                },
            )

            if (isLoading && !hasError) {
                CircularProgressIndicator(
                    color = Orange500Pv,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (hasError) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    Text(
                        text = "페이지를 불러오지 못했어요",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlackPv,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "인터넷 연결을 확인하고 다시 시도해 주세요.",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        color = Brown700Pv,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyTopBar(onBack: () -> Unit) {
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
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackPv,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "개인정보 처리 방침",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackPv,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PrivacyPolicyScreenPreview() {
    SiheungGagaeTheme { PrivacyPolicyScreen() }
}
