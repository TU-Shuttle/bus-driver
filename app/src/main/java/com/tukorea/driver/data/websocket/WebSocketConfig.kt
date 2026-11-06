package com.tukorea.driver.data.websocket

/**
 * WebSocket 연결 및 재연결 설정을 관리하는 데이터 클래스.
 */
data class WebSocketConfig(
    /**
     * WebSocket 서버 URL
     * BuildConfig에서 주입받습니다.
     */
    val url: String,
    /**
     * 서버(/ws) 인증 토큰. X-Driver-Token 헤더로 전송합니다.
     * BuildConfig(.env의 DRIVER_TOKEN)에서 주입받습니다.
     */
    val driverToken: String = "",
    /**
     * 재연결 시도 최대 횟수
     */
    val maxReconnectAttempts: Int = 10,
    /**
     * 초기 재연결 지연 시간 (밀리초)
     */
    val initialReconnectDelayMs: Long = 1_000L,
    /**
     * 최대 재연결 지연 시간 (밀리초)
     */
    val maxReconnectDelayMs: Long = 60_000L,
    /**
     * 재연결 지연 시간 배수 (exponential backoff)
     */
    val reconnectBackoffMultiplier: Double = 2.0,
    /**
     * 연결 타임아웃 (밀리초)
     */
    val connectionTimeoutMs: Long = 10_000L,
    /**
     * Ping 주기 (밀리초, 연결 유지용)
     */
    val pingIntervalMs: Long = 30_000L,
)
