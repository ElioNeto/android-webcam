# Android App

Kotlin + Jetpack Compose app that streams the phone camera over WebRTC.

## Requirements

- Android 10+ (API 29+)
- Permissions: `CAMERA`, `RECORD_AUDIO`, `INTERNET`

## Build

```bash
cd android
./gradlew assembleDebug
```

## Key modules

| Module | Purpose |
|---|---|
| `:app` | Main Compose UI, navigation |
| `:camera` | CameraX capture, MediaCodec H.265 encoding |
| `:webrtc` | WebRTC signaling + RTP transport |
| `:settings` | Resolution, FPS, bitrate preferences |
