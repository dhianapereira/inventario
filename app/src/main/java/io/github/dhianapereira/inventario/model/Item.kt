package io.github.dhianapereira.inventario.model

import java.time.LocalDate
import java.time.Period

data class Item(
    val id: String,
    val name: String,
    val categoryId: String,
    val arrivalDate: LocalDate,
    val purchasePriceInCents: Long,
    val currency: ItemCurrency = ItemCurrency.BRL,
    val description: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    fun timeInUse(onDate: LocalDate = LocalDate.now()): Period {
        return Period.between(arrivalDate, onDate)
    }
}
