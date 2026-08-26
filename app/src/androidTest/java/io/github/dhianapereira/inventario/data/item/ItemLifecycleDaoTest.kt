package io.github.dhianapereira.inventario.data.item

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.dhianapereira.inventario.data.database.InventarioDatabase
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureEntity
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemLifecycleDaoTest {
    private lateinit var database: InventarioDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, InventarioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun updatesAndClosureArePersistedAndDeletedWithItem() = runTest {
        val item = ItemEntity("item-1", "Headphones", "electronics", 19_793, 189_900)
        val update = ItemUpdateEntity("update-1", item.id, 20_000, "Battery replaced", 12_990)
        val closure = ItemClosureEntity(item.id, 21_000, "SOLD", "Working well", 100_000)

        database.itemDao().insert(item)
        database.itemUpdateDao().insert(update)
        database.itemClosureDao().upsert(closure)

        assertEquals(listOf(update), database.itemUpdateDao().observeAll().first())
        assertEquals(listOf(closure), database.itemClosureDao().observeAll().first())

        database.itemDao().delete(item)

        assertTrue(database.itemUpdateDao().observeAll().first().isEmpty())
        assertTrue(database.itemClosureDao().observeAll().first().isEmpty())
    }
}
