package io.github.dhianapereira.inventario.data.itemclosure

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemClosureDao {
    @Query("SELECT * FROM item_closures ORDER BY dateEpochDay, itemId")
    fun observeAll(): Flow<List<ItemClosureEntity>>

    @Query("SELECT * FROM item_closures WHERE itemId = :itemId")
    suspend fun findByItemId(itemId: String): ItemClosureEntity?

    @Upsert
    suspend fun upsert(closure: ItemClosureEntity)

    @Delete
    suspend fun delete(closure: ItemClosureEntity): Int

    @Query("SELECT * FROM item_closures ORDER BY itemId")
    suspend fun getAllForBackup(): List<ItemClosureEntity>

    @Upsert
    suspend fun upsertAll(closures: List<ItemClosureEntity>)

    @Query("DELETE FROM item_closures")
    suspend fun deleteAll()
}
