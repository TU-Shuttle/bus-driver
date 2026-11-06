package com.tukorea.driver.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.tukorea.driver.ui.theme.busColors

/**
 * 큰 라운드 버튼(56dp). 누르면 살짝 눌리는 애니메이션이 있다.
 */
@Composable
fun BusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: BusButtonStyle = BusButtonStyle.PRIMARY,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "button_press")

    val colorScheme = MaterialTheme.colorScheme
    val bus = MaterialTheme.busColors
    val colors =
        when (style) {
            BusButtonStyle.PRIMARY ->
                ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    disabledContainerColor = colorScheme.surfaceVariant,
                    disabledContentColor = bus.textTertiary,
                )
            BusButtonStyle.SECONDARY ->
                ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurface,
                    disabledContainerColor = colorScheme.surfaceVariant,
                    disabledContentColor = bus.textTertiary,
                )
            BusButtonStyle.DANGER ->
                ButtonDefaults.buttonColors(
                    containerColor = bus.dangerContainer,
                    contentColor = bus.danger,
                    disabledContainerColor = colorScheme.surfaceVariant,
                    disabledContentColor = bus.textTertiary,
                )
        }

    Button(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        elevation = null,
        interactionSource = interactionSource,
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** 상태 점. pulsing 이면 숨 쉬듯 깜빡여 '진행 중'임을 보여준다. */
@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "dot_pulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MAX_SCALE,
        animationSpec = infiniteRepeatable(tween(PULSE_MS), RepeatMode.Restart),
        label = "dot_scale",
    )

    Box(modifier = modifier.size(20.dp), contentAlignment = Alignment.Center) {
        if (pulsing) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .scale(pulse)
                        // 커질수록 옅어져 최대 크기에서 사라진다.
                        .alpha((PULSE_MAX_SCALE - pulse) / (PULSE_MAX_SCALE - 1f) * PULSE_MAX_ALPHA)
                        .clip(CircleShape)
                        .background(color),
            )
        }
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
        )
    }
}

private const val PULSE_MAX_SCALE = 2.2f
private const val PULSE_MAX_ALPHA = 0.4f
private const val PULSE_MS = 1200
