package dev.elioneto.androidwebcam.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resolution
            Text("Resolution", style = MaterialTheme.typography.titleMedium)
            val resolutions = listOf("3840x2160 (4K)", "1920x1080 (1080p)", "1280x720 (720p)")
            resolutions.forEach { res ->
                Row(Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = settings.resolution == res,
                        onClick = { viewModel.setResolution(res) }
                    )
                    Text(res, modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider()

            // FPS
            Text("Frame Rate: ${settings.fps} fps", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.fps.toFloat(),
                onValueChange = { viewModel.setFps(it.toInt()) },
                valueRange = 15f..60f,
                steps = 2
            )

            HorizontalDivider()

            // Codec
            Text("Video Codec", style = MaterialTheme.typography.titleMedium)
            listOf("H.265 (HEVC)", "H.264 (AVC)").forEach { codec ->
                Row(Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = settings.codec == codec,
                        onClick = { viewModel.setCodec(codec) }
                    )
                    Text(codec, modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider()

            // Bitrate
            Text("Bitrate: ${settings.bitrateMbps} Mbps", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.bitrateMbps.toFloat(),
                onValueChange = { viewModel.setBitrate(it.toInt()) },
                valueRange = 2f..50f
            )

            HorizontalDivider()

            // Transport
            Text("Transport", style = MaterialTheme.typography.titleMedium)
            listOf("WebRTC (auto, preferred)", "Raw RTP/UDP (LAN only)").forEach { t ->
                Row(Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = settings.transport == t,
                        onClick = { viewModel.setTransport(t) }
                    )
                    Text(t, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
