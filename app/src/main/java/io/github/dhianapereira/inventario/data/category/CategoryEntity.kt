package io.github.dhianapereira.inventario.data.category

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import io.github.dhianapereira.inventario.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "created_at", defaultValue = "0") val createdAt: Long = 0,
    @ColumnInfo(name = "updated_at", defaultValue = "0") val updatedAt: Long = 0,
) {
    fun toModel() = Category(id, name, createdAt, updatedAt)

    companion object {
        fun fromModel(category: Category) = CategoryEntity(
            category.id,
            category.name,
            category.createdAt,
            category.updatedAt,
        )
    }
}
