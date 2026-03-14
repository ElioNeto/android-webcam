package dev.elioneto.androidwebcam.ui.streaming

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.elioneto.androidwebcam.streaming.StreamingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamStats(
    val resolution: String,
    val fps: Int,
    val bitrateMbps: Float,
    val latencyMs: Long
)

data class StreamingUiState(
    val isStreaming: Boolean = false,
    val stats: StreamStats? = null,
    val error: String? = null
)

@HiltViewModel
class StreamingViewModel @Inject constructor(
    private val streamingService: StreamingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamingUiState())
    val uiState: StateFlow<StreamingUiState> = _uiState
    private var previewView: PreviewView? = null

    fun bindPreview(view: PreviewView) {
        previewView = view
    }

    fun startStreaming(context: Context) {
        viewModelScope.launch {
            streamingService.start(context, previewView)
            streamingService.statsFlow.collect { stats ->
                _uiState.value = _uiState.value.copy(
                    isStreaming = true,
                    stats = StreamStats(
                        resolution = stats.resolution,
                        fps = stats.fps,
                        bitrateMbps = stats.bitrateMbps,
                        latencyMs = stats.latencyMs
                    )
                )
            }
        }
    }

    fun stopStreaming() {
        streamingService.stop()
        _uiState.value = StreamingUiState()
    }
}
