package com.tukorea.driver.domain

import com.tukorea.driver.data.model.RideSummaryDto
import com.tukorea.driver.data.repository.toDomain
import com.tukorea.driver.domain.model.DepartureRiders
import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.model.ShuttleStop
import com.tukorea.driver.domain.repository.RideSummaryRepository
import com.tukorea.driver.domain.usecase.GetUpcomingRidersUseCase
import com.tukorea.driver.domain.util.AppResult
import com.tukorea.driver.domain.util.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpcomingRidersTest {

    private val useCase = GetUpcomingRidersUseCase(
        object : RideSummaryRepository {
            override suspend fun today(): AppResult<List<DepartureRiders>> = Result.Success(emptyList())
        },
    )

    private fun riders(minute: Int, count: Int) =
        DepartureRiders(ShuttleDirection.STATION_TO_CAMPUS1, minute, count, mapOf(ShuttleStop.CAMPUS1 to count))

    @Test
    fun `지난_출발편과_0명인_출발편은_빼고_가까운_순으로_3개`() {
        val all = listOf(riders(620, 2), riders(590, 5), riders(600, 0), riders(610, 1), riders(700, 3), riders(800, 1))

        val result = useCase.select(all, nowMinute = 600)

        assertEquals(listOf(610, 620, 700), result.map { it.departMinute })
    }

    @Test
    fun `서버_응답을_변환하고_모르는_코드는_버린다`() {
        val dto = RideSummaryDto(
            direction = "STATION_TO_CAMPUS2_VIA_CAMPUS1",
            departTime = "09:00:00",
            riders = 3,
            dropOffs = mapOf("CAMPUS1_WEST_GATE" to 1L, "CAMPUS2" to 2L, "MARS" to 9L),
        )

        val result = dto.toDomain()!!

        assertEquals(540, result.departMinute)
        assertEquals(mapOf(ShuttleStop.CAMPUS1_WEST_GATE to 1, ShuttleStop.CAMPUS2 to 2), result.dropOffs)
        assertNull(dto.copy(direction = "TO_THE_MOON").toDomain())
    }
}
