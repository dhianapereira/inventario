package io.github.dhianapereira.inventario.model

data class Category(
    val id: String,
    val name: String,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)
