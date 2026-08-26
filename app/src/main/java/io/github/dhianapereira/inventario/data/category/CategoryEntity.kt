package io.github.dhianapereira.inventario.data.category

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.dhianapereira.inventario.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
) {
    fun toModel() = Category(id, name)

    companion object {
        fun fromModel(category: Category) = CategoryEntity(category.id, category.name)
    }
}
