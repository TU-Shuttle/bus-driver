package com.tukorea.driver.di

import android.content.Context
import com.tukorea.driver.data.location.LocationDataSource
import com.tukorea.driver.data.repository.MapRepositoryImpl
import com.tukorea.driver.data.websocket.WebSocketDataSource
import com.tukorea.driver.domain.repository.MapRepository
import com.tukorea.driver.domain.usecase.ConnectWebSocketUseCase
import com.tukorea.driver.domain.usecase.DisconnectWebSocketUseCase
import com.tukorea.driver.domain.usecase.GetCurrentLocationUseCase
import com.tukorea.driver.domain.usecase.GetLocationUpdatesUseCase
import com.tukorea.driver.domain.usecase.ObserveWebSocketConnectionStateUseCase
import com.tukorea.driver.domain.usecase.SendLocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {
    @Provides
    @Singleton
    fun provideLocationDataSource(
        @ApplicationContext context: Context,
    ): LocationDataSource = LocationDataSource(context)

    @Provides
    @Singleton
    fun provideMapRepository(
        locationDataSource: LocationDataSource,
        webSocketDataSource: WebSocketDataSource,
    ): MapRepository = MapRepositoryImpl(locationDataSource, webSocketDataSource)

    @Provides
    @Singleton
    fun provideGetCurrentLocationUseCase(repository: MapRepository): GetCurrentLocationUseCase =
        GetCurrentLocationUseCase(repository)

    @Provides
    @Singleton
    fun provideGetLocationUpdatesUseCase(repository: MapRepository): GetLocationUpdatesUseCase =
        GetLocationUpdatesUseCase(repository)

    @Provides
    @Singleton
    fun provideSendLocationUseCase(repository: MapRepository): SendLocationUseCase = SendLocationUseCase(repository)

    @Provides
    @Singleton
    fun provideConnectWebSocketUseCase(repository: MapRepository): ConnectWebSocketUseCase =
        ConnectWebSocketUseCase(repository)

    @Provides
    @Singleton
    fun provideDisconnectWebSocketUseCase(repository: MapRepository): DisconnectWebSocketUseCase =
        DisconnectWebSocketUseCase(repository)

    @Provides
    @Singleton
    fun provideObserveWebSocketConnectionStateUseCase(
        repository: MapRepository,
    ): ObserveWebSocketConnectionStateUseCase = ObserveWebSocketConnectionStateUseCase(repository)
}
