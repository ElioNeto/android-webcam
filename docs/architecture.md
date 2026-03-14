# Architecture & Design Decisions

## Why WebRTC?

WebRTC is chosen as the primary transport because:

- **Built-in congestion control** (TWCC / GCC) adapts bitrate automatically to Wi-Fi jitter
- **DTLS-SRTP** encryption with no extra setup
- **Jitter buffer** on the receiver side absorbs minor network hiccups without adding perceptible latency
- Pion (Go) and libwebrtc (Android) are mature, well-maintained stacks

## Low Latency Checklist

| Layer | Technique |
|---|---|
| Camera capture | CameraX `ImageAnalysis` at target resolution, no double-buffering |
| Encoder | MediaCodec H.265 with `KEY_LATENCY=0`, `KEY_PRIORITY=0` (real-time), IDR every 1 s |
| Network | UDP/SRTP via WebRTC; Wi-Fi 5 GHz or USB tethering preferred |
| Jitter buffer | Pion WebRTC uses a minimal adaptive jitter buffer |
| Decoder | GStreamer `vaapih265dec` (Intel/AMD) or `nvdec` (NVIDIA) for hardware decode |
| OS sink | v4l2loopback with `exclusive_caps=1` avoids format negotiation overhead |

## Video Quality Checklist

| Setting | Recommendation |
|---|---|
| Codec | H.265 Main10 when phone supports it; fall back to H.265 Main or H.264 High |
| Resolution | 1080p60 for meetings; 4K30 for recording |
| Bitrate | 8–15 Mbps for 1080p, 25–50 Mbps for 4K |
| Color space | YUV 4:2:0 (standard) or 4:2:2 if phone supports via Camera2 |
| HDR | HLG if monitor supports it (Android 12+ with Camera2) |

## Signaling Protocol

A minimal TCP JSON channel on port 8888 is used to exchange SDP and ICE candidates:

```
Android (offerer) ──TCP JSON──► Desktop (answerer)
  { type:"offer",  sdp: "..." }
                    ◄── { type:"answer", sdp: "..." }
  { type:"candidate", candidate:"..." }
                    ◄── { type:"candidate", candidate:"..." }
```

After ICE is established, all media flows over SRTP/UDP — the TCP connection is only for signaling.

## Future: USB mode

Android 14+ supports the UVC (USB Video Class) protocol natively when connected via USB.
This gives the lowest possible latency (< 30 ms) and removes the need for network configuration.
Track [AOSP feature](https://source.android.com/docs/core/camera/webcam).
