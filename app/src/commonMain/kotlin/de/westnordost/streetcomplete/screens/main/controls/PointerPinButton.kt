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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.ktx.proportionalAbsoluteOffset
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
    contentPadding: Dp = 8.dp,
    rotate: Float = 0f,
    distance: String? = null,
    content: @Composable (BoxScope.() -> Unit),
) {
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
        Column(
            modifier = Modifier
                .proportionalPadding(top = 0.15f, bottom = 0.1f, start = 0.1f, end = 0.1f)
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box { content() }
            if (distance != null) {
                // Normalize rotate to [-180, 180] to easily detect which side it's pointing to
                var normalizedRotate = rotate % 360f
                if (normalizedRotate > 180f) normalizedRotate -= 360f
                if (normalizedRotate < -180f) normalizedRotate += 360f

                // Rotate text by 90 or -90 relative to capsule to keep text upright on screen
                val textRotation = if (normalizedRotate > 0f) -90f else 90f

                Text(
                    text = distance,
                    style = MaterialTheme.typography.caption.copy(fontSize = 12.sp),
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.rotateLayout(textRotation)
                )
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
        val path = Path()
        val w = size.width
        val h = size.height
        val r = w / 2f

        // Original pointer geometry ratios (based on radius R = 24, tip distance = 38):
        val hTop = (38f / 24f) * r
        val dx = -0.78735f * r
        val dy = -0.61596f * r

        // Tip is at (w/2, 0)
        path.moveTo(w / 2f, 0f)
        
        // Line to left shoulder transition point
        path.lineTo(w / 2f + dx, hTop + dy)
        
        // Arc from left shoulder to left vertical edge (0, hTop)
        path.arcTo(
            rect = Rect(w / 2f - r, hTop - r, w / 2f + r, hTop + r),
            startAngleDegrees = 218f,
            sweepAngleDegrees = -38f,
            forceMoveTo = false
        )
        
        // Line down to bottom-left curve start
        path.lineTo(0f, h - r)
        
        // Bottom curve (semi-circle arc)
        path.arcTo(
            rect = Rect(0f, h - 2f * r, w, h),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        
        // Line up to right vertical edge end
        path.lineTo(w, hTop)
        
        // Arc from right vertical edge to right shoulder transition point
        path.arcTo(
            rect = Rect(w / 2f - r, hTop - r, w / 2f + r, hTop + r),
            startAngleDegrees = 0f,
            sweepAngleDegrees = -38f,
            forceMoveTo = false
        )
        
        path.close()

        return Outline.Generic(path)
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
    PointerPinButton(onClick = {}, rotate = rotation, distance = "120 m") {
        Image(
            painter = painterResource(Res.drawable.location_dot_small),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
