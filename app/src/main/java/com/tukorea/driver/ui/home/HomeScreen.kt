package com.tukorea.driver.ui.home

import android.Manifest
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.tukorea.driver.R
import com.tukorea.driver.domain.model.DrivingStatus
import com.tukorea.driver.domain.model.RouteProgress
import com.tukorea.driver.domain.model.ShuttleDirection
import com.tukorea.driver.domain.model.ShuttleStop
import com.tukorea.driver.domain.repository.BusNumberRepository
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import com.tukorea.driver.ui.common.distanceLabel
import com.tukorea.driver.ui.common.label
import com.tukorea.driver.ui.common.nameRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.font.FontWeight
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
 * 운행 상태와 위치 전송 상태를 한눈에 보여준다.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
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
    // 호차 번호·노선 시트. 둘 중 빠진 게 있으면 [운행 시작]이 차례로 열고, 다 정하면 바로 운행을 시작한다.
    var picker by remember { mutableStateOf<Picker>(Picker.Closed) }
    fun continueStart(number: Int?, direction: ShuttleDirection?) {
        picker = when {
            number == null -> Picker.Number(thenStart = true)
            direction == null -> Picker.Route(thenStart = true)
            else -> {
                viewModel.startDriving()
                Picker.Closed
            }
        }
    }
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
            route = mapState.route,
            stopCaption = { stringResource(it.nameRes()) },
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
            busNumber = uiState.busNumber,
            stoppedForDuplicate = uiState.stoppedForDuplicate,
            direction = uiState.direction,
            progress = mapState.progress,
            onPickBusNumber = { picker = Picker.Number(thenStart = false) },
            onPickRoute = { picker = Picker.Route(thenStart = false) },
            onStart = { continueStart(uiState.busNumber, uiState.direction) },
            onWait = viewModel::startWaiting,
            onStop = viewModel::stopDriving,
        )
    }

    val current = picker
    if (current != Picker.Closed) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { picker = Picker.Closed },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            when (current) {
                is Picker.Number -> BusNumberSheet(
                    initial = uiState.busNumber,
                    isChecking = uiState.isCheckingNumber,
                    takenNumber = uiState.takenNumber,
                    onConfirm = { number ->
                        viewModel.claimBusNumber(number) { claimed ->
                            if (current.thenStart) continueStart(claimed, uiState.direction) else picker = Picker.Closed
                        }
                    },
                )
                is Picker.Route -> RoutePicker(
                    selected = uiState.direction,
                    onSelect = { direction ->
                        viewModel.selectDirection(direction)
                        if (current.thenStart) continueStart(uiState.busNumber, direction) else picker = Picker.Closed
                    },
                )
                Picker.Closed -> Unit
            }
        }
    }
}

private sealed interface Picker {
    data object Closed : Picker
    data class Number(val thenStart: Boolean) : Picker
    data class Route(val thenStart: Boolean) : Picker
}

@Composable
fun DriverSheet(
    status: DrivingStatus,
    connection: WebSocketConnectionState,
    busNumber: Int?,
    direction: ShuttleDirection?,
    progress: RouteProgress?,
    onPickBusNumber: () -> Unit,
    onPickRoute: () -> Unit,
    onStart: () -> Unit,
    onWait: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    stoppedForDuplicate: Int? = null,
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
                .padding(top = 24.dp, bottom = 16.dp),
        ) {
            DriverStatusPanel(
                status = status,
                connection = connection,
                busNumber = busNumber,
                stoppedForDuplicate = stoppedForDuplicate,
                direction = direction,
                progress = progress,
                onPickBusNumber = onPickBusNumber,
                onPickRoute = onPickRoute,
                onStart = onStart,
                onWait = onWait,
                onStop = onStop,
            )
        }
    }
}

/**
 * 지금 상태를 크게 보여주고, 상태에 맞는 행동만 버튼으로 둔다.
 * - 운행 전: [운행 시작]
 * - 운행 중: [대기로 전환][운행 종료]
 * - 대기 중: [운행 시작][운행 종료]
 * 상태 아래에 운행 노선 한 줄(누르면 변경)과, 운행 중이면 다음 정류장을 둔다.
 */
