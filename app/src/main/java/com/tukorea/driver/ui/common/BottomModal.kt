package com.tukorea.driver.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tukorea.driver.ui.home.ModalHeight

/**
 * 드래그 가능한 바텀 모달 컴포넌트
 *
 * @param modalHeight 현재 모달 높이 단계
 * @param onModalHeightChange 모달 높이 변경 콜백
 * @param screenHeight 화면 높이
 * @param density Density (픽셀 변환용)
 * @param isVisible 모달 표시 여부
 * @param content 모달 내부 콘텐츠
 */
@Composable
fun BottomModal(
    modalHeight: ModalHeight,
    onModalHeightChange: (ModalHeight) -> Unit,
    screenHeight: Dp,
    density: Density,
    isVisible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    // 3단계 높이 정의
    val lowHeightPx: Float = with(density) { 80.dp.toPx() } // 1단계: 80dp (드래그 바만 보임)
    val midHeightPx: Float = with(density) { (screenHeight * 0.40f).toPx() } // 2단계: 40%
    val highHeightPx: Float = with(density) { (screenHeight * 0.75f).toPx() } // 3단계: 75% (상단 바에 안 가리게)

    // 현재 목표 높이 (단계별)
    val targetHeightPx = when (modalHeight) {
        ModalHeight.LOW -> lowHeightPx
        ModalHeight.MID -> midHeightPx
        ModalHeight.HIGH -> highHeightPx
    }

    // 드래그 상태
    var isDragging by remember { mutableStateOf(false) }

    // 현재 실제 높이 (드래그 중에도 유지)
    var currentHeightPx by remember { mutableStateOf(midHeightPx) }

    // modalHeight가 변경되면 목표 높이 업데이트
    LaunchedEffect(modalHeight) {
        currentHeightPx = when (modalHeight) {
            ModalHeight.LOW -> lowHeightPx
            ModalHeight.MID -> midHeightPx
            ModalHeight.HIGH -> highHeightPx
        }
    }

    // 애니메이션 높이 (드래그 중이 아닐 때만 목표로 이동)
    val animatedHeightPx by animateFloatAsState(
        targetValue = if (isVisible) {
            if (isDragging) currentHeightPx else targetHeightPx
        } else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "modal_height",
        finishedListener = {
            // 애니메이션 완료 후 현재 높이 업데이트
            if (!isDragging) {
                currentHeightPx = targetHeightPx
            }
        },
    )

    // 최종 높이
    val finalHeight = with(density) { animatedHeightPx.toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(finalHeight)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // 드래그 핸들 영역 (터치 영역 확장)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                },
                                onDragEnd = {
                                    // 현재 높이를 기준으로 가장 가까운 단계로 스냅
                                    val newHeight = when {
                                        currentHeightPx < (lowHeightPx + midHeightPx) / 2 -> ModalHeight.LOW
                                        currentHeightPx < (midHeightPx + highHeightPx) / 2 -> ModalHeight.MID
                                        else -> ModalHeight.HIGH
                                    }

                                    // 드래그 종료
                                    isDragging = false

                                    // 항상 새로운 단계로 변경 (같은 단계여도 스냅되도록)
                                    onModalHeightChange(newHeight)
                                },
                                onDragCancel = {
                                    isDragging = false
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    // 드래그량만큼 높이 변경 (아래로 = 양수 = 감소, 위로 = 음수 = 증가)
                                    currentHeightPx =
                                        (currentHeightPx - dragAmount).coerceIn(
                                            lowHeightPx,
                                            highHeightPx,
                                        )
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(2.dp),
                            ),
                    )
                }

                val scrollState = rememberScrollState()

                // 스크롤이 맨 위에 있을 때 아래로 드래그하면 모달을 내리기 위한 nestedScroll
                val nestedScrollConnection = remember {
                    object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                        override fun onPreScroll(
                            available: androidx.compose.ui.geometry.Offset,
                            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource,
                        ): androidx.compose.ui.geometry.Offset {
                            // 스크롤이 맨 위에 있고, 아래로 스크롤하려는 경우 (available.y > 0)
                            if (scrollState.value == 0 && available.y > 0) {
                                // 모달 높이를 줄임
                                currentHeightPx = (currentHeightPx - available.y).coerceIn(
                                    lowHeightPx,
                                    highHeightPx,
                                )
                                isDragging = true
                                return available // 스크롤 이벤트를 소비
                            }
                            return androidx.compose.ui.geometry.Offset.Zero
                        }

                        override fun onPostScroll(
                            consumed: androidx.compose.ui.geometry.Offset,
                            available: androidx.compose.ui.geometry.Offset,
                            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource,
                        ): androidx.compose.ui.geometry.Offset {
                            return androidx.compose.ui.geometry.Offset.Zero
                        }

                        override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                            if (isDragging) {
                                // Fling 종료 시 가장 가까운 단계로 스냅
                                val newHeight = when {
                                    currentHeightPx < (lowHeightPx + midHeightPx) / 2 -> ModalHeight.LOW
                                    currentHeightPx < (midHeightPx + highHeightPx) / 2 -> ModalHeight.MID
                                    else -> ModalHeight.HIGH
                                }
                                isDragging = false
                                onModalHeightChange(newHeight)
                                return available // velocity를 소비
                            }
                            return androidx.compose.ui.unit.Velocity.Zero
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .nestedScroll(nestedScrollConnection)
                        .verticalScroll(scrollState),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    content()
                    // 하단 여백 추가 (콘텐츠가 잘리지 않도록)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
