package com.example.siheunggagae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

class   MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        display?.supportedModes
            ?.maxByOrNull { it.refreshRate }
            ?.let { mode ->
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = mode.modeId
                }
            }

        setContent {
            SiheungGagaeTheme {
                AppNavGraph()
            }
        }
    }
}

