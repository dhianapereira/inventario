package io.github.dhianapereira.inventario.model

import java.time.LocalDate

fun isUpdateDateValid(item: Item, closure: ItemClosure?, date: LocalDate): Boolean =
    date >= item.arrivalDate && closure?.let { date <= it.date } != false

fun isClosureDateValid(item: Item, updates: List<ItemUpdate>, date: LocalDate): Boolean =
    date >= item.arrivalDate && updates.maxOfOrNull(ItemUpdate::date)?.let { date >= it } != false

fun maintenanceCost(updates: List<ItemUpdate>): Long = updates.sumOf { it.costInCents ?: 0L }

fun timelineOrdered(updates: List<ItemUpdate>): List<ItemUpdate> = updates.sortedWith(
    compareBy<ItemUpdate>(ItemUpdate::date)
        .thenBy(ItemUpdate::createdAtEpochMillis)
        .thenBy(ItemUpdate::id),
)

fun totalCost(item: Item, updates: List<ItemUpdate>): Long = item.purchasePriceInCents + maintenanceCost(updates)

fun netCost(item: Item, updates: List<ItemUpdate>, closure: ItemClosure?): Long =
    totalCost(item, updates) - (closure?.recoveredValueInCents ?: 0L)

fun String.normalizedOptional(): String? = trim().ifEmpty { null }
