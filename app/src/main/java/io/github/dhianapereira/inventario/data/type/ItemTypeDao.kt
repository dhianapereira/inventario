package io.github.dhianapereira.inventario.data.type

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemTypeDao {
    @Query("SELECT * FROM item_types ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<ItemTypeEntity>>

    @Insert
    suspend fun insert(type: ItemTypeEntity)

    @Update
    suspend fun update(type: ItemTypeEntity): Int

    @Delete
    suspend fun delete(type: ItemTypeEntity): Int

    @Query("SELECT COUNT(*) FROM items WHERE typeId = :typeId")
    suspend fun countItems(typeId: String): Int
}
