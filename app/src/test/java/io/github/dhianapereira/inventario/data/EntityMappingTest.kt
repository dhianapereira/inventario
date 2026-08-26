package io.github.dhianapereira.inventario.data

import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureEntity
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateEntity
import io.github.dhianapereira.inventario.model.ClosureReason
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.ItemClosure
import io.github.dhianapereira.inventario.model.ItemCurrency
import io.github.dhianapereira.inventario.model.ItemUpdate
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntityMappingTest {
    @Test
    fun `item mapping preserves currency and normalizes description`() {
        val item = Item(
            "item", "Camera", "electronics", LocalDate.of(2020, 6, 10), 329_900,
            ItemCurrency.USD, "  Main camera  ",
        )

        val entity = ItemEntity.fromModel(item)

        assertEquals("USD", entity.currencyCode)
        assertEquals("Main camera", entity.description)
        assertEquals(item.copy(description = "Main camera"), entity.toModel())
    }

    @Test
    fun `blank item description becomes null`() {
        assertNull(ItemEntity.fromModel(
            Item("item", "Camera", "category", LocalDate.now(), 100, description = "  "),
        ).description)
    }

    @Test
    fun `update mapping preserves optional cost and date`() {
        val update = ItemUpdate("update", "item", LocalDate.of(2025, 4, 5), "Battery", 12_990)
        assertEquals(update, ItemUpdateEntity.fromModel(update).toModel())
    }

    @Test
    fun `closure mapping preserves enum sale value and normalizes note`() {
        val closure = ItemClosure(
            "item", LocalDate.of(2026, 1, 1), ClosureReason.SOLD, "  Sold online  ", 200_000,
        )

        val entity = ItemClosureEntity.fromModel(closure)

        assertEquals("SOLD", entity.reason)
        assertEquals("Sold online", entity.note)
        assertEquals(closure.copy(note = "Sold online"), entity.toModel())
    }
}
