package com.tukorea.driver.domain.model

/**
 * WebSocket 연결 상태를 나타내는 enum.
 */
enum class WebSocketConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Disconnecting,
}
