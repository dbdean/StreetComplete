package de.westnordost.streetcomplete.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

object DistanceFormatter {
    enum class UnitSystem(val unitInMeters: Double, val limit: Double) {
        METRIC(1.0, 1000.0),
        IMPERIAL_FEET(0.3048, 5280.0),
        IMPERIAL_YARDS(0.9144, 1760.0);

        companion object {
            fun fromRegion(region: String?): UnitSystem = when (region) {
                in regionsUsingFeetAndMiles -> IMPERIAL_FEET
                in regionsUsingYardsAndMiles -> IMPERIAL_YARDS
                else -> METRIC
            }

            fun fromMeasure(measure: ScaleBarMeasure): UnitSystem = when (measure) {
                ScaleBarMeasure.FeetAndMiles -> IMPERIAL_FEET
                ScaleBarMeasure.YardsAndMiles -> IMPERIAL_YARDS
                else -> METRIC
            }
        }
    }

    private val regionsUsingFeetAndMiles = setOf(
        "US", "AS", "GU", "MP", "PR", "VI",
        "FM", "MH", "PW",
        "LR",
    )

    private val regionsUsingYardsAndMiles = setOf(
        "GB", "AI", "BM", "FK", "GG", "GI", "GS", "IM", "IO", "JE", "KY", "MS", "PN", "SH", "TC", "VG",
        "BS", "BZ", "GD", "KN", "VC",
        "MM",
    )

    /**
     * Resolves the default UnitSystem for the current device (checking system measurement settings first, falling back to locale region).
     */
    @Composable
    fun defaultUnitSystem(region: String? = Locale.current.region): UnitSystem =
        systemDefaultUnitSystem() ?: UnitSystem.fromRegion(region)

    /**
     * Formats a raw distance in meters to a clean, localized string based on ScaleBarMeasure.
     */
    @Composable
    fun format(meters: Double, measure: ScaleBarMeasure): String =
        format(meters, UnitSystem.fromMeasure(measure))

    /**
     * Formats a raw distance in meters to a clean, localized string using the system default unit system.
     */
    @Composable
    fun format(meters: Double): String =
        format(meters, defaultUnitSystem())

    /**
     * Formats a raw distance in meters to a clean, localized string based on locale region.
     */
    @Composable
    fun format(meters: Double, region: String?): String =
        format(meters, UnitSystem.fromRegion(region))

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

