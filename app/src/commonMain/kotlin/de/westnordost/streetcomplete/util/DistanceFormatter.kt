package de.westnordost.streetcomplete.util

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

object DistanceFormatter {
    enum class UnitSystem(val unitInMeters: Double, val limit: Double) {
        METRIC(1.0, 1000.0),
        IMPERIAL_FEET(0.3048, 5280.0),
        IMPERIAL_YARDS(0.9144, 1760.0)
    }

    /**
     * Formats a raw distance in meters to a clean, localized string.
     */
    @Composable
    fun format(meters: Double, system: UnitSystem): String {
        val distanceInUnit = meters / system.unitInMeters
        return if (distanceInUnit >= system.limit) {
            val valueInLargerUnit = distanceInUnit / system.limit
            val roundedLargerUnit = (valueInLargerUnit * 10.0).roundToInt() / 10.0
            val symbol = when (system) {
                UnitSystem.METRIC -> stringResource(Res.string.kilometers_symbol)
                else -> stringResource(Res.string.miles_symbol)
            }
            formatForDisplay(roundedLargerUnit, symbol)
        } else {
            val roundedUnit = distanceInUnit.roundToInt().toDouble()
            val symbol = when (system) {
                UnitSystem.METRIC -> stringResource(Res.string.meters_symbol)
                UnitSystem.IMPERIAL_FEET -> stringResource(Res.string.feet_symbol)
                UnitSystem.IMPERIAL_YARDS -> stringResource(Res.string.yards_symbol)
            }
            formatForDisplay(roundedUnit, symbol)
        }
    }

    private fun formatForDisplay(value: Double, symbol: String): String =
        if (value.toInt().toDouble() == value) "${value.toInt()} $symbol" else "$value $symbol"
}
