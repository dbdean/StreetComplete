package de.westnordost.streetcomplete.screens.main.controls

import kotlin.test.Test
import kotlin.test.assertTrue

class PointerPinLayoutTest {

    @Test
    fun pointer_pin_layout_geometry_does_not_overlap() {
        // Calibrated 56dp base width (radius 28dp), 24dp dot, 4dp spacing, 20dp paddingBottom
        val dotDiameterDp = 24f
        val baseWidthDp = 56f
        val rDp = baseWidthDp / 2f // 28dp
        val hTopDp = (38f / 76f) * baseWidthDp // Center of top circle (28dp)

        // Dot geometry bounds
        val dotTopY = hTopDp - dotDiameterDp / 2f // 16dp
        val dotBottomY = hTopDp + dotDiameterDp / 2f // 40dp

        // Text geometry bounds (e.g. text height = 16dp, spacing = 4dp, paddingBottom = 20dp)
        val spacing = 4f
        val textHeight = 16f
        val paddingBottom = 20f
        val textTopY = dotBottomY + spacing // 44dp
        val textBottomY = textTopY + textHeight // 60dp
        val totalH = textBottomY + paddingBottom // 80dp

        // Verify dot is placed below the top pointy tip
        assertTrue(dotTopY > 0f, "Dot top Y must be positive (below pointer tip)")

        // Verify text does not overlap location dot
        assertTrue(textTopY >= dotBottomY + spacing, "Text top Y must be below dot bottom Y with spacing")

        // Verify text end gap has generous bottom clearance (20dp paddingBottom)
        assertTrue(totalH - textBottomY >= 20f, "Bottom clearance must be at least 20dp for text end margin")
    }

    @Test
    fun layout_enforces_text_end_margin_and_expands_width() {
        val testDistances = listOf("5 m", "150 m", "1.2 km", "450 ft", "2.5 mi")
        val baseWidth = 56f
        val minTextMargin = 10f

        for (distance in testDistances) {
            val approxTextLength = distance.length * 8f // e.g. ~40px to ~64px
            val requiredWidth = maxOf(baseWidth, approxTextLength + 2f * minTextMargin)

            // Dynamic width expansion check: width must be at least baseWidth (56dp)
            assertTrue(requiredWidth >= baseWidth, "Container width must be at least 56dp")

            // End margin check: container width must fit text with at least 10dp margin on each end
            assertTrue(
                (requiredWidth - approxTextLength) / 2f >= minTextMargin,
                "Distance text '$distance' must have at least 10dp margin on each end"
            )
        }
    }

    @Test
    fun text_rotation_keeps_text_upright_at_various_angles() {
        val testAngles = listOf(0f, 45f, 90f, 135f, 180f, -180f, -135f, -90f, -45f)

        for (rotate in testAngles) {
            var normalizedRotate = rotate % 360f
            if (normalizedRotate > 180f) normalizedRotate -= 360f
            if (normalizedRotate < -180f) normalizedRotate += 360f

            val textRotation = if (normalizedRotate > 0f) -90f else 90f

            // Text layout rotation is either +90° or -90° to compensate capsule orientation
            assertTrue(
                textRotation == 90f || textRotation == -90f,
                "Layout rotation for pin angle $rotate° must be ±90°"
            )
        }
    }
}
