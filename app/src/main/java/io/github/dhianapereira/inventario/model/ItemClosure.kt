package io.github.dhianapereira.inventario.model

import java.time.LocalDate

data class ItemClosure(
    val itemId: String,
    val date: LocalDate,
    val reason: ClosureReason,
    val note: String?,
    val recoveredValueInCents: Long?,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)

enum class ClosureReason {
    STOPPED_WORKING,
    DONATED,
    SOLD,
    DISCARDED_OR_RECYCLED,
    LOST,
    STOLEN,
    REPLACED,
    OTHER,
}
