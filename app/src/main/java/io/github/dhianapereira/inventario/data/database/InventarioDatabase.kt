package io.github.dhianapereira.inventario.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.dhianapereira.inventario.data.item.ItemDao
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.data.category.CategoryDao
import io.github.dhianapereira.inventario.data.category.CategoryEntity
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureDao
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureEntity
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateDao
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateEntity

@Database(
    entities = [ItemEntity::class, CategoryEntity::class, ItemUpdateEntity::class, ItemClosureEntity::class],
    version = 8,
    exportSchema = true,
)
abstract class InventarioDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun itemUpdateDao(): ItemUpdateDao
    abstract fun itemClosureDao(): ItemClosureDao

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
        val Migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `item_updates` (`id` TEXT NOT NULL, `itemId` TEXT NOT NULL, " +
                        "`dateEpochDay` INTEGER NOT NULL, `description` TEXT NOT NULL, `costInCents` INTEGER, " +
                        "PRIMARY KEY(`id`), FOREIGN KEY(`itemId`) REFERENCES `items`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_updates_itemId` ON `item_updates` (`itemId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `item_closures` (`itemId` TEXT NOT NULL, " +
                        "`dateEpochDay` INTEGER NOT NULL, `reason` TEXT NOT NULL, `note` TEXT, " +
                        "`recoveredValueInCents` INTEGER, " +
                        "PRIMARY KEY(`itemId`), FOREIGN KEY(`itemId`) REFERENCES `items`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
            }
        }
        val Migration4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `items` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT 'BRL'")
            }
        }
        val Migration5To6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `items` ADD COLUMN `description` TEXT")
            }
        }
        val Migration6To7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `item_updates` ADD COLUMN `createdAtEpochMillis` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL("UPDATE `item_updates` SET `createdAtEpochMillis` = rowid")
            }
        }
        val Migration7To8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `item_updates` RENAME COLUMN `createdAtEpochMillis` TO `created_at`")
                db.execSQL("ALTER TABLE `item_updates` ADD COLUMN `updated_at` INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE `item_updates` SET `created_at` = (strftime('%s','now') * 1000) + `created_at`, " +
                        "`updated_at` = (strftime('%s','now') * 1000) + `created_at`",
                )
                listOf("items", "categories", "item_closures").forEach { table ->
                    db.execSQL("ALTER TABLE `$table` ADD COLUMN `created_at` INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE `$table` ADD COLUMN `updated_at` INTEGER NOT NULL DEFAULT 0")
                    db.execSQL(
                        "UPDATE `$table` SET `created_at` = (strftime('%s','now') * 1000) + rowid, " +
                            "`updated_at` = (strftime('%s','now') * 1000) + rowid",
                    )
                }
            }
        }
    }
}
