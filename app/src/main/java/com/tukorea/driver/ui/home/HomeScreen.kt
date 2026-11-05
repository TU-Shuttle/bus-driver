package com.tukorea.driver.ui.home

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.tukorea.driver.R
import com.tukorea.driver.domain.model.DepartureRiders
import com.tukorea.driver.domain.model.DrivingStatus
import com.tukorea.driver.domain.model.ShuttleStop
import com.tukorea.driver.domain.model.WebSocketConnectionState
import com.tukorea.driver.ui.common.BusButton
import com.tukorea.driver.ui.common.BusButtonStyle
import com.tukorea.driver.ui.common.StatusDot
import com.tukorea.driver.ui.map.MapViewModel
import com.tukorea.driver.ui.map.NaverMapView
import com.tukorea.driver.ui.theme.BusTheme
import com.tukorea.driver.ui.theme.busColors

private val Inset = 24.dp

/**
 * 기사 앱의 유일한 화면. 전체 지도 위에 고정 시트를 두고,
 * 운행 상태·위치 전송 상태·다음 출발편 탑승 예정을 한눈에 보여준다.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DriverHomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mapState by mapViewModel.state.collectAsStateWithLifecycle()

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
    )
    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) locationPermissions.launchMultiplePermissionRequest()
    }
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            mapViewModel.onLocationPermissionGranted()
            mapViewModel.loadCurrentLocation()
        }
    }

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var sheetHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMapView(
            modifier = Modifier.fillMaxSize(),
            currentLocation = mapState.currentLocation,
            isLocationPermissionGranted = locationPermissions.allPermissionsGranted,
            onMapReady = {
                if (locationPermissions.allPermissionsGranted) mapViewModel.loadCurrentLocation()
            },
            bottomPadding = with(density) { sheetHeight.roundToPx() },
            onMapInitialized = { naverMap = it },
        )

        if (locationPermissions.allPermissionsGranted) {
            LocateMeButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = sheetHeight + 16.dp),
                onClick = { naverMap?.locationTrackingMode = LocationTrackingMode.Follow },
            )
        }

        DriverSheet(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { sheetHeight = with(density) { it.height.toDp() } },
            status = uiState.drivingStatus,
            connection = mapState.webSocketConnectionState,
            riders = uiState.upcomingRiders,
            onStart = viewModel::startDriving,
            onWait = viewModel::startWaiting,
            onStop = viewModel::stopDriving,
        )
    }
}

@Composable
fun DriverSheet(
    status: DrivingStatus,
    connection: WebSocketConnectionState,
    riders: List<DepartureRiders>?,
    onStart: () -> Unit,
    onWait: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = Inset, end = Inset, top = 24.dp, bottom = 16.dp),
        ) {
            DriverStatusPanel(status = status, connection = connection, onStart = onStart, onWait = onWait, onStop = onStop)
            HorizontalDivider(
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            UpcomingRidersPanel(riders = riders)
        }
    }
}

/**
 * 지금 상태를 크게 보여주고, 상태에 맞는 행동만 버튼으로 둔다.
 * - 운행 전: [운행 시작]
 * - 운행 중: [대기로 전환][운행 종료]
 * - 대기 중: [운행 시작][운행 종료]
 */
@Composable
fun DriverStatusPanel(
    status: DrivingStatus,
    connection: WebSocketConnectionState,
    onStart: () -> Unit,
    onWait: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bus = MaterialTheme.busColors
    val (titleRes, dotColor) = when (status) {
        DrivingStatus.Stop -> R.string.driver_driving_status_stop to bus.textTertiary
        DrivingStatus.Running -> R.string.driver_driving_status_running to bus.success
        DrivingStatus.Waiting -> R.string.driver_driving_status_waiting to bus.warning
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = dotColor, pulsing = status == DrivingStatus.Running)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                when (status) {
                    DrivingStatus.Stop -> R.string.driver_driving_status_stop_message
                    DrivingStatus.Running -> R.string.driver_driving_status_running_message
                    DrivingStatus.Waiting -> R.string.driver_driving_status_waiting_message
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = bus.textSecondary,
        )
        if (status != DrivingStatus.Stop) {
            Spacer(modifier = Modifier.height(8.dp))
            ConnectionLine(connection)
        }
        Spacer(modifier = Modifier.height(20.dp))

        when (status) {
            DrivingStatus.Stop -> BusButton(text = stringResource(R.string.driver_driving_button_start), onClick = onStart)
            DrivingStatus.Running -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BusButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.driver_driving_button_waiting),
                    onClick = onWait,
                    style = BusButtonStyle.Secondary,
                )
                BusButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.driver_driving_button_stop),
                    onClick = onStop,
                    style = BusButtonStyle.Danger,
                )
            }
            DrivingStatus.Waiting -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BusButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.driver_driving_button_start),
                    onClick = onStart,
                )
                BusButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.driver_driving_button_stop),
                    onClick = onStop,
                    style = BusButtonStyle.Danger,
                )
            }
        }
    }
}

