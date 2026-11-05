package com.tukorea.driver.data.model

data class RideSummaryEnvelope(
    val data: List<RideSummaryDto>?,
)

data class RideSummaryDto(
    val direction: String?,
    val departTime: String?,
    val riders: Long?,
    val dropOffs: Map<String, Long>?,
)
