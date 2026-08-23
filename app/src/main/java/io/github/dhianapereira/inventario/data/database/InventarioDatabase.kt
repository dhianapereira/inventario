package io.github.dhianapereira.inventario.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.dhianapereira.inventario.data.item.ItemDao
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.data.type.ItemTypeDao
import io.github.dhianapereira.inventario.data.type.ItemTypeEntity

@Database(
    entities = [ItemEntity::class, ItemTypeEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun itemTypeDao(): ItemTypeDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `item_types` " +
                        "(`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))",
                )
            }
        }
    }
}
