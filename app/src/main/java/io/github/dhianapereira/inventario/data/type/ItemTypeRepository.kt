package io.github.dhianapereira.inventario.data.type

import io.github.dhianapereira.inventario.model.ItemType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ItemTypeRepository @Inject constructor(
    private val dao: ItemTypeDao,
) {
    fun observeTypes(): Flow<List<ItemType>> = dao.observeAll()
        .map { types -> types.map(ItemTypeEntity::toModel) }

    suspend fun createType(name: String) {
        require(name.isNotBlank())
        dao.insert(ItemTypeEntity(UUID.randomUUID().toString(), name.trim()))
    }

    suspend fun updateType(type: ItemType): Boolean {
        require(type.name.isNotBlank())
        return dao.update(ItemTypeEntity.fromModel(type.copy(name = type.name.trim()))) > 0
    }

    suspend fun deleteType(type: ItemType): Boolean {
        if (dao.countItems(type.id) > 0) return false
        return dao.delete(ItemTypeEntity.fromModel(type)) > 0
    }
}