/** 위치가 실제로 서버에 가고 있는지. 끊기면 기사가 바로 알 수 있게 빨간 점으로 보여준다. */
@Composable
private fun ConnectionLine(connection: WebSocketConnectionState) {
    val bus = MaterialTheme.busColors
    val (textRes, color) = when (connection) {
        WebSocketConnectionState.Connected -> R.string.driver_link_connected to bus.success
        WebSocketConnectionState.Connecting -> R.string.driver_link_connecting to bus.warning
        WebSocketConnectionState.Disconnected,
        WebSocketConnectionState.Disconnecting,
        -> R.string.driver_link_disconnected to bus.danger
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = color) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(textRes), style = MaterialTheme.typography.bodySmall, color = bus.textSecondary)
    }
}

/** 학생들이 '이 셔틀 탈게요'로 남긴 다음 출발편 인원과 하차지. */
@Composable
fun UpcomingRidersPanel(riders: List<DepartureRiders>?, modifier: Modifier = Modifier) {
    val bus = MaterialTheme.busColors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.driver_riders_title),
            style = MaterialTheme.typography.titleSmall,
            color = bus.textSecondary,
        )
        when {
            riders == null -> EmptyLine(R.string.driver_riders_unavailable)
            riders.isEmpty() -> EmptyLine(R.string.driver_riders_empty)
            else -> riders.forEachIndexed { index, departure ->
                if (index > 0) HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                RidersRow(departure)
            }
        }
    }
}

@Composable
private fun EmptyLine(@StringRes textRes: Int) {
    Text(
        modifier = Modifier.padding(vertical = 12.dp),
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.busColors.textSecondary,
    )
}

@Composable
private fun RidersRow(departure: DepartureRiders) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row {
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = "%02d:%02d".format(departure.departMinute / 60, departure.departMinute % 60),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = stringResource(
                        R.string.driver_direction,
                        stringResource(departure.direction.origin.nameRes()),
                        stringResource(departure.direction.destination.nameRes()),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            // 하차지가 하나면 오른쪽 인원과 겹치므로 "본교 하차"만 쓴다.
            val single = departure.dropOffs.keys.singleOrNull()
            val breakdown = departure.dropOffs.entries.sortedByDescending { it.value }.map { (stop, count) ->
                stringResource(R.string.driver_drop_off_count, stringResource(stop.nameRes()), count)
            }
            val singleLabel = single?.let { stringResource(R.string.driver_drop_off_only, stringResource(it.nameRes())) }
            Text(
                text = singleLabel ?: breakdown.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.busColors.textSecondary,
            )
        }
        Text(
            text = stringResource(R.string.driver_riders_count, departure.riders),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun LocateMeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(48.dp),
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = stringResource(R.string.driver_locate_me),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@StringRes
private fun ShuttleStop.nameRes(): Int = when (this) {
    ShuttleStop.JEONGWANG_STATION -> R.string.driver_stop_station
    ShuttleStop.CAMPUS1 -> R.string.driver_stop_campus1
    ShuttleStop.CAMPUS1_WEST_GATE -> R.string.driver_stop_west_gate
    ShuttleStop.CAMPUS2 -> R.string.driver_stop_campus2
}

@Preview(showBackground = true, name = "운행 중")
@Composable
private fun DriverSheetRunningPreview() {
    BusTheme {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp), contentAlignment = Alignment.BottomCenter) {
            DriverSheet(
                status = DrivingStatus.Running,
                connection = WebSocketConnectionState.Connected,
                riders = emptyList(),
                onStart = {},
                onWait = {},
                onStop = {},
            )
        }
    }
}
