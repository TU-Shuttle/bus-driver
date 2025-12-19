package com.tukorea.driver.di

import com.tukorea.driver.data.api.ApiService
import com.tukorea.driver.data.repository.TempRepositoryImpl
import com.tukorea.driver.domain.repository.TempRepository
import com.tukorea.driver.domain.usecase.GetTempListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TempModule {
    @Provides
    @Singleton
    fun provideTempRepository(api: ApiService): TempRepository = TempRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideGetTempListUseCase(repository: TempRepository): GetTempListUseCase = GetTempListUseCase(repository)
}
