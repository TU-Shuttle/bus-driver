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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tukorea.driver.ui.theme.busColors

/** 버튼 종류. Primary는 화면당 하나만 쓴다. */
enum class BusButtonStyle { Primary, Secondary, Danger }

/**
 * 큰 라운드 버튼(56dp). 누르면 살짝 눌리는 애니메이션이 있다.
 */
@Composable
fun BusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: BusButtonStyle = BusButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "button_press")

    val colorScheme = MaterialTheme.colorScheme
    val bus = MaterialTheme.busColors
    val colors =
        when (style) {
            BusButtonStyle.Primary ->
                ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    disabledContainerColor = colorScheme.surfaceVariant,
                    disabledContentColor = bus.textTertiary,
                )
            BusButtonStyle.Secondary ->
                ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurface,
                    disabledContainerColor = colorScheme.surfaceVariant,
                    disabledContentColor = bus.textTertiary,
                )
            BusButtonStyle.Danger ->
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

/** 행 안의 작은 보조 버튼(36dp). 위험한 동작이 아니면 빨간색을 쓰지 않는다. */
@Composable
fun SmallSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        modifier = modifier.height(36.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.busColors.textTertiary,
            )
        }
    }
}

enum class ChipTone { Neutral, Primary, Success, Warning, Danger }

/** 작은 상태 표시 칩. */
@Composable
fun StatusChip(
    text: String,
    tone: ChipTone,
    modifier: Modifier = Modifier,
) {
    val bus = MaterialTheme.busColors
    val scheme = MaterialTheme.colorScheme
    val (container, content) =
        when (tone) {
            ChipTone.Neutral -> scheme.surfaceVariant to bus.textSecondary
            ChipTone.Primary -> scheme.primaryContainer to scheme.primary
            ChipTone.Success -> bus.successContainer to bus.success
            ChipTone.Warning -> bus.warningContainer to bus.warning
            ChipTone.Danger -> bus.dangerContainer to bus.danger
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
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
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "dot_scale",
    )

    Box(modifier = modifier.size(20.dp), contentAlignment = Alignment.Center) {
        if (pulsing) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .scale(pulse)
                        .alpha((2.2f - pulse) / 1.2f * 0.4f)
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

/** 제목 옆에 상태 점을 붙이는 한 줄. */
@Composable
fun StatusTitle(
    title: String,
    dotColor: Color,
    pulsing: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusDot(color = dotColor, pulsing = pulsing)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
