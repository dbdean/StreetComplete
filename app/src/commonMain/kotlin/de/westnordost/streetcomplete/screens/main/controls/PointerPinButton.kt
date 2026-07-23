package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
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
import de.westnordost.streetcomplete.ui.theme.divider
import de.westnordost.streetcomplete.util.DistanceFormatter
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A view for the pointer pin displayed at the edge of the screen.
 *
 *  Layout geometry:
 *  ```
 *        /\        <-- Pointy tip (top-center anchor)
 *       /  \
 *      /    \      <-- Top circular head (centered at y = w/2)
 *     / (•)  \
 *    |        |    <-- Location icon content (24dp)
 *    | 120 m  |    <-- Distance text (rotated ±90° to stay upright)
 *     \______/     <-- Extended capsule bottom curve
 *  ```
 *
 *  [rotate] rotates the outer pin capsule around its tip anchor. The distance text is rotated
 *  by ±90° relative to the capsule so it remains readable and upright on screen.
 */
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
    val a = (rotate * PI / 180f).toFloat()

    Surface(
        onClick = onClick,
        modifier = modifier
            // Dynamically offsets the pin so its top pointy tip anchors precisely to screen edges
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val w = placeable.width.toFloat()
                val h = placeable.height.toFloat()
                val xFactor = -0.5f - (h / (2f * w)) * sin(a)
                val yFactor = -0.5f + 0.5f * cos(a)
                layout(placeable.width, placeable.height) {
                    placeable.place((xFactor * w).toInt(), (yFactor * h).toInt())
                }
            }
            .graphicsLayer { rotationZ = rotate },
        enabled = enabled,
        shape = pointerPinShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        border = BorderStroke(1.dp, MaterialTheme.colors.divider),
        elevation = 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp, bottom = 22.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(modifier = Modifier.size(24.dp)) { content() }
            if (distanceText != null) {
                // Keep text upright on screen by rotating +90° or -90° depending on pointing angle
                var normalizedRotate = rotate % 360f
                if (normalizedRotate > 180f) normalizedRotate -= 360f
                if (normalizedRotate < -180f) normalizedRotate += 360f
                val textRotation = if (normalizedRotate > 0f) -90f else 90f

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.caption.copy(fontSize = 12.sp),
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.graphicsLayer { rotationZ = textRotation }
                )
            }
        }
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
        // Path base height is 76f with top tip at (38,0) and circle center at (38,38).
        // dh vertically elongates the capsule by extending parallel straight lines at y = 38.
        val baseH = 76f
        val dh = ((h / w) * 76f - baseH).coerceAtLeast(0f)

        val pathString = "M 38,${62f + dh} C 24.745,${62f + dh} 14,${51.255f + dh} 14,${38f + dh} L 14,38 C 14.003,32.6405 15.7995,27.4365 19.1035,23.217 L 38,0 56.914,23.2715 C 60.2005,27.4785 61.99,32.6615 62,38 L 62,${38f + dh} C 62,${51.255f + dh} 51.255,${62f + dh} 38,${62f + dh} Z"
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
