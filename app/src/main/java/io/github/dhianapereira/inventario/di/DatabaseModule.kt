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
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureDao
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateDao
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
            InventarioDatabase.Migration3To4,
            InventarioDatabase.Migration4To5,
            InventarioDatabase.Migration5To6,
            InventarioDatabase.Migration6To7,
            InventarioDatabase.Migration7To8,
        ).build()
    }

    @Provides
    fun provideItemDao(database: InventarioDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideCategoryDao(database: InventarioDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideItemUpdateDao(database: InventarioDatabase): ItemUpdateDao = database.itemUpdateDao()

    @Provides
    fun provideItemClosureDao(database: InventarioDatabase): ItemClosureDao = database.itemClosureDao()
}
