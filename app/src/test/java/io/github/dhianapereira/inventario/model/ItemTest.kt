package io.github.dhianapereira.inventario.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemTest {
    @Test
    fun `time in use is calculated from arrival date`() {
        val item = Item(
            id = "item-1",
            name = "Headphones",
            typeId = "electronics",
            arrivalDate = LocalDate.of(2022, 3, 12),
            purchasePriceInCents = 189_900,
        )

        val period = item.timeInUse(LocalDate.of(2026, 8, 22))

        assertEquals(4, period.years)
        assertEquals(5, period.months)
        assertEquals(10, period.days)
    }
}
