package dev.elioneto.androidwebcam.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.elioneto.androidwebcam.streaming.StreamingService
import dev.elioneto.androidwebcam.streaming.StreamingServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StreamingModule {
    @Binds
    @Singleton
    abstract fun bindStreamingService(impl: StreamingServiceImpl): StreamingService
}
