package io.github.dhianapereira.inventario.data.item

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.dhianapereira.inventario.data.database.InventarioDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {
    private lateinit var database: InventarioDatabase
    private lateinit var dao: ItemDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, InventarioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.itemDao()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun itemCanBeCreatedReadUpdatedAndDeleted() = runTest {
        val item = ItemEntity("1", "Headphones", "electronics", 19_793, 189_900)
        dao.insert(item)

        assertEquals(item, dao.findById("1"))
        assertEquals(listOf(item), dao.observeAll().first())

        val updated = item.copy(name = "Sony Headphones", purchasePriceInCents = 179_900)
        assertEquals(1, dao.update(updated))
        assertEquals(updated, dao.findById("1"))

        assertEquals(1, dao.delete(updated))
        assertNull(dao.findById("1"))
    }
}
