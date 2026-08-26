package io.github.dhianapereira.inventario.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.dhianapereira.inventario.data.database.InventarioDatabase
import io.github.dhianapereira.inventario.data.item.ItemDao
import io.github.dhianapereira.inventario.data.category.CategoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InventarioDatabase {
        return Room.databaseBuilder(
            context,
            InventarioDatabase::class.java,
            "inventario.db",
        ).addMigrations(
            InventarioDatabase.Migration1To2,
            InventarioDatabase.Migration2To3,
        ).build()
    }

    @Provides
    fun provideItemDao(database: InventarioDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideCategoryDao(database: InventarioDatabase): CategoryDao = database.categoryDao()
}
