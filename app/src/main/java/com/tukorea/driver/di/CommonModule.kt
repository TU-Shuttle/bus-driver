package com.tukorea.driver.di

import android.content.Context
import com.tukorea.driver.ui.common.ErrorMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @Singleton
    fun provideErrorMapper(
        @ApplicationContext context: Context,
    ): ErrorMapper = ErrorMapper(context)
}
