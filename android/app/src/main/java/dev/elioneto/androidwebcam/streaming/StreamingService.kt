package dev.elioneto.androidwebcam.streaming

import android.content.Context
import androidx.camera.view.PreviewView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Orchestrates CameraX capture → MediaCodec H.265 encoding → WebRTC/RTP transport.
 *
 * Implementation steps:
 * 1. Open CameraX with the configured resolution/fps via Camera2Interop for low-level control.
 * 2. Use ImageAnalysis use-case to grab YUV_420_888 frames.
 * 3. Feed frames into MediaCodec configured as H.265 encoder with the following flags for low latency:
 *    - KEY_LATENCY = 0
 *    - KEY_I_FRAME_INTERVAL = 1 (1-second IDR interval)
 *    - KEY_PRIORITY = 0 (real-time priority)
 *    - KEY_PROFILE = HEVCProfileMain10HDR10 when supported
 * 4. Packetise NAL units into RTP (RFC 7798 for HEVC) or hand off to libwebrtc encoder.
 * 5. WebRTC: use the phone as the offerer, desktop as the answerer;
 *    exchange SDP via a lightweight JSON-over-TCP signaling channel on port 8888.
 * 6. Emit stats (resolution, fps, bitrate, RTT) to statsFlow.
 */
interface StreamingService {
    val statsFlow: Flow<StreamStats>
    suspend fun start(context: Context, previewView: PreviewView?)
    fun stop()
}

data class StreamStats(
    val resolution: String,
    val fps: Int,
    val bitrateMbps: Float,
    val latencyMs: Long
)

class StreamingServiceImpl : StreamingService {
    private val _statsFlow = MutableStateFlow(StreamStats("", 0, 0f, 0))
    override val statsFlow: Flow<StreamStats> = _statsFlow

    override suspend fun start(context: Context, previewView: PreviewView?) {
        // TODO: implement CameraX + MediaCodec + WebRTC pipeline
        // See kdoc above for detailed steps
    }

    override fun stop() {
        // TODO: release encoder, close WebRTC peer connection
    }
}
