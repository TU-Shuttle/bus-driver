package com.tukorea.driver.data.model

import com.google.gson.annotations.SerializedName

data class BusLocationResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
    @SerializedName("accuracy")
    val accuracy: Float,
    @SerializedName("updatedAt")
    val updatedAt: Long,
)