@Composable
fun DriverStatusPanel(
    status: DrivingStatus,
    connection: WebSocketConnectionState,
    busNumber: Int?,
    direction: ShuttleDirection?,
    progress: RouteProgress?,
    onPickBusNumber: () -> Unit,
    onPickRoute: () -> Unit,
    onStart: () -> Unit,
    onWait: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    stoppedForDuplicate: Int? = null,
) {
    val bus = MaterialTheme.busColors
    val (titleRes, dotColor) = when (status) {
        DrivingStatus.Stop -> R.string.driver_driving_status_stop to bus.textTertiary
        DrivingStatus.Running -> R.string.driver_driving_status_running to bus.success
        DrivingStatus.Waiting -> R.string.driver_driving_status_waiting to bus.warning
    }
    val driving = status != DrivingStatus.Stop

    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = Inset)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(color = dotColor, pulsing = status == DrivingStatus.Running)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(titleRes), style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (driving && progress != null) {
                    progressText(progress, direction)
                } else {
                    stringResource(
                        when (status) {
                            DrivingStatus.Stop -> R.string.driver_driving_status_stop_message
                            DrivingStatus.Running -> R.string.driver_driving_status_running_message
                            DrivingStatus.Waiting -> R.string.driver_driving_status_waiting_message
                        },
                    )
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (progress?.isOffRoute == true && driving) bus.warning else bus.textSecondary,
            )
            if (driving) {
                Spacer(modifier = Modifier.height(8.dp))
                ConnectionLine(connection)
            } else if (stoppedForDuplicate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.bus_number_stopped_duplicate, stoppedForDuplicate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = bus.danger,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SettingRow(
            label = stringResource(R.string.bus_label),
            value = busNumber?.let { stringResource(R.string.bus_number_value, it) },
            placeholder = stringResource(R.string.bus_number_choose),
            // 운행 중에 번호를 바꾸면 이전 번호 버스가 학생 앱에 잠시 남는다.
            onClick = onPickBusNumber.takeIf { !driving },
            lockedHint = stringResource(R.string.bus_number_locked).takeIf { driving },
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Inset),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        SettingRow(
            label = stringResource(R.string.route_label),
            value = direction?.label(),
            placeholder = stringResource(R.string.route_choose),
            onClick = onPickRoute,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.padding(horizontal = Inset)) {
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
}

@Composable
private fun progressText(progress: RouteProgress, direction: ShuttleDirection?): String {
    val next = progress.nextStop
    return when {
        progress.isOffRoute -> stringResource(R.string.route_off)
        next == null -> stringResource(R.string.route_arrived, direction?.destination?.let { stringResource(it.nameRes()) }.orEmpty())
        progress.metersToNextStop != null ->
            stringResource(R.string.route_next_stop_distance, stringResource(next.nameRes()), distanceLabel(progress.metersToNextStop))
        else -> stringResource(R.string.route_next_stop, stringResource(next.nameRes()))
    }
}

/** 평평한 설정 한 줄: 위에 작은 이름, 아래 값, 오른쪽 "변경". [onClick]이 없으면 잠긴 줄([lockedHint] 표시). */
@Composable
private fun SettingRow(
    label: String,
    value: String?,
    placeholder: String,
    onClick: (() -> Unit)?,
    lockedHint: String? = null,
) {
    val bus = MaterialTheme.busColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Inset, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = bus.textSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value ?: placeholder,
                style = MaterialTheme.typography.titleMedium,
                color = if (value == null) bus.textTertiary else MaterialTheme.colorScheme.onSurface,
            )
        }
        if (onClick != null) {
            Text(
                text = stringResource(R.string.route_change),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
            Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = bus.textSecondary)
        } else if (lockedHint != null) {
            Text(text = lockedHint, style = MaterialTheme.typography.bodySmall, color = bus.textTertiary)
        }
    }
}

/** 호차 번호 입력. 숫자만 받고 1~99일 때만 확인을 누를 수 있다. */
@Composable
private fun BusNumberSheet(initial: Int?, isChecking: Boolean, takenNumber: Int?, onConfirm: (Int) -> Unit) {
    val bus = MaterialTheme.busColors
    // 기존 번호는 전체 선택해 두어, 새 번호를 치면 바로 덮어쓰게 한다.
    var field by remember {
        val initialText = initial?.toString().orEmpty()
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(0, initialText.length)))
    }
    val text = field.text
    val number = text.toIntOrNull()?.takeIf { it in BusNumberRepository.VALID }
    val isTaken = number != null && number == takenNumber
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(start = Inset, end = Inset, bottom = 16.dp),
    ) {
        Text(text = stringResource(R.string.bus_number_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.bus_number_body),
            style = MaterialTheme.typography.bodyMedium,
            color = bus.textSecondary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focus),
            value = field,
            onValueChange = { input ->
                val digits = input.text.filter(Char::isDigit).take(2)
                field = input.copy(text = digits, selection = TextRange(digits.length))
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium,
            placeholder = { Text(stringResource(R.string.bus_number_hint), style = MaterialTheme.typography.headlineMedium) },
            suffix = { Text(stringResource(R.string.bus_number_suffix), style = MaterialTheme.typography.titleMedium) },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (!isChecking) number?.let(onConfirm) }),
            isError = isTaken,
            supportingText = if (isTaken) {
                { Text(stringResource(R.string.bus_number_taken, number!!)) }
            } else {
                null
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                errorBorderColor = bus.danger,
                errorSupportingTextColor = bus.danger,
                errorCursorColor = bus.danger,
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        BusButton(
            text = stringResource(R.string.bus_number_confirm),
            onClick = { number?.let(onConfirm) },
            enabled = number != null && !isTaken && !isChecking,
        )
    }
}

@Composable
private fun RoutePicker(selected: ShuttleDirection?, onSelect: (ShuttleDirection) -> Unit) {
    val bus = MaterialTheme.busColors
    Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(horizontal = Inset)) {
            Text(text = stringResource(R.string.route_picker_title), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.route_picker_body),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ShuttleDirection.entries.forEach { direction ->
            val isSelected = direction == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable { onSelect(direction) }
                    .padding(horizontal = Inset, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = direction.label(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else null,
                )
                if (isSelected) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                }
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

@Preview(showBackground = true, name = "운행 중")
@Composable
private fun DriverSheetRunningPreview() {
    BusTheme {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp), contentAlignment = Alignment.BottomCenter) {
            DriverSheet(
                status = DrivingStatus.Running,
                connection = WebSocketConnectionState.Connected,
                busNumber = 3,
                direction = ShuttleDirection.STATION_TO_CAMPUS1,
                progress = RouteProgress(ShuttleStop.CAMPUS1, 820.0, isOffRoute = false),
                onPickBusNumber = {},
                onPickRoute = {},
                onStart = {},
                onWait = {},
                onStop = {},
            )
        }
    }
}
