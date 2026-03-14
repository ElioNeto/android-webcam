package dev.elioneto.androidwebcam.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class AppSettings(
    val resolution: String = "1920x1080 (1080p)",
    val fps: Int = 30,
    val codec: String = "H.265 (HEVC)",
    val bitrateMbps: Int = 10,
    val transport: String = "WebRTC (auto, preferred)"
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    fun setResolution(res: String) { _settings.value = _settings.value.copy(resolution = res) }
    fun setFps(fps: Int) { _settings.value = _settings.value.copy(fps = fps) }
    fun setCodec(codec: String) { _settings.value = _settings.value.copy(codec = codec) }
    fun setBitrate(mbps: Int) { _settings.value = _settings.value.copy(bitrateMbps = mbps) }
    fun setTransport(t: String) { _settings.value = _settings.value.copy(transport = t) }
}
