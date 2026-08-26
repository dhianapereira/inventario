package io.github.dhianapereira.inventario.data.itemupdate

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.model.ItemUpdate
import java.time.LocalDate

@Entity(
    tableName = "item_updates",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itemId")],
)
data class ItemUpdateEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val dateEpochDay: Long,
    val description: String,
    val costInCents: Long?,
    @ColumnInfo(name = "created_at", defaultValue = "0") val createdAtEpochMillis: Long = 0,
    @ColumnInfo(name = "updated_at", defaultValue = "0") val updatedAtEpochMillis: Long = createdAtEpochMillis,
) {
    fun toModel() = ItemUpdate(
        id,
        itemId,
        LocalDate.ofEpochDay(dateEpochDay),
        description,
        costInCents,
        createdAtEpochMillis,
        updatedAtEpochMillis,
    )

    companion object {
        fun fromModel(update: ItemUpdate) = ItemUpdateEntity(
            update.id,
            update.itemId,
            update.date.toEpochDay(),
            update.description,
            update.costInCents,
            update.createdAtEpochMillis,
            update.updatedAtEpochMillis,
        )
    }
}
