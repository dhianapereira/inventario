package io.github.dhianapereira.inventario.data.itemupdate

import io.github.dhianapereira.inventario.model.ItemUpdate
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ItemUpdateRepository @Inject constructor(private val dao: ItemUpdateDao) {
    private val lastCreatedAt = AtomicLong(0)

    fun observeAll(): Flow<List<ItemUpdate>> = dao.observeAll().map { rows -> rows.map(ItemUpdateEntity::toModel) }

    suspend fun create(itemId: String, date: LocalDate, description: String, costInCents: Long?) {
        require(description.isNotBlank())
        require(costInCents == null || costInCents >= 0)
        val now = nextCreatedAt()
        dao.insert(
            ItemUpdateEntity(
                UUID.randomUUID().toString(),
                itemId,
                date.toEpochDay(),
                description.trim(),
                costInCents,
                now,
                now,
            ),
        )
    }

    suspend fun update(update: ItemUpdate): Boolean = dao.update(
        ItemUpdateEntity.fromModel(
            update.copy(description = update.description.trim(), updatedAtEpochMillis = System.currentTimeMillis()),
        ),
    ) > 0

    suspend fun delete(update: ItemUpdate): Boolean = dao.delete(ItemUpdateEntity.fromModel(update)) > 0

    private fun nextCreatedAt(): Long = lastCreatedAt.updateAndGet { previous ->
        maxOf(System.currentTimeMillis(), previous + 1)
    }
}
