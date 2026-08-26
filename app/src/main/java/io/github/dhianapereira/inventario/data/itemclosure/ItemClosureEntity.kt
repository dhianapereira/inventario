package io.github.dhianapereira.inventario.data.itemclosure

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.model.ClosureReason
import io.github.dhianapereira.inventario.model.ItemClosure
import java.time.LocalDate
import io.github.dhianapereira.inventario.model.normalizedOptional

@Entity(
    tableName = "item_closures",
    foreignKeys = [ForeignKey(
        entity = ItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["itemId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class ItemClosureEntity(
    @PrimaryKey val itemId: String,
    val dateEpochDay: Long,
    val reason: String,
    val note: String?,
    val recoveredValueInCents: Long?,
    @ColumnInfo(name = "created_at", defaultValue = "0") val createdAt: Long = 0,
    @ColumnInfo(name = "updated_at", defaultValue = "0") val updatedAt: Long = 0,
) {
    fun toModel() = ItemClosure(
        itemId,
        LocalDate.ofEpochDay(dateEpochDay),
        ClosureReason.valueOf(reason),
        note,
        recoveredValueInCents,
        createdAt,
        updatedAt,
    )

    companion object {
        fun fromModel(closure: ItemClosure) = ItemClosureEntity(
            closure.itemId,
            closure.date.toEpochDay(),
            closure.reason.name,
            closure.note?.normalizedOptional(),
            closure.recoveredValueInCents,
            closure.createdAt,
            closure.updatedAt,
        )
    }
}
