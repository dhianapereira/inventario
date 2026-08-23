package io.github.dhianapereira.inventario.model

enum class AppLanguage(val tag: String) {
    PORTUGUESE("pt-BR"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String) = if (code == "en") ENGLISH else PORTUGUESE
    }
}
