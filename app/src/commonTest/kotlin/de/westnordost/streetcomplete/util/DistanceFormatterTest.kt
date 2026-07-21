package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceFormatterTest {

    @Test
    fun unit_system_from_region() {
        assertEquals(
            DistanceFormatter.UnitSystem.IMPERIAL_FEET,
            DistanceFormatter.UnitSystem.fromRegion("US")
        )
        assertEquals(
            DistanceFormatter.UnitSystem.IMPERIAL_YARDS,
            DistanceFormatter.UnitSystem.fromRegion("GB")
        )
        assertEquals(
            DistanceFormatter.UnitSystem.METRIC,
            DistanceFormatter.UnitSystem.fromRegion("DE")
        )
        assertEquals(
            DistanceFormatter.UnitSystem.METRIC,
            DistanceFormatter.UnitSystem.fromRegion(null)
        )
    }

    @Test
    fun format_length_object() {
        val meterLength = Length.Meters(2.5)
        val feetLength = Length.FeetAndInches(8, 2)

        assertEquals("2.5 m", DistanceFormatter.format(meterLength))
        assertEquals("8′2″", DistanceFormatter.format(feetLength))
    }

    @Test
    fun format_length_unit_overload() {
        assertEquals("3 m", DistanceFormatter.format(3.0, LengthUnit.METER))
        assertEquals("6′7″", DistanceFormatter.format(2.0, LengthUnit.FOOT_AND_INCH))
    }
}
