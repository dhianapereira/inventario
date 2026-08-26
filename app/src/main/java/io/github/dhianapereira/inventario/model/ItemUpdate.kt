package io.github.dhianapereira.inventario.model

import java.time.LocalDate

data class ItemUpdate(
    val id: String,
    val itemId: String,
    val date: LocalDate,
    val description: String,
    val costInCents: Long?,
    val createdAtEpochMillis: Long = 0,
    val updatedAtEpochMillis: Long = createdAtEpochMillis,
)
