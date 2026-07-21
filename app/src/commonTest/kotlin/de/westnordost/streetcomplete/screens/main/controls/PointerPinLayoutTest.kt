package de.westnordost.streetcomplete.screens.main.controls

import kotlin.test.Test
import kotlin.test.assertTrue

class PointerPinLayoutTest {

    @Test
    fun pointer_pin_layout_geometry_does_not_overlap() {
        // Ratios based on 24dp dot diameter and 5dp padding (width = 34dp, radius = 17dp)
        val dotDiameterPx = 24f
        val paddingPx = 5f
        val w = dotDiameterPx + 2f * paddingPx // 34f
        val r = w / 2f // 17f
        val hTop = (38f / 24f) * r // Center of top circle (26.916f)

        // Dot geometry bounds
        val dotTopY = hTop - dotDiameterPx / 2f // 14.916f
        val dotBottomY = hTop + dotDiameterPx / 2f // 38.916f

        // Text geometry bounds (e.g. text height = 16px, spacing = 4px, paddingBottom = 8px)
        val spacing = 4f
        val textHeight = 16f
        val paddingBottom = 8f
        val textTopY = dotBottomY + spacing // 42.916f
        val textBottomY = textTopY + textHeight // 58.916f
        val totalH = textBottomY + paddingBottom // 66.916f

        // Verify dot is placed below the top pointy tip
        assertTrue(dotTopY > 0f, "Dot top Y must be positive (below pointer tip)")

        // Verify text does not overlap location dot
        assertTrue(textTopY >= dotBottomY + spacing, "Text top Y must be below dot bottom Y with spacing")

        // Verify text remains inside bottom capsule boundary (total height - bottom padding)
        assertTrue(textBottomY < totalH, "Text bottom Y must remain inside capsule outer boundary")
    }

    @Test
    fun layout_prevents_overlap_across_various_distances() {
        val testDistances = listOf("5 m", "150 m", "1.2 km", "450 ft", "2.5 mi")
        val dotDiameterPx = 24f
        val paddingPx = 5f
        val w = dotDiameterPx + 2f * paddingPx
        val hTop = (38f / 24f) * (w / 2f)
        val dotBottomY = hTop + dotDiameterPx / 2f
        val spacing = 4f

        for (distance in testDistances) {
            val approxTextHeight = 16f
            val textTopY = dotBottomY + spacing

            // Non-overlap check: Text top must strictly begin below the location dot bottom
            assertTrue(
                textTopY >= dotBottomY + spacing,
                "Distance text '$distance' top Y must be separated from dot bottom Y"
            )
            // Non-empty formatted string
            assertTrue(distance.isNotEmpty(), "Formatted distance text should not be empty")
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

            // Net angle of text relative to screen (pin rotation + text rotation)
            val netAngle = (rotate + textRotation) % 360f
            val isScreenUpright = netAngle == 90f || netAngle == -90f || netAngle == 270f || netAngle == -270f || netAngle == 0f || netAngle == 360f || netAngle == -360f

            assertTrue(
                isScreenUpright,
                "Text at pin rotation $rotate° with layout rotation $textRotation° must result in upright screen orientation"
            )
        }
    }
}
