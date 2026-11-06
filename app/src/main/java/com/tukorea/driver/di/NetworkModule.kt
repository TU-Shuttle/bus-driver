package com.tukorea.driver.di

import android.content.Context
import com.google.gson.Gson
import com.tukorea.driver.data.device.DeviceIdStore
import com.google.gson.GsonBuilder
import com.tukorea.driver.BuildConfig
import com.tukorea.driver.data.api.ApiService
import com.tukorea.driver.data.network.NetworkMonitor
import com.tukorea.driver.data.websocket.WebSocketConfig
import com.tukorea.driver.data.websocket.WebSocketDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context,
    ): NetworkMonitor = NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideWebSocketConfig(): WebSocketConfig = WebSocketConfig(url = BuildConfig.WS_URL, driverToken = BuildConfig.DRIVER_TOKEN)

    @Provides
    @Singleton
    fun provideWebSocketDataSource(
        okHttpClient: OkHttpClient,
        gson: Gson,
        config: WebSocketConfig,
        networkMonitor: NetworkMonitor,
        deviceIdStore: DeviceIdStore,
    ): WebSocketDataSource = WebSocketDataSource(okHttpClient, gson, config, networkMonitor, deviceIdStore)
}
