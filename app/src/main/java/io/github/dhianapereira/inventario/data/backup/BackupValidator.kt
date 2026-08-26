package io.github.dhianapereira.inventario.data.backup

import io.github.dhianapereira.inventario.data.category.CategoryEntity
import io.github.dhianapereira.inventario.data.item.ItemEntity
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureEntity
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateEntity
import io.github.dhianapereira.inventario.model.ClosureReason
import io.github.dhianapereira.inventario.model.ItemCurrency

internal object BackupValidator {
    fun validate(
        categories: List<CategoryEntity>,
        items: List<ItemEntity>,
        updates: List<ItemUpdateEntity>,
        closures: List<ItemClosureEntity>,
    ) {
        if (categories.isEmpty() && items.isEmpty() && updates.isEmpty() && closures.isEmpty()) {
            throw BackupException.EmptyBackup()
        }
        val categoryIds = categories.map { it.id }.toSet()
        val itemIds = items.map { it.id }.toSet()
        if (categoryIds.size != categories.size || itemIds.size != items.size ||
            updates.map { it.id }.toSet().size != updates.size ||
            closures.map { it.itemId }.toSet().size != closures.size
        ) invalid()
        categories.forEach {
            validText(it.id, MAX_ID_LENGTH); validText(it.name); validTimestamps(it.createdAt, it.updatedAt)
        }
        items.forEach {
            validText(it.id, MAX_ID_LENGTH); validText(it.name)
            if (it.categoryId !in categoryIds || it.purchasePriceInCents < 0 || ItemCurrency.entries.none { currency -> currency.code == it.currencyCode }) invalid()
            validDate(it.arrivalDateEpochDay); optionalText(it.description); validTimestamps(it.createdAt, it.updatedAt)
        }
        updates.forEach {
            val item = items.find { item -> item.id == it.itemId }
            if (item == null || it.costInCents?.let { cost -> cost < 0 } == true || it.dateEpochDay < item.arrivalDateEpochDay) invalid()
            validText(it.id, MAX_ID_LENGTH); validText(it.description); validDate(it.dateEpochDay)
            validTimestamps(it.createdAtEpochMillis, it.updatedAtEpochMillis)
        }
        closures.forEach {
            val item = items.find { item -> item.id == it.itemId }
            val latestUpdate = updates.filter { update -> update.itemId == it.itemId }.maxOfOrNull { update -> update.dateEpochDay }
            if (item == null || it.dateEpochDay < item.arrivalDateEpochDay || latestUpdate?.let { date -> it.dateEpochDay < date } == true ||
                ClosureReason.entries.none { reason -> reason.name == it.reason } || it.recoveredValueInCents?.let { value -> value < 0 } == true
            ) invalid()
            validDate(it.dateEpochDay); optionalText(it.note); validTimestamps(it.createdAt, it.updatedAt)
        }
    }

    private fun validDate(value: Long) { if (value !in MIN_EPOCH_DAY..MAX_EPOCH_DAY) invalid() }
    private fun validTimestamps(created: Long, updated: Long) { if (created < 0 || updated < created) invalid() }
    private fun validText(value: String, max: Int = MAX_TEXT_LENGTH) { if (value.isBlank() || value.length > max) invalid() }
    private fun optionalText(value: String?) { if (value != null && (value.isBlank() || value.length > MAX_TEXT_LENGTH)) invalid() }
    private fun invalid(): Nothing = throw BackupException.InvalidFile()

    private const val MAX_ID_LENGTH = 200
    private const val MAX_TEXT_LENGTH = 20_000
    private const val MIN_EPOCH_DAY = -365_243_219_162L
    private const val MAX_EPOCH_DAY = 365_241_780_471L
}
