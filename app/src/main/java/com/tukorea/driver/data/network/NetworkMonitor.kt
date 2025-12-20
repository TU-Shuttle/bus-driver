package com.tukorea.driver.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 네트워크 연결 상태를 모니터링하는 클래스.
 */
@Singleton
class NetworkMonitor
    @Inject
    constructor(
        private val context: Context,
    ) {
        private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        private companion object {
            private const val TAG = "NetworkMonitor"
        }

        /**
         * 현재 네트워크 연결 상태를 확인합니다.
         * @return 네트워크가 연결되어 있으면 true
         */
        fun isNetworkAvailable(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        /**
         * 네트워크 연결 상태 변화를 관찰하는 Flow를 반환합니다.
         * @return 네트워크 연결 상태 (true: 연결됨, false: 연결 안 됨)
         */
        fun observeNetworkState(): Flow<Boolean> = callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.d(TAG, "네트워크 연결됨")
                        trySend(true)
                    }

                    override fun onLost(network: Network) {
                        Log.d(TAG, "네트워크 연결 끊김")
                        trySend(false)
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities,
                    ) {
                        val isConnected =
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        Log.d(TAG, "네트워크 상태 변경: 연결=$isConnected")
                        trySend(isConnected)
                    }
                }

            val request =
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

            connectivityManager.registerNetworkCallback(request, callback)

            // 초기 상태 전송
            trySend(isNetworkAvailable())

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
