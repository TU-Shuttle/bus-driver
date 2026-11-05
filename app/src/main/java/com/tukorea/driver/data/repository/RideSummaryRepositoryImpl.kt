package com.tukorea.driver.data.repository

import com.tukorea.driver.data.api.ApiService
import com.tukorea.driver.data.model.RideSummaryDto
import com.tukorea.driver.data.util.NetworkErrorHandler
import com.tukorea.driver.domain.model.DepartureRiders
import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.model.ShuttleStop
import com.tukorea.driver.domain.repository.RideSummaryRepository
import com.tukorea.driver.domain.util.AppResult
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideSummaryRepositoryImpl
    @Inject
    constructor(
        private val api: ApiService,
    ) : RideSummaryRepository {
        override suspend fun today(): AppResult<List<DepartureRiders>> =
            NetworkErrorHandler.handleNetworkRequest {
                api.getRideSummary(todayInSeoul()).data.orEmpty().mapNotNull { it.toDomain() }
            }

        private fun todayInSeoul(): String {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
            return "%04d-%02d-%02d".format(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
            )
        }
    }

internal fun RideSummaryDto.toDomain(): DepartureRiders? {
    val direction = ShuttleDirection.entries.firstOrNull { it.name == direction } ?: return null
    val parts = departTime?.split(":") ?: return null
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
    return DepartureRiders(
        direction = direction,
        departMinute = hour * 60 + minute,
        riders = (riders ?: 0L).toInt(),
        dropOffs = dropOffs.orEmpty()
            .mapNotNull { (stop, count) ->
                ShuttleStop.entries.firstOrNull { it.name == stop }?.let { it to count.toInt() }
            }.toMap(),
    )
}
