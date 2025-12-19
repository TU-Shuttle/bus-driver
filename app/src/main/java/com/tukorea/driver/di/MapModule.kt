package com.tukorea.driver.di

import android.content.Context
import com.tukorea.driver.data.location.LocationDataSource
import com.tukorea.driver.data.repository.MapRepositoryImpl
import com.tukorea.driver.domain.repository.MapRepository
import com.tukorea.driver.domain.usecase.GetCurrentLocationUseCase
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
        @ApplicationContext context: Context
    ): LocationDataSource = LocationDataSource(context)


    @Provides
    @Singleton
    fun provideMapRepository(
        locationDataSource: LocationDataSource
    ): MapRepository = MapRepositoryImpl(locationDataSource)


    @Provides
    @Singleton
    fun provideGetCurrentLocationUseCase(
        repository: MapRepository
    ): GetCurrentLocationUseCase = GetCurrentLocationUseCase(repository)
}

