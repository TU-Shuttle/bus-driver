package com.tukorea.driver.ui.home

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.tukorea.driver.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tukorea.driver.domain.model.DepartureRiders
import com.tukorea.driver.domain.model.DrivingStatus
import com.tukorea.driver.domain.model.ShuttleStop
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.tukorea.driver.ui.common.BottomModal
import com.tukorea.driver.ui.common.BusButton
import com.tukorea.driver.ui.common.BusButtonStyle
import com.tukorea.driver.ui.common.StatusTitle
import com.tukorea.driver.ui.theme.BusTheme
import com.tukorea.driver.ui.theme.busColors
import com.tukorea.driver.ui.map.MapViewModel
import com.tukorea.driver.ui.map.NaverMapView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mapState by mapViewModel.state.collectAsStateWithLifecycle()

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            mapViewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.modalHeight != ModalHeight.MID) {
            viewModel.updateModalHeight(ModalHeight.MID)
        }

        if (locationPermissionsState.allPermissionsGranted) {
            mapViewModel.loadCurrentLocation()
        }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    // 모달 높이
    val fixedHeight = screenHeight * 0.35f

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                NaverMapView(
                    modifier = Modifier.fillMaxSize(),
                    currentLocation = mapState.currentLocation,
                    isLocationPermissionGranted = locationPermissionsState.allPermissionsGranted,
                    onMapReady = {
                        if (locationPermissionsState.allPermissionsGranted) {
                            mapViewModel.loadCurrentLocation()
                        }
                    },
                    bottomPadding = with(density) {
                        (fixedHeight.toPx()).toInt()
                    },
                    onMapInitialized = { _ ->
                    },
                )
            }

            BottomModal(
                modalHeight = uiState.modalHeight,
                onModalHeightChange = viewModel::updateModalHeight,
                screenHeight = screenHeight,
                density = density,
                isVisible = uiState.isModalVisible,
            ) {
                // 기사용 홈 화면 콘텐츠
                DriverHomeContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DriverHomeContent(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        DriverStatusPanel(
            status = uiState.drivingStatus,
            onStart = viewModel::startDriving,
            onWait = viewModel::startWaiting,
            onStop = viewModel::stopDriving,
        )
        UpcomingRidersPanel(riders = uiState.upcomingRiders)
    }
}

/** 학생들이 '이 셔틀 탈게요'로 남긴 다음 출발편 인원과 하차지. */
@Composable
fun UpcomingRidersPanel(
    riders: List<DepartureRiders>?,
    modifier: Modifier = Modifier,
) {
    val bus = MaterialTheme.busColors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.driver_riders_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        when {
            riders == null -> Text(
                text = stringResource(R.string.driver_riders_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
            riders.isEmpty() -> Text(
                text = stringResource(R.string.driver_riders_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
            else -> riders.forEach { departure -> RidersRow(departure) }
        }
    }
}

@Composable
private fun RidersRow(departure: DepartureRiders) {
    val bus = MaterialTheme.busColors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "%02d:%02d".format(departure.departMinute / 60, departure.departMinute % 60) +
                        "  " + stringResource(
                            R.string.driver_direction,
                            stringResource(departure.direction.origin.nameRes()),
                            stringResource(departure.direction.destination.nameRes()),
                        ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                val dropOffs = departure.dropOffs.entries.sortedByDescending { it.value }.map { (stop, count) ->
                    stringResource(R.string.driver_drop_off_count, stringResource(stop.nameRes()), count)
                }
                Text(
                    text = dropOffs.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = bus.textSecondary,
                )
            }
            Text(
                text = stringResource(R.string.driver_riders_count, departure.riders),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@androidx.annotation.StringRes
private fun ShuttleStop.nameRes(): Int = when (this) {
    ShuttleStop.JEONGWANG_STATION -> R.string.driver_stop_station
    ShuttleStop.CAMPUS1 -> R.string.driver_stop_campus1
    ShuttleStop.CAMPUS1_WEST_GATE -> R.string.driver_stop_west_gate
    ShuttleStop.CAMPUS2 -> R.string.driver_stop_campus2
}

/**
 * 기사 홈 패널. 지금 상태를 크게 보여주고, 상태에 맞는 행동만 버튼으로 보여준다.
 * - 종료: [운행 시작]
 * - 운행 중: [대기로 전환] [운행 종료]
 * - 대기 중: [운행 시작] [운행 종료]
 */
@Composable
fun DriverStatusPanel(
    status: DrivingStatus,
    onStart: () -> Unit,
    onWait: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bus = MaterialTheme.busColors
    val (title, message, dotColor) =
        when (status) {
            DrivingStatus.Stop ->
                Triple(
                    stringResource(R.string.driver_driving_status_stop),
                    stringResource(R.string.driver_driving_status_stop_message),
                    bus.textTertiary,
                )
            DrivingStatus.Running ->
                Triple(
                    stringResource(R.string.driver_driving_status_running),
                    stringResource(R.string.driver_driving_status_running_message),
                    bus.success,
                )
            DrivingStatus.Waiting ->
                Triple(
                    stringResource(R.string.driver_driving_status_waiting),
                    stringResource(R.string.driver_driving_status_waiting_message),
                    bus.warning,
                )
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusTitle(title = title, dotColor = dotColor, pulsing = status == DrivingStatus.Running)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = bus.textSecondary,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (status) {
                DrivingStatus.Stop ->
                    BusButton(
                        text = stringResource(R.string.driver_driving_button_start),
                        onClick = onStart,
                    )
                DrivingStatus.Running -> {
                    BusButton(
                        text = stringResource(R.string.driver_driving_button_waiting),
                        onClick = onWait,
                        style = BusButtonStyle.Secondary,
                    )
                    BusButton(
                        text = stringResource(R.string.driver_driving_button_stop),
                        onClick = onStop,
                        style = BusButtonStyle.Danger,
                    )
                }
                DrivingStatus.Waiting -> {
                    BusButton(
                        text = stringResource(R.string.driver_driving_button_start),
                        onClick = onStart,
                    )
                    BusButton(
                        text = stringResource(R.string.driver_driving_button_stop),
                        onClick = onStop,
                        style = BusButtonStyle.Danger,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "운행 중")
@Composable
private fun DriverStatusPanelRunningPreview() {
    BusTheme {
        Surface { DriverStatusPanel(DrivingStatus.Running, {}, {}, {}, Modifier.padding(20.dp)) }
    }
}

@Preview(showBackground = true, name = "종료 - 다크", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DriverStatusPanelStopDarkPreview() {
    BusTheme(darkTheme = true) {
        Surface { DriverStatusPanel(DrivingStatus.Stop, {}, {}, {}, Modifier.padding(20.dp)) }
    }
}
