# Desktop Client (Go)

Receives the WebRTC/RTP stream from the Android app and exposes it as a virtual camera to the OS.

## Requirements

- Go 1.22+
- Linux: `v4l2loopback` kernel module + GStreamer 1.24+ (for decoding H.265)
- Windows: OBS VirtualCam or NDI Virtual Input as virtual camera sink
- macOS: OBS VirtualCam

## Build

```bash
cd desktop
go build -o android-webcam-desktop ./cmd/server
```

## Run

```bash
# Linux: load v4l2loopback first
sudo modprobe v4l2loopback devices=1 video_nr=10 card_label="AndroidWebcam" exclusive_caps=1

# Start desktop client (will auto-discover phone on LAN)
./android-webcam-desktop --port 8888 --output /dev/video10
```

## Architecture

```
TCP :8888 (signaling) ──► WebRTC Answer ──► GStreamer decode pipeline ──► v4l2loopback / OBS sink
```

## Flags

| Flag | Default | Description |
|---|---|---|
| `--port` | 8888 | TCP signaling port |
| `--output` | `/dev/video10` | v4l2loopback device (Linux) |
| `--codec` | `h265` | Expected codec (h264 or h265) |
| `--width` | 1920 | Expected frame width |
| `--height` | 1080 | Expected frame height |
| `--fps` | 30 | Expected frame rate |
