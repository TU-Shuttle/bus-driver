package com.tukorea.driver.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.tukorea.driver.data.network.NetworkMonitor
import com.tukorea.driver.domain.model.MapLocation
import com.tukorea.driver.domain.model.WebSocketConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket을 통해 백엔드 서버와 통신하는 DataSource.
 * 실무 수준의 기능을 제공합니다:
 * - 자동 재연결 (exponential backoff)
 * - 네트워크 상태 감지
 * - 위치 전송 큐 (오프라인 시 저장 후 재전송)
 * - 연결 상태 관리
 */
@Singleton
class WebSocketDataSource
    @Inject
    constructor(
        private val okHttpClient: OkHttpClient,
        private val gson: Gson,
        private val config: WebSocketConfig,
        private val networkMonitor: NetworkMonitor,
    ) {
        private var webSocket: WebSocket? = null
        private val locationQueue = LocationQueue()
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private var reconnectJob: Job? = null
        private var reconnectAttempts = 0
        private var isManualDisconnect = false

        private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
        val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()

        private companion object {
            private const val TAG = "WebSocketDataSource"
            private const val DRIVER_TOKEN_HEADER = "X-Driver-Token"
        }

        init {
            observeNetworkState()
        }

        /**
         * WebSocket 연결을 시작합니다.
         */
        fun connect() {
            if (webSocket != null && _connectionState.value == WebSocketConnectionState.Connected) {
                Log.d(TAG, "WebSocket이 이미 연결되어 있습니다.")
                return
            }

            isManualDisconnect = false
            reconnectAttempts = 0
            attemptConnection()
        }

        /**
         * WebSocket 연결을 종료합니다.
         */
        fun disconnect() {
            Log.d(TAG, "WebSocket 연결 종료 요청")
            isManualDisconnect = true
            reconnectJob?.cancel()
            reconnectJob = null
            webSocket?.close(1000, "정상 종료")
            webSocket = null
            _connectionState.value = WebSocketConnectionState.Disconnected
        }

        /**
         * 위치 정보를 서버로 전송합니다.
         * 연결이 끊겨있으면 큐에 저장하고, 연결되면 자동으로 전송합니다.
         * @param busId 버스/드라이버 ID
         * @param location 위치 정보
         * @param status 운행 상태 ("RUNNING", "WAITING", "STOP")
         */
        fun sendLocation(busId: String, location: MapLocation, status: String, direction: String?) {
            if (_connectionState.value == WebSocketConnectionState.Connected) {
                sendLocationImmediately(busId, location, status, direction)
            } else {
                // 연결이 끊겨있으면 큐에 저장 (전달받은 status를 그대로 저장)
                locationQueue.enqueue(busId, location, status, direction)
                Log.d(
                    TAG,
                    "위치를 큐에 저장: busId=$busId, status=$status, 큐 크기=${locationQueue.size()}",
                )

                // 네트워크가 연결되어 있으면 재연결 시도
                if (networkMonitor.isNetworkAvailable() && !isManualDisconnect) {
                    connect()
                }
            }
        }

        /**
         * 즉시 위치를 전송합니다.
         */
        private fun sendLocationImmediately(busId: String, location: MapLocation, status: String, direction: String?) {
            val webSocket = this.webSocket
            if (webSocket == null) {
                Log.w(TAG, "WebSocket이 null입니다.")
                locationQueue.enqueue(busId, location, status, direction)
                return
            }

            val request = UpdateBusLocationRequest(
                id = busId,
                lat = location.latitude,
                lng = location.longitude,
                accuracy = location.accuracy,
                status = status,
                direction = direction,
            )

            val json = gson.toJson(request)
            val sent = webSocket.send(json)

            if (sent) {
                Log.d(
                    TAG,
                    "위치 전송 성공: busId=$busId, lat=${location.latitude}, lng=${location.longitude}, status=$status",
                )
            } else {
                Log.e(TAG, "위치 전송 실패: WebSocket send 실패")
                locationQueue.enqueue(busId, location, status, direction)
            }
        }

        /**
         * 연결을 시도합니다.
         */
        private fun attemptConnection() {
            if (isManualDisconnect) {
                return
            }

            if (!networkMonitor.isNetworkAvailable()) {
                Log.w(TAG, "네트워크가 연결되지 않아 WebSocket 연결을 시도하지 않습니다.")
                _connectionState.value = WebSocketConnectionState.Disconnected
                return
            }

            if (webSocket != null) {
                Log.d(TAG, "기존 WebSocket 연결을 정리합니다.")
                webSocket?.close(1000, "재연결")
                webSocket = null
            }

            Log.d(TAG, "WebSocket 연결 시도: ${config.url} (시도 횟수: ${reconnectAttempts + 1})")
            _connectionState.value = WebSocketConnectionState.Connecting

            val request =
                Request.Builder()
                    .url(config.url)
                    .apply { if (config.driverToken.isNotBlank()) header(DRIVER_TOKEN_HEADER, config.driverToken) }
                    .build()
            webSocket = okHttpClient.newWebSocket(request, createWebSocketListener())
        }

        /**
         * 재연결을 시도합니다 (exponential backoff).
         */
        private fun scheduleReconnect() {
            if (isManualDisconnect) {
                return
            }

            if (reconnectAttempts >= config.maxReconnectAttempts) {
                Log.e(TAG, "최대 재연결 시도 횟수에 도달했습니다: ${config.maxReconnectAttempts}")
                _connectionState.value = WebSocketConnectionState.Disconnected
                return
            }

            reconnectJob?.cancel()
            reconnectJob =
                scope.launch {
                    val delayMs =
                        (config.initialReconnectDelayMs * Math.pow(config.reconnectBackoffMultiplier, reconnectAttempts.toDouble())).toLong()
                        .coerceAtMost(config.maxReconnectDelayMs)

                    Log.d(TAG, "재연결 대기: ${delayMs}ms 후 재시도")
                    delay(delayMs)

                    if (isActive && !isManualDisconnect) {
                        reconnectAttempts++
                        attemptConnection()
                    }
                }
        }

        /**
         * 큐에 저장된 위치를 모두 전송합니다.
         */
        private fun flushLocationQueue() {
            if (locationQueue.isEmpty()) {
                return
            }

            Log.d(TAG, "큐에 저장된 위치 전송 시작: ${locationQueue.size()}개")
            val queuedLocations = locationQueue.dequeueAll()

            queuedLocations.forEach { queuedLocation ->
                sendLocationImmediately(
                    queuedLocation.busId,
                    queuedLocation.location,
                    queuedLocation.status,
                    queuedLocation.direction,
                )
            }

            Log.d(TAG, "큐에 저장된 위치 전송 완료")
        }

        /**
         * 네트워크 상태를 관찰하고 자동으로 재연결합니다.
         */
        private fun observeNetworkState() {
            networkMonitor.observeNetworkState()
                .onEach { isConnected ->
                    if (isConnected && !isManualDisconnect && _connectionState.value == WebSocketConnectionState.Disconnected) {
                        Log.d(TAG, "네트워크 연결됨: WebSocket 재연결 시도")
                        connect()
                    } else if (!isConnected) {
                        Log.d(TAG, "네트워크 연결 끊김: WebSocket 연결 종료")
                        webSocket?.close(1000, "네트워크 연결 끊김")
                        _connectionState.value = WebSocketConnectionState.Disconnected
                    }
                }
                .launchIn(scope)
        }

        /**
         * WebSocket 리스너를 생성합니다.
         */
        private fun createWebSocketListener(): WebSocketListener =
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "WebSocket 연결 성공")
                    reconnectAttempts = 0
                    reconnectJob?.cancel()
                    reconnectJob = null
                    _connectionState.value = WebSocketConnectionState.Connected

                    // 큐에 저장된 위치 전송
                    flushLocationQueue()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    // 서버는 잘못된 위치 메시지에 {"error":"..."} 로 응답합니다(연결은 유지됨).
                    if (text.contains("\"error\"")) {
                        Log.w(TAG, "서버가 위치 메시지를 거절했습니다: $text")
                    } else {
                        Log.d(TAG, "WebSocket 메시지 수신: $text")
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket 연결 종료 중: code=$code, reason=$reason")
                    _connectionState.value = WebSocketConnectionState.Disconnecting
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket 연결 종료됨: code=$code, reason=$reason")
                    _connectionState.value = WebSocketConnectionState.Disconnected

                    // 정상 종료가 아니고 수동 종료가 아닌 경우 재연결 시도
                    if (code != 1000 && !isManualDisconnect) {
                        scheduleReconnect()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket 연결 실패", t)
                    _connectionState.value = WebSocketConnectionState.Disconnected

                    // 수동 종료가 아닌 경우 재연결 시도
                    if (!isManualDisconnect) {
                        scheduleReconnect()
                    }
                }
            }

        /**
         * 리소스를 정리합니다.
         */
        fun cleanup() {
            disconnect()
            scope.cancel()
            locationQueue.clear()
        }

        /**
         * 서버로 전송할 위치 업데이트 요청 DTO
         */
        private data class UpdateBusLocationRequest(
            val id: String,
            val lat: Double,
            val lng: Double,
            val accuracy: Float,
            val status: String,
            /** 고른 노선이 없으면 null(Gson이 필드를 빼고 보낸다). */
            val direction: String?,
        )
    }
