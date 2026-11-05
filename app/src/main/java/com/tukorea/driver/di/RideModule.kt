package com.tukorea.driver.di

import com.tukorea.driver.data.repository.RideSummaryRepositoryImpl
import com.tukorea.driver.domain.repository.RideSummaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RideModule {
    @Binds
    abstract fun bindRideSummaryRepository(impl: RideSummaryRepositoryImpl): RideSummaryRepository
}
