package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.model.GeoPoint
import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.model.ShuttleRoute
import com.tukorea.driver.domain.model.ShuttleStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRouteProgressUseCaseTest {
    private val track = TrackRouteProgressUseCase()

    // 정왕역 → 본교 서문 → 제2캠퍼스를 직선으로 잇는 단순 경로
    private val via =
        ShuttleRoute(
            ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1,
            listOf(ShuttleStop.JEONGWANG_STATION.point, ShuttleStop.CAMPUS1_WEST_GATE.point, ShuttleStop.CAMPUS2.point),
        )

    @Test
    fun `출발 직후엔 다음 정류장이 경유지다`() {
        val progress = track(via, ShuttleStop.JEONGWANG_STATION.point)!!
        assertEquals(ShuttleStop.CAMPUS1_WEST_GATE, progress.nextStop)
        assertTrue(progress.metersToNextStop!! > 1_000)
    }

    @Test
    fun `경유지를 지나면 종점이 다음 정류장이다`() {
        val progress = track(via, ShuttleStop.CAMPUS1_WEST_GATE.point)!!
        assertEquals(ShuttleStop.CAMPUS2, progress.nextStop)
    }

    @Test
    fun `종점에 닿으면 다음 정류장이 없다`() {
        val progress = track(via, ShuttleStop.CAMPUS2.point)!!
        assertNull(progress.nextStop)
        assertEquals(false, progress.isOffRoute)
    }

    @Test
    fun `노선에서 멀리 떨어지면 이탈로 본다`() {
        val progress = track(via, GeoPoint(37.40, 126.80))!!
        assertTrue(progress.isOffRoute)
    }
}
