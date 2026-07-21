package de.westnordost.streetcomplete.util

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
}
