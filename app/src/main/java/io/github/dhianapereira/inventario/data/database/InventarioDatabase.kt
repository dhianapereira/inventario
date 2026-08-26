package io.github.dhianapereira.inventario.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.dhianapereira.inventario.data.item.ItemDao
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.data.category.CategoryDao
import io.github.dhianapereira.inventario.data.category.CategoryEntity

@Database(
    entities = [ItemEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `item_types` " +
                        "(`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))",
                )
            }
        }
        val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` " +
                        "(`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL("INSERT INTO `categories` (`id`, `name`) SELECT `id`, `name` FROM `item_types`")
                db.execSQL(
                    "CREATE TABLE `items_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                        "`categoryId` TEXT NOT NULL, `arrivalDateEpochDay` INTEGER NOT NULL, " +
                        "`purchasePriceInCents` INTEGER NOT NULL, PRIMARY KEY(`id`))",
                )
                db.execSQL(
                    "INSERT INTO `items_new` (`id`, `name`, `categoryId`, `arrivalDateEpochDay`, `purchasePriceInCents`) " +
                        "SELECT `id`, `name`, `typeId`, `arrivalDateEpochDay`, `purchasePriceInCents` FROM `items`",
                )
                db.execSQL("DROP TABLE `items`")
                db.execSQL("ALTER TABLE `items_new` RENAME TO `items`")
                db.execSQL("CREATE INDEX `index_items_categoryId` ON `items` (`categoryId`)")
                db.execSQL("DROP TABLE `item_types`")
            }
        }
    }
}
