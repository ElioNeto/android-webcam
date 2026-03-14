package dev.elioneto.androidwebcam.ui.streaming

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingScreen(
    onStop: () -> Unit,
    viewModel: StreamingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.startStreaming(context) }
    DisposableEffect(Unit) { onDispose { viewModel.stopStreaming() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streaming") },
                actions = {
                    IconButton(onClick = {
                        viewModel.stopStreaming()
                        onStop()
                    }) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera preview
            AndroidView(
                factory = { PreviewView(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                update = { previewView -> viewModel.bindPreview(previewView) }
            )
            // Stats overlay
            uiState.stats?.let { stats ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(label = "Resolution", value = stats.resolution)
                        StatChip(label = "FPS", value = stats.fps.toString())
                        StatChip(label = "Bitrate", value = "${stats.bitrateMbps} Mbps")
                        StatChip(label = "Latency", value = "${stats.latencyMs} ms")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.labelLarge)
        Text(text = label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
