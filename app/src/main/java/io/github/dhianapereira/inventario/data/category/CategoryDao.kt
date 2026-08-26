package io.github.dhianapereira.inventario.data.category

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity): Int

    @Delete
    suspend fun delete(category: CategoryEntity): Int

    @Query("SELECT COUNT(*) FROM items WHERE categoryId = :categoryId")
    suspend fun countItems(categoryId: String): Int

    @Query("SELECT * FROM categories ORDER BY id")
    suspend fun getAllForBackup(): List<CategoryEntity>

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
