package io.github.dhianapereira.inventario.data.type

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.dhianapereira.inventario.model.ItemType

@Entity(tableName = "item_types")
data class ItemTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
) {
    fun toModel() = ItemType(id, name)

    companion object {
        fun fromModel(type: ItemType) = ItemTypeEntity(type.id, type.name)
    }
}
