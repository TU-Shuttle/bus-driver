package com.tukorea.driver.data.api

import com.tukorea.driver.data.model.TempResponse
import retrofit2.http.GET

interface ApiService {
    @GET("/api/temps") // GET /temp
    suspend fun getTemps(): TempResponse
}
