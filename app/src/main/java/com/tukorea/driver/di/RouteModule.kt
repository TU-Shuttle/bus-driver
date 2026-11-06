package com.tukorea.driver.di

import com.tukorea.driver.data.repository.AssetRouteRepository
import com.tukorea.driver.data.repository.PrefsBusNumberRepository
import com.tukorea.driver.domain.repository.BusNumberRepository
import com.tukorea.driver.domain.repository.RouteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RouteModule {
    @Binds
    abstract fun bindRouteRepository(impl: AssetRouteRepository): RouteRepository

    @Binds
    abstract fun bindBusNumberRepository(impl: PrefsBusNumberRepository): BusNumberRepository
}
