package io.github.dhianapereira.inventario.data.item

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.ItemCurrency
import io.github.dhianapereira.inventario.model.normalizedOptional
import java.time.LocalDate

@Entity(
    tableName = "items",
    indices = [Index("categoryId")],
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val arrivalDateEpochDay: Long,
    val purchasePriceInCents: Long,
    @ColumnInfo(defaultValue = "'BRL'") val currencyCode: String = "BRL",
    val description: String? = null,
    @ColumnInfo(name = "created_at", defaultValue = "0") val createdAt: Long = 0,
    @ColumnInfo(name = "updated_at", defaultValue = "0") val updatedAt: Long = 0,
) {
    fun toModel() = Item(
        id = id,
        name = name,
        categoryId = categoryId,
        arrivalDate = LocalDate.ofEpochDay(arrivalDateEpochDay),
        purchasePriceInCents = purchasePriceInCents,
        currency = ItemCurrency.fromCode(currencyCode),
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromModel(item: Item) = ItemEntity(
            id = item.id,
            name = item.name,
            categoryId = item.categoryId,
            arrivalDateEpochDay = item.arrivalDate.toEpochDay(),
            purchasePriceInCents = item.purchasePriceInCents,
            currencyCode = item.currency.code,
            description = item.description?.normalizedOptional(),
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
        )
    }
}
