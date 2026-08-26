package io.github.dhianapereira.inventario.data.category

import io.github.dhianapereira.inventario.model.Category
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: CategoryDao,
) {
    fun observeCategories(): Flow<List<Category>> = dao.observeAll()
        .map { categories -> categories.map(CategoryEntity::toModel) }

    suspend fun createCategory(name: String) {
        require(name.isNotBlank())
        dao.insert(CategoryEntity(UUID.randomUUID().toString(), name.trim()))
    }

    suspend fun updateCategory(category: Category): Boolean {
        require(category.name.isNotBlank())
        return dao.update(CategoryEntity.fromModel(category.copy(name = category.name.trim()))) > 0
    }

    suspend fun deleteCategory(category: Category): Boolean {
        if (dao.countItems(category.id) > 0) return false
        return dao.delete(CategoryEntity.fromModel(category)) > 0
    }
}
