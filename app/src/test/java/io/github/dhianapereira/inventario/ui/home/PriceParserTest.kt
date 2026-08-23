package io.github.dhianapereira.inventario.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceParserTest {
    @Test fun `parses Brazilian decimal value`() = assertEquals(189_900L, parsePriceInCents("1.899,00"))
    @Test fun `parses dot decimal value`() = assertEquals(12_990L, parsePriceInCents("129.90"))
    @Test fun `rejects empty value`() = assertNull(parsePriceInCents(""))
    @Test fun `rejects negative value`() = assertNull(parsePriceInCents("-1,00"))
}
