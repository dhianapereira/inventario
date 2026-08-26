package io.github.dhianapereira.inventario.model

enum class ItemCurrency(val code: String) {
    BRL("BRL"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP");

    companion object {
        fun fromCode(code: String): ItemCurrency = entries.find { it.code == code } ?: BRL
    }
}
