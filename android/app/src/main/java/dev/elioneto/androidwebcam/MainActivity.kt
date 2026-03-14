package dev.elioneto.androidwebcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.elioneto.androidwebcam.ui.AppNavHost
import dev.elioneto.androidwebcam.ui.theme.AndroidWebcamTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidWebcamTheme {
                AppNavHost()
            }
        }
    }
}
