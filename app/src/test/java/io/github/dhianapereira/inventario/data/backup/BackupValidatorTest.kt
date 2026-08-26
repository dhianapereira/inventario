package io.github.dhianapereira.inventario.data.backup

import io.github.dhianapereira.inventario.data.category.CategoryEntity
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureEntity
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateEntity
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupValidatorTest {
    private val category = CategoryEntity("category", "Eletrônicos", 1, 1)
    private val item = ItemEntity("item", "Fone", category.id, 10, 10000, "BRL", null, 2, 2)

    @Test fun `accepts a complete consistent lifecycle`() {
        BackupValidator.validate(
            listOf(category), listOf(item),
            listOf(ItemUpdateEntity("update", item.id, 11, "Troca de bateria", 500, 3, 3)),
            listOf(ItemClosureEntity(item.id, 12, "SOLD", null, 8000, 4, 4)),
        )
    }

    @Test fun `rejects item whose category is missing`() {
        assertThrows(BackupException.InvalidFile::class.java) {
            BackupValidator.validate(emptyList(), listOf(item), emptyList(), emptyList())
        }
    }

    @Test fun `rejects history whose item is missing`() {
        val update = ItemUpdateEntity("update", "missing", 11, "Reparo", null, 3, 3)
        assertThrows(BackupException.InvalidFile::class.java) {
            BackupValidator.validate(listOf(category), listOf(item), listOf(update), emptyList())
        }
    }

    @Test fun `rejects closure before latest update`() {
        val update = ItemUpdateEntity("update", item.id, 12, "Reparo", null, 3, 3)
        val closure = ItemClosureEntity(item.id, 11, "SOLD", null, null, 4, 4)
        assertThrows(BackupException.InvalidFile::class.java) {
            BackupValidator.validate(listOf(category), listOf(item), listOf(update), listOf(closure))
        }
    }

    @Test fun `rejects empty restore`() {
        assertThrows(BackupException.EmptyBackup::class.java) {
            BackupValidator.validate(emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
}
