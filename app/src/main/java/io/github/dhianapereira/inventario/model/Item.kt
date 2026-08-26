package io.github.dhianapereira.inventario.model

import java.time.LocalDate
import java.time.Period

data class Item(
    val id: String,
    val name: String,
    val categoryId: String,
    val arrivalDate: LocalDate,
    val purchasePriceInCents: Long,
) {
    fun timeInUse(onDate: LocalDate = LocalDate.now()): Period {
        return Period.between(arrivalDate, onDate)
    }
}
