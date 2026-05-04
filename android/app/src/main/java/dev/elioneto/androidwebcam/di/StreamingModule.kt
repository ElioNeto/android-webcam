package dev.elioneto.androidwebcam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.elioneto.androidwebcam.signaling.SignalingClient
import dev.elioneto.androidwebcam.streaming.CameraXManager
import dev.elioneto.androidwebcam.streaming.StreamingService
import dev.elioneto.androidwebcam.streaming.StreamingServiceImpl
import dev.elioneto.androidwebcam.webrtc.WebRTCManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StreamingModule {

    @Binds
    @Singleton
    abstract fun bindStreamingService(impl: StreamingServiceImpl): StreamingService

    @Binds
    @Singleton
    abstract fun bindSignalingClient(impl: SignalingClient): SignalingClient

    @Binds
    @Singleton
    abstract fun bindWebRTCManager(impl: WebRTCManager): WebRTCManager

    @Module
    @InstallIn(SingletonComponent::class)
    object CameraModule {

        @Provides
        @Singleton
        fun provideCameraXManager(@ApplicationContext context: Context): CameraXManager {
            return CameraXManager(context)
        }
    }
}
