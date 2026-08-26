package io.github.dhianapereira.inventario.data.item

import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.ItemCurrency
import io.github.dhianapereira.inventario.model.normalizedOptional
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao,
) {
    fun observeItems(): Flow<List<Item>> = itemDao.observeAll()
        .map { items -> items.map(ItemEntity::toModel) }

    fun observeItem(id: String): Flow<Item?> = itemDao.observeById(id)
        .map { it?.toModel() }

    suspend fun getItem(id: String): Item? = itemDao.findById(id)?.toModel()

    suspend fun createItem(
        name: String,
        categoryId: String,
        arrivalDate: java.time.LocalDate,
        purchasePriceInCents: Long,
        currency: ItemCurrency,
        description: String?,
    ): String {
        require(name.isNotBlank()) { "Item name cannot be blank" }
        require(categoryId.isNotBlank()) { "Item category cannot be blank" }
        require(purchasePriceInCents >= 0) { "Purchase price cannot be negative" }

        val now = System.currentTimeMillis()
        val item = Item(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            categoryId = categoryId,
            arrivalDate = arrivalDate,
            purchasePriceInCents = purchasePriceInCents,
            currency = currency,
            description = description?.normalizedOptional(),
            createdAt = now,
            updatedAt = now,
        )
        itemDao.insert(ItemEntity.fromModel(item))
        return item.id
    }

    suspend fun updateItem(item: Item): Boolean {
        require(item.name.isNotBlank()) { "Item name cannot be blank" }
        require(item.categoryId.isNotBlank()) { "Item category cannot be blank" }
        require(item.purchasePriceInCents >= 0) { "Purchase price cannot be negative" }
        val existing = itemDao.findById(item.id) ?: return false
        return itemDao.update(
            ItemEntity.fromModel(
                item.copy(
                    name = item.name.trim(),
                    createdAt = existing.createdAt,
                    updatedAt = System.currentTimeMillis(),
                ),
            ),
        ) > 0
    }

    suspend fun deleteItem(id: String): Boolean {
        val item = itemDao.findById(id) ?: return false
        return itemDao.delete(item) > 0
    }
}
