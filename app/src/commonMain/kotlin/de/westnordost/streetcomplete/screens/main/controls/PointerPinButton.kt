package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.ktx.proportionalAbsoluteOffset
import de.westnordost.streetcomplete.util.DistanceFormatter
import de.westnordost.streetcomplete.ui.ktx.proportionalPadding
import de.westnordost.streetcomplete.ui.theme.divider
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A view for the pointer pin that ought to be displayed at the edge of the screen. The upper left
 *  corner is always the position at which it is pointing to, i.e. it will be drawn outside of
 *  its bounds when pointing to the right.
 *  [rotate] rotates the pin. As opposed to normal rotation, the content always stays upright */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PointerPinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.surface,
    ),
    contentPadding: Dp = 12.dp,
    rotate: Float = 0f,
    distanceInMeters: Double? = null,
    content: @Composable (BoxScope.() -> Unit),
) {
    val distanceText = distanceInMeters?.takeIf { it > 0.0 }?.let { DistanceFormatter.format(it) }
    val pointerPinShape = remember { PointerPinShape() }

    Surface(
        onClick = onClick,
        modifier = modifier
            .pointerPinOffset(rotate)
            .graphicsLayer {
                rotationZ = rotate
            },
        enabled = enabled,
        shape = pointerPinShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        border = BorderStroke(1.dp, MaterialTheme.colors.divider),
        elevation = 4.dp
    ) {
        androidx.compose.ui.layout.Layout(
            content = {
                Box(modifier = Modifier.size(24.dp)) { content() }
                if (distanceText != null) {
                    // Normalize rotate to [-180, 180] to easily detect which side it's pointing to
                    var normalizedRotate = rotate % 360f
                    if (normalizedRotate > 180f) normalizedRotate -= 360f
                    if (normalizedRotate < -180f) normalizedRotate += 360f

                    // Rotate text by 90 or -90 relative to capsule to keep text upright on screen
                    val textRotation = if (normalizedRotate > 0f) -90f else 90f

                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.caption.copy(fontSize = 12.sp),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.rotateLayout(textRotation)
                    )
                }
            }
        ) { measurables, constraints ->
            val dotPlaceable = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
            val textPlaceable = if (measurables.size > 1) {
                measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
            } else null

            val baseWidth = 56.dp.toPx() // 56dp width (radius 28dp), sweet spot between 48dp and 65.7dp
            val textMargin = 10.dp.toPx() // Clean 10dp margin at each end of text string
            val w = if (textPlaceable != null) {
                maxOf(baseWidth, textPlaceable.width + 2f * textMargin)
            } else baseWidth

            val r = w / 2f
            // In 76-unit SVG space, pointy tip is y=0, circle center is y=38 out of 76 (28dp from tip)
            val hTop = (38f / 76f) * w

            val spacing = 4.dp.toPx()
            val paddingBottom = 20.dp.toPx()

            // Calculate exact height dynamically
            val h = if (textPlaceable != null) {
                hTop + dotPlaceable.height / 2f + spacing + textPlaceable.height + paddingBottom
            } else {
                hTop + dotPlaceable.height / 2f + paddingBottom
            }

            layout(w.toInt(), h.toInt()) {
                // Place dot centered exactly at hTop
                val dotX = (w - dotPlaceable.width) / 2f
                val dotY = hTop - dotPlaceable.height / 2f
                dotPlaceable.place(dotX.toInt(), dotY.toInt())

                // Place text below dot
                if (textPlaceable != null) {
                    val textX = (w - textPlaceable.width) / 2f
                    val textY = hTop + dotPlaceable.height / 2f + spacing
                    textPlaceable.place(textX.toInt(), textY.toInt())
                }
            }
        }
    }
}

// A custom modifier that rotates the layout bounds along with the content
private fun Modifier.rotateLayout(rotation: Float) = layout { measurable, constraints ->
    val is90or270 = (rotation % 180f) != 0f
    val rotatedConstraints = if (is90or270) {
        constraints.copy(
            minWidth = constraints.minHeight,
            maxWidth = constraints.maxHeight,
            minHeight = constraints.minWidth,
            maxHeight = constraints.maxWidth
        )
    } else {
        constraints
    }
    val placeable = measurable.measure(rotatedConstraints)
    val width = if (is90or270) placeable.height else placeable.width
    val height = if (is90or270) placeable.width else placeable.height
    layout(width, height) {
        if (is90or270) {
            placeable.placeWithLayer(
                x = (width - placeable.width) / 2,
                y = (height - placeable.height) / 2
            ) {
                rotationZ = rotation
            }
        } else {
            placeable.place(0, 0)
        }
    }
}

// A custom modifier that calculates the correct mathematical offset dynamically
private fun Modifier.pointerPinOffset(rotate: Float) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val w = placeable.width.toFloat()
    val h = placeable.height.toFloat()

    val a = (rotate * PI / 180f).toFloat()
    val hDiv2w = h / (2f * w)

    val xFactor = -0.5f - hDiv2w * sin(a)
    val yFactor = -0.5f + 0.5f * cos(a)

    layout(placeable.width, placeable.height) {
        placeable.place(
            x = (xFactor * w).toInt(),
            y = (yFactor * h).toInt()
        )
    }
}

private class PointerPinShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        // Path base width is 76f. dh is the extended vertical length in SVG coordinate space.
        val baseH = 76f
        val dh = ((h / w) * 76f - baseH).coerceAtLeast(0f)

        val pathString = "M 38,0 L 19.1035,23.217 C 15.7995,27.4365 14.003,32.6405 14,38 L 14,${38f + dh} C 14,${51.255f + dh} 24.745,${62f + dh} 38,${62f + dh} C 51.255,${62f + dh} 62,${51.255f + dh} 62,${38f + dh} L 62,38 C 61.99,32.6615 60.2005,27.4785 56.914,23.2715 Z"
        val p = PathParser().parsePathString(pathString).toNodes().toPath()
        val m = Matrix()
        m.scale(
            x = w / 76f,
            y = h / (baseH + dh)
        )
        p.transform(m)
        return Outline.Generic(p)
    }
}

@Preview
@Composable
private fun PreviewPointerPinButton() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(12000, 0, LinearEasing)),
    )
    PointerPinButton(onClick = {}, rotate = rotation, distanceInMeters = 120.0) {
        Image(
            painter = painterResource(Res.drawable.location_dot_small),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
