package io.github.dhianapereira.inventario.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemLifecycleRulesTest {
    private val arrival = LocalDate.of(2024, 1, 10)
    private val item = Item("item", "Notebook", "electronics", arrival, 500_000, ItemCurrency.BRL)

    @Test
    fun `update accepts arrival date and dates before closure`() {
        val closure = closure(LocalDate.of(2024, 3, 1))

        assertTrue(isUpdateDateValid(item, closure, arrival))
        assertTrue(isUpdateDateValid(item, closure, LocalDate.of(2024, 2, 1)))
        assertTrue(isUpdateDateValid(item, closure, closure.date))
    }

    @Test
    fun `update rejects dates outside item lifecycle`() {
        val closure = closure(LocalDate.of(2024, 3, 1))

        assertFalse(isUpdateDateValid(item, closure, arrival.minusDays(1)))
        assertFalse(isUpdateDateValid(item, closure, closure.date.plusDays(1)))
    }

    @Test
    fun `open item accepts updates after arrival`() {
        assertTrue(isUpdateDateValid(item, null, LocalDate.of(2030, 1, 1)))
    }

    @Test
    fun `closure cannot precede arrival or latest update`() {
        val updates = listOf(update("first", LocalDate.of(2024, 2, 1), null))

        assertFalse(isClosureDateValid(item, updates, arrival.minusDays(1)))
        assertFalse(isClosureDateValid(item, updates, LocalDate.of(2024, 1, 31)))
        assertTrue(isClosureDateValid(item, updates, LocalDate.of(2024, 2, 1)))
    }

    @Test
    fun `cost calculations include optional expenses and subtract recovered value`() {
        val updates = listOf(
            update("battery", LocalDate.of(2024, 2, 1), 10_000),
            update("cleaning", LocalDate.of(2024, 3, 1), null),
            update("repair", LocalDate.of(2024, 4, 1), 25_000),
        )
        val sold = closure(LocalDate.of(2024, 5, 1), 300_000)

        assertEquals(35_000L, maintenanceCost(updates))
        assertEquals(535_000L, totalCost(item, updates))
        assertEquals(235_000L, netCost(item, updates, sold))
        assertEquals(535_000L, netCost(item, updates, null))
    }

    @Test
    fun `optional text is trimmed and blank becomes null`() {
        assertEquals("Useful note", "  Useful note  ".normalizedOptional())
        assertNull("   ".normalizedOptional())
    }

    @Test
    fun `currency resolves known code and safely falls back to BRL`() {
        assertEquals(ItemCurrency.EUR, ItemCurrency.fromCode("EUR"))
        assertEquals(ItemCurrency.BRL, ItemCurrency.fromCode("INVALID"))
    }

    @Test
    fun `updates on the same day keep registration order`() {
        val date = LocalDate.of(2024, 2, 1)
        val registeredSecond = update("second", date, null).copy(createdAtEpochMillis = 200)
        val registeredFirst = update("first", date, null).copy(createdAtEpochMillis = 100)
        val nextDay = update("next-day", date.plusDays(1), null).copy(createdAtEpochMillis = 50)

        assertEquals(
            listOf(registeredFirst, registeredSecond, nextDay),
            timelineOrdered(listOf(nextDay, registeredSecond, registeredFirst)),
        )
    }

    private fun update(id: String, date: LocalDate, cost: Long?) =
        ItemUpdate(id, item.id, date, "Update", cost)

    private fun closure(date: LocalDate, recovered: Long? = null) =
        ItemClosure(item.id, date, ClosureReason.SOLD, null, recovered)
}
