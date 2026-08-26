package io.github.dhianapereira.inventario.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceParserTest {
    @Test fun `parses Brazilian decimal value`() = assertEquals(189_900L, parsePriceInCents("1.899,00"))
    @Test fun `parses dot decimal value`() = assertEquals(12_990L, parsePriceInCents("129.90"))
    @Test fun `rejects empty value`() = assertNull(parsePriceInCents(""))
    @Test fun `rejects negative value`() = assertNull(parsePriceInCents("-1,00"))

    @Test
    fun `formatted input remains parseable`() {
        assertEquals(123_45L, parsePriceInCents(formatPriceInput("12345")))
    }

    @Test fun `currency digits represent cents directly`() =
        assertEquals(12_345L, parseCurrencyDigits("12345"))

    @Test fun `currency input strips formatting and limits its size`() =
        assertEquals("123456789012", sanitizeCurrencyDigits("R$ 1.234.567.890.123,45"))

    @Test fun `currency digits reject an empty value`() = assertNull(parseCurrencyDigits(""))

    @Test fun `currency placeholder displays zero cents`() =
        assertEquals(formatPriceInput("0"), currencyInputPlaceholder())
}
