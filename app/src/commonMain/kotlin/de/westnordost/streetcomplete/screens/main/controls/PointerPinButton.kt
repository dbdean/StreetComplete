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
    val a = (rotate * PI / 180f).toFloat()
    
    val width = 50.dp
    val height = 90.dp
    val hDiv2w = height.value / (2f * width.value) // 90 / 100 = 0.9f

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(width, height)
            .proportionalAbsoluteOffset(
                x = -0.5f - hDiv2w * sin(a),
                y = -0.5f + 0.5f * cos(a),
            )
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                content()
            }
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .graphicsLayer {
                            rotationZ = textRotation
                        }
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
        val path = Path()
        val w = size.width
        val h = size.height
        val r = w / 2f

        // Elongated capsule with pointy end on the top. Tip at (w/2, 0)
        path.moveTo(w / 2f, 0f)
        path.lineTo(0f, r) // Left shoulder
        path.lineTo(0f, h - r) // Left edge
        path.arcTo(Rect(0f, h - 2 * r, w, h), 180f, -180f, false) // Bottom curve
        path.lineTo(w, r) // Right shoulder
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
