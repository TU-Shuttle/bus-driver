package com.tukorea.driver.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

data class AvailabilityResponse(val available: Boolean?)

interface ApiService {
    /** 이 기기가 이 버스 id(호차 번호)를 쓸 수 있는지. 다른 기사가 운행 중이면 false. */
    @GET("/api/bus/{id}/availability")
    suspend fun checkAvailability(
        @Path("id") id: String,
        @Query("deviceId") deviceId: String,
        @Header("X-Driver-Token") token: String,
    ): AvailabilityResponse
}
