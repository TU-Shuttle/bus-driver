package com.tukorea.driver.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tukorea.driver.domain.model.GeoPoint
import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.model.ShuttleRoute
import com.tukorea.driver.domain.repository.RouteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 노선 경로는 학생 앱과 같은 `assets/shuttle_routes.json`(방향 코드 → [[lat, lng], …])을 쓴다.
 * 고른 노선은 SharedPreferences에 남겨, 다음 운행 때 다시 고르지 않아도 되게 한다.
 */
@Singleton
class AssetRouteRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) : RouteRepository {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _selected = MutableStateFlow(
        prefs.getString(KEY_DIRECTION, null)?.let { saved -> ShuttleDirection.entries.firstOrNull { it.name == saved } },
    )
    override val selectedDirection: StateFlow<ShuttleDirection?> = _selected.asStateFlow()

    private val routes: Map<ShuttleDirection, ShuttleRoute> by lazy {
        val type = object : TypeToken<Map<String, List<List<Double>>>>() {}.type
        val raw: Map<String, List<List<Double>>> =
            context.assets.open(FILE).bufferedReader().use { gson.fromJson(it, type) }
        raw.mapNotNull { (code, coordinates) ->
            val direction = ShuttleDirection.entries.firstOrNull { it.name == code } ?: return@mapNotNull null
            direction to ShuttleRoute(direction, coordinates.map { GeoPoint(it[0], it[1]) })
        }.toMap()
    }

    override fun select(direction: ShuttleDirection) {
        _selected.value = direction
        prefs.edit().putString(KEY_DIRECTION, direction.name).apply()
    }

    override fun routeOf(direction: ShuttleDirection): ShuttleRoute? = routes[direction]

    private companion object {
        const val FILE = "shuttle_routes.json"
        const val PREFS = "drive_route"
        const val KEY_DIRECTION = "direction"
    }
}
