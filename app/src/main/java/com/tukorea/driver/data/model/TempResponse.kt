package com.tukorea.driver.data.model

import com.tukorea.driver.domain.model.Temp

data class TempResponse(
    val data: List<Temp> = emptyList()
)

