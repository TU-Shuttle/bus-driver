package com.tukorea.driver.data.websocket

import com.tukorea.driver.domain.model.MapLocation
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 위치 전송 대기 큐.
 * WebSocket 연결이 끊겼을 때 위치 데이터를 임시 저장하고,
 * 재연결 시 자동으로 전송합니다.
 */
class LocationQueue {
    private val queue = ConcurrentLinkedQueue<QueuedLocation>()

    /**
     * 큐에 위치 데이터를 추가합니다.
     * @param busId 버스/드라이버 ID
     * @param location 위치 정보
     */
    fun enqueue(busId: String, location: MapLocation) {
        queue.offer(QueuedLocation(busId, location))
    }

    /**
     * 큐에서 모든 위치 데이터를 가져옵니다.
     * @return 큐에 저장된 위치 데이터 리스트
     */
    fun dequeueAll(): List<QueuedLocation> {
        val items = mutableListOf<QueuedLocation>()
        while (queue.isNotEmpty()) {
            queue.poll()?.let { items.add(it) }
        }
        return items
    }

    /**
     * 큐의 크기를 반환합니다.
     */
    fun size(): Int = queue.size

    /**
     * 큐가 비어있는지 확인합니다.
     */
    fun isEmpty(): Boolean = queue.isEmpty()

    /**
     * 큐를 비웁니다.
     */
    fun clear() {
        queue.clear()
    }

    /**
     * 큐에 저장된 위치 데이터
     */
    data class QueuedLocation(
        val busId: String,
        val location: MapLocation,
    )
}
