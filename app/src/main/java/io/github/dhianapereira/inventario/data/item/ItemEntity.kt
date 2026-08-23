package io.github.dhianapereira.inventario.data.item

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.dhianapereira.inventario.model.Item
import java.time.LocalDate

@Entity(
    tableName = "items",
    indices = [Index("typeId")],
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val typeId: String,
    val arrivalDateEpochDay: Long,
    val purchasePriceInCents: Long,
) {
    fun toModel() = Item(
        id = id,
        name = name,
        typeId = typeId,
        arrivalDate = LocalDate.ofEpochDay(arrivalDateEpochDay),
        purchasePriceInCents = purchasePriceInCents,
    )

    companion object {
        fun fromModel(item: Item) = ItemEntity(
            id = item.id,
            name = item.name,
            typeId = item.typeId,
            arrivalDateEpochDay = item.arrivalDate.toEpochDay(),
            purchasePriceInCents = item.purchasePriceInCents,
        )
    }
}
