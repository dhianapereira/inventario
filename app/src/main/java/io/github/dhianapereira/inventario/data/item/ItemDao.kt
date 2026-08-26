package io.github.dhianapereira.inventario.data.item

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun observeById(id: String): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun findById(id: String): ItemEntity?

    @Insert
    suspend fun insert(item: ItemEntity)

    @Update
    suspend fun update(item: ItemEntity): Int

    @Delete
    suspend fun delete(item: ItemEntity): Int

    @Query("SELECT * FROM items ORDER BY id")
    suspend fun getAllForBackup(): List<ItemEntity>

    @Insert
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
