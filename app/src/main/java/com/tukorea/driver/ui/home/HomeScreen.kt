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
import com.tukorea.driver.domain.model.DrivingStatus
import com.tukorea.driver.ui.common.BottomModal
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 운행 상태 표시
        Text(
            text = stringResource(R.string.driver_driving_status_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )

        // 현재 운행 상태 표시
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = when (uiState.drivingStatus) {
                DrivingStatus.Stop -> MaterialTheme.colorScheme.surfaceVariant
                DrivingStatus.Running -> MaterialTheme.colorScheme.primaryContainer
                DrivingStatus.Waiting -> MaterialTheme.colorScheme.secondaryContainer
            },
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = when (uiState.drivingStatus) {
                    DrivingStatus.Stop -> stringResource(R.string.driver_driving_status_stop)
                    DrivingStatus.Running -> stringResource(R.string.driver_driving_status_running)
                    DrivingStatus.Waiting -> stringResource(R.string.driver_driving_status_waiting)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 운행 상태 버튼들
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 운행 버튼
            Button(
                onClick = { viewModel.startDriving() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.drivingStatus != DrivingStatus.Running,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.driver_driving_button_start),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }

            // 대기 버튼
            Button(
                onClick = { viewModel.startWaiting() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.drivingStatus != DrivingStatus.Waiting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.driver_driving_button_waiting),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }

            // 운행 종료 버튼
            OutlinedButton(
                onClick = { viewModel.stopDriving() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.drivingStatus != DrivingStatus.Stop,
            ) {
                Text(
                    text = stringResource(R.string.driver_driving_button_stop),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }
        }

        // 안내 메시지
        Text(
            text = when (uiState.drivingStatus) {
                DrivingStatus.Stop -> stringResource(R.string.driver_driving_status_stop_message)
                DrivingStatus.Running -> stringResource(R.string.driver_driving_status_running_message)
                DrivingStatus.Waiting -> stringResource(R.string.driver_driving_status_waiting_message)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
