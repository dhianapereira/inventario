package io.github.dhianapereira.inventario.model

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStorage(value: String?) = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
