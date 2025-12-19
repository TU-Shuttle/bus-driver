package com.tukorea.driver.data.repository

import com.tukorea.driver.data.api.ApiService
import com.tukorea.driver.data.model.TempResponse
import com.tukorea.driver.data.util.NetworkErrorHandler
import com.tukorea.driver.domain.model.Temp
import com.tukorea.driver.domain.repository.TempRepository
import com.tukorea.driver.domain.util.AppResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempRepositoryImpl @Inject constructor(
    private val api: ApiService
) : TempRepository {
    override suspend fun getTemps(): AppResult<List<Temp>> {
        return NetworkErrorHandler.handleNetworkRequest {
            val response: TempResponse = api.getTemps()
            response.data
        }
    }
}

