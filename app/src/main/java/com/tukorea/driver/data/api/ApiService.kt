package com.tukorea.driver.data.api

import com.tukorea.driver.data.model.BusLocationResponse
import com.tukorea.driver.data.model.RideSummaryEnvelope
import com.tukorea.driver.data.model.TempResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/temps") // GET /temp
    suspend fun getTemps(): TempResponse

    /**
     * 특정 버스의 최신 위치를 조회합니다.
     * @param id 버스/드라이버 ID
     * @return 버스 위치 정보 (없으면 null)
     */
    @GET("/api/bus/{id}/location")
    suspend fun getBusLocation(@Path("id") id: String): BusLocationResponse?

    /** 날짜별 출발편 탑승 예정 인원(학생 앱의 '이 셔틀 탈게요'). */
    @GET("/api/rides/summary")
    suspend fun getRideSummary(@Query("date") date: String): RideSummaryEnvelope
}
