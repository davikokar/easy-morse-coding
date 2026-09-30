package com.example.easymorsecoding.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.easymorsecoding.ui.theme.ChassisBevelDark
import com.example.easymorsecoding.ui.theme.ChassisBevelLight
import com.example.easymorsecoding.ui.theme.ChassisGlass
import com.example.easymorsecoding.ui.theme.ChassisRecess
import com.example.easymorsecoding.ui.theme.ChassisRivet

/** Flattens an alpha channel against the enclosure black so lenses read as tinted resin. */
fun Color.overChassis(): Color = Color(
    red = red * alpha,
    green = green * alpha,
    blue = blue * alpha,
    alpha = 1f
)

/**
 * Directional 1px light: highlight on the top/left edges, drop shadow on bottom/right.
 * Reads as a stamped sheet-metal faceplate raised off the chassis.
 */
fun Modifier.chassisBevel(
    light: Color = ChassisBevelLight,
    dark: Color = ChassisBevelDark
): Modifier = drawBehind {
    val w = 1.dp.toPx()
    val half = w / 2f
    drawLine(light, Offset(0f, half), Offset(size.width, half), w)
    drawLine(light, Offset(half, 0f), Offset(half, size.height), w)
    drawLine(dark, Offset(0f, size.height - half), Offset(size.width, size.height - half), w)
    drawLine(dark, Offset(size.width - half, 0f), Offset(size.width - half, size.height), w)
}

/**
 * Punched sheet-metal well: hard outline plus a heavy inner shadow cast from the top lip.
 */
fun Modifier.recessedWell(
    background: Color = ChassisGlass,
    depth: Dp = 6.dp
): Modifier = this
    .background(background)
    .border(1.dp, Color.Black.copy(alpha = 0.9f), RectangleShape)
    .drawWithContent {
        drawContent()
        val d = depth.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent),
                startY = 0f,
                endY = d
            ),
            size = Size(size.width, d)
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
                startX = 0f,
                endX = d
            ),
            size = Size(d, size.height)
        )
    }

/** Localized optical halo projected by lit readouts and indicator lamps. */
fun Modifier.illuminate(color: Color, radius: Dp = 12.dp): Modifier =
    shadow(elevation = radius, shape = RectangleShape, ambientColor = color, spotColor = color)

/** Inset stamped fastener anchoring a panel corner. */
@Composable
fun Rivet(modifier: Modifier = Modifier, size: Dp = 6.dp) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    brush = Brush.verticalGradient(
                        listOf(ChassisRivet, Color.Black)
                    )
                )
            }
    )
}

/**
 * A modular plug-in equipment cassette: chassis surface, hard bevel, corner fasteners.
 */
@Composable
fun ChassisPanel(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentPadding: Dp = 12.dp,
    showRivets: Boolean = true,
    verticalSpacing: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .chassisBevel()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content
        )
        if (showRivets) {
            Rivet(Modifier.align(Alignment.TopStart).padding(5.dp))
            Rivet(Modifier.align(Alignment.TopEnd).padding(5.dp))
        }
    }
}

/** Discrete jewel lens: deep tinted resin when dark, saturated core with bloom when lit. */
@Composable
fun StatusLamp(
    color: Color,
    lit: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp
) {
    val lensColor by animateColorAsState(
        targetValue = if (lit) color else color.copy(alpha = 0.18f).overChassis(),
        animationSpec = tween(150),
        label = "lamp"
    )
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                val r = this.size.minDimension / 2f
                if (lit) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.5f), Color.Transparent),
                            radius = r * 2.2f
                        ),
                        radius = r * 2.2f
                    )
                }
                drawCircle(color = lensColor, radius = r)
            }
    )
}

/** Silk-screened sub-panel heading with its associated status jewel. */
@Composable
fun PanelLabel(
    text: String,
    lampColor: Color,
    modifier: Modifier = Modifier,
    lit: Boolean = true,
    trailing: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusLamp(color = lampColor, lit = lit, size = 9.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = lampColor,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke(this)
    }
}

/**
 * Heavy square-cut momentary switch. Pressing physically seats the cap 2dp downward and
 * extinguishes the underside lip.
 */
@Composable
fun MechanicalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    glow: Boolean = false,
    minHeight: Dp = 40.dp,
    horizontalPadding: Dp = 14.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val seated = pressed && enabled
    val travel by animateDpAsState(if (seated) 3.dp else 0.dp, tween(40), label = "seat")

    val face = if (enabled) containerColor else containerColor.copy(alpha = 0.35f).overChassis()
    val label = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)

    Row(
        modifier = modifier
            // The strip left exposed below the cap reads as the dark drop lip.
            .drawBehind { drawRect(ChassisRecess) }
            .padding(bottom = 3.dp)
            .offset(y = travel)
            .then(if (glow && enabled) Modifier.illuminate(face, 12.dp) else Modifier)
            .background(face)
            .chassisBevel(
                light = if (seated) ChassisBevelDark else Color.White.copy(alpha = 0.15f),
                dark = if (seated) Color.White.copy(alpha = 0.10f) else ChassisBevelDark
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .defaultMinSize(minHeight = minHeight)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides label) { content() }
    }
}

/** Slotted bezel housing a bat-handle toggle with silkscreened dual labels. */
@Composable
fun ToggleBracket(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val track by animateColorAsState(
        targetValue = when {
            !enabled -> Color(0xFF23261F)
            checked -> activeColor.copy(alpha = 0.3f).overChassis()
            else -> Color(0xFF15180F)
        },
        animationSpec = tween(120),
        label = "track"
    )
    val batOffset by animateDpAsState(if (checked) 18.dp else 0.dp, tween(90), label = "bat")

    Box(
        modifier = modifier
            .width(40.dp)
            .height(22.dp)
            .recessedWell(background = track, depth = 4.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .offset(x = batOffset)
                .size(18.dp)
                .then(
                    if (checked && enabled) Modifier.illuminate(activeColor, 8.dp) else Modifier
                )
                .background(
                    Brush.verticalGradient(
                        if (checked && enabled) {
                            listOf(activeColor, activeColor.copy(alpha = 0.6f).overChassis())
                        } else {
                            listOf(Color(0xFF6E7566), Color(0xFF2A2E26))
                        }
                    )
                )
                .chassisBevel(light = Color.White.copy(alpha = 0.2f), dark = Color.Black)
        )
    }
}
