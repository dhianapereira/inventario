package io.github.dhianapereira.inventario.data.itemclosure

import io.github.dhianapereira.inventario.model.ItemClosure
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ItemClosureRepository @Inject constructor(private val dao: ItemClosureDao) {
    fun observeAll(): Flow<List<ItemClosure>> = dao.observeAll().map { rows -> rows.map(ItemClosureEntity::toModel) }

    suspend fun save(closure: ItemClosure) {
        val existing = dao.findByItemId(closure.itemId)
        val now = System.currentTimeMillis()
        dao.upsert(
            ItemClosureEntity.fromModel(
                closure.copy(createdAt = existing?.createdAt ?: now, updatedAt = now),
            ),
        )
    }

    suspend fun delete(closure: ItemClosure): Boolean = dao.delete(ItemClosureEntity.fromModel(closure)) > 0
}
