package io.github.dhianapereira.inventario.data.itemupdate

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemUpdateDao {
    @Query("SELECT * FROM item_updates ORDER BY dateEpochDay, created_at, id")
    fun observeAll(): Flow<List<ItemUpdateEntity>>

    @Insert
    suspend fun insert(update: ItemUpdateEntity)

    @Update
    suspend fun update(update: ItemUpdateEntity): Int

    @Delete
    suspend fun delete(update: ItemUpdateEntity): Int

    @Query("SELECT * FROM item_updates ORDER BY id")
    suspend fun getAllForBackup(): List<ItemUpdateEntity>

    @Insert
    suspend fun insertAll(updates: List<ItemUpdateEntity>)

    @Query("DELETE FROM item_updates")
    suspend fun deleteAll()
}
