# android-webcam

Use your Android phone as a **high-quality, low-latency webcam** for meetings and video recording.

## Architecture

```
┌──────────────────────────────┐        Wi-Fi / USB
│   Android App (Kotlin)       │ ──────────────────────► │   Desktop Client (Go)   │
│   Camera2 / CameraX          │   H.264/H.265 over RTP  │   Virtual Camera (V4L2  │
│   Jetpack Compose UI         │   or WebRTC (SRTP)      │   / OBS VirtualCam)     │
└──────────────────────────────┘                         └─────────────────────────┘
```

## Components

| Component | Tech stack | Location |
|---|---|---|
| Android app | Kotlin, Jetpack Compose, CameraX, MediaCodec | `android/` |
| Desktop client | Go, GStreamer bindings, V4L2 loopback (Linux) / DirectShow (Windows) | `desktop/` |

## Goals

- Maximum video quality: up to **4K 30fps** or **1080p 60fps**, H.265 hardware encoding
- Minimum latency: target **< 100 ms** glass-to-glass over local network
- Transport: **WebRTC** (preferred, handles NAT, uses SRTP + RTCP) with fallback to raw RTP over UDP
- Audio: AAC or Opus over the same session

## Quick Start

See [android/README.md](android/README.md) and [desktop/README.md](desktop/README.md).

## License

Apache 2.0
