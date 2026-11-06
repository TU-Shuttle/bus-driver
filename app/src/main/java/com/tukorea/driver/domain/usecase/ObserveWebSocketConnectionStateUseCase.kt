package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.WebSocketConnectionState
import com.tukorea.driver.domain.repository.MapRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * WebSocket 연결 상태를 관찰하는 UseCase.
 */
class ObserveWebSocketConnectionStateUseCase
    @Inject
    constructor(
        private val repository: MapRepository,
    ) {
        /**
         * WebSocket 연결 상태를 관찰하는 Flow를 반환합니다.
         */
        operator fun invoke(): StateFlow<WebSocketConnectionState> = repository.observeWebSocketConnectionState()
    }
