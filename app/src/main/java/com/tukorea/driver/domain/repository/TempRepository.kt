package com.tukorea.driver.domain.repository

import com.tukorea.driver.domain.model.Temp
import com.tukorea.driver.domain.util.AppResult

interface TempRepository {
    suspend fun getTemps(): AppResult<List<Temp>>
}