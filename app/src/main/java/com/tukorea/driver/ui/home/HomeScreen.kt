package com.tukorea.driver.ui.home

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
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

    // 지도 아래 여백 = 시트 기본(MID) 높이
    val fixedHeight = screenHeight * 0.40f
    var naverMap by remember { mutableStateOf<com.naver.maps.map.NaverMap?>(null) }

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
                    onMapInitialized = { map -> naverMap = map },
                )
                if (locationPermissionsState.allPermissionsGranted) {
                    LocateMeButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = fixedHeight + 16.dp),
                        onClick = {
                            naverMap?.locationTrackingMode = com.naver.maps.map.LocationTrackingMode.Follow
                        },
                    )
                }
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
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = stringResource(R.string.driver_riders_title),
            style = MaterialTheme.typography.titleSmall,
            color = bus.textSecondary,
        )
        when {
            riders == null -> Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = stringResource(R.string.driver_riders_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
            riders.isEmpty() -> Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = stringResource(R.string.driver_riders_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
            else -> riders.forEachIndexed { index, departure ->
                if (index > 0) {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
                RidersRow(departure)
            }
        }
    }
}

@Composable
private fun RidersRow(departure: DepartureRiders) {
    val bus = MaterialTheme.busColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
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
            val dropOffs = departure.dropOffs.entries.sortedByDescending { it.value }.map { (stop, count) ->
                stringResource(R.string.driver_drop_off_count, stringResource(stop.nameRes()), count)
            }
            val singleLabel = single?.let { stringResource(R.string.driver_drop_off_only, stringResource(it.nameRes())) }
            Text(
                text = singleLabel ?: dropOffs.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
        }
        Text(
            text = stringResource(R.string.driver_riders_count, departure.riders),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

/** 지도 위 둥근 내 위치 버튼(기본 SDK 버튼 대신). */
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
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusTitle(title = title, dotColor = dotColor, pulsing = status == DrivingStatus.Running)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = bus.textSecondary,
            )
        }

        // 운행 중/대기 중엔 두 버튼을 나란히 둬서 시트가 길어지지 않게 한다.
        when (status) {
            DrivingStatus.Stop ->
                BusButton(
                    text = stringResource(R.string.driver_driving_button_start),
                    onClick = onStart,
                )
            DrivingStatus.Running ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
            DrivingStatus.Waiting ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
