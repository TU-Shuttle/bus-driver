package com.tukorea.driver.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tukorea.driver.R
import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.model.ShuttleStop

@StringRes
fun ShuttleStop.nameRes(): Int = when (this) {
    ShuttleStop.JEONGWANG_STATION -> R.string.stop_jeongwang_station
    ShuttleStop.CAMPUS1 -> R.string.stop_campus1
    ShuttleStop.CAMPUS1_WEST_GATE -> R.string.stop_campus1_west_gate
    ShuttleStop.CAMPUS2 -> R.string.stop_campus2
    ShuttleStop.OIDO_STATION -> R.string.stop_oido_station
}

/** 노선 이름은 "정왕역 → 본교 → 제2캠퍼스"처럼 짧게(서문도 노선 이름에선 본교). */
@Composable
fun ShuttleDirection.label(): String {
    val names = stops.map { stop ->
        stringResource(if (stop == ShuttleStop.CAMPUS1_WEST_GATE) R.string.stop_campus1 else stop.nameRes())
    }
    return names.joinToString(" → ")
}

@Composable
fun distanceLabel(meters: Double): String =
    if (meters < 1_000) {
        stringResource(R.string.distance_meters, (meters / 10).toInt() * 10)
    } else {
        stringResource(R.string.distance_km, meters / 1_000)
    }
