package io.github.dhianapereira.inventario.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dhianapereira.inventario.data.item.ItemRepository
import io.github.dhianapereira.inventario.data.category.CategoryRepository
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.Category
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<Item> = emptyList(),
    val categories: List<Category> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        itemRepository.observeItems(),
        categoryRepository.observeCategories(),
        ::HomeUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun saveItem(id: String?, name: String, categoryId: String, date: String, price: String): Boolean {
        if (name.isBlank() || categoryId.isBlank()) return false
        val arrivalDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return false
        val cents = parsePriceInCents(price) ?: return false
        viewModelScope.launch {
            runCatching {
                if (id == null) {
                    itemRepository.createItem(name, categoryId, arrivalDate, cents)
                } else {
                    itemRepository.updateItem(Item(id, name, categoryId, arrivalDate, cents))
                }
            }
        }
        return true
    }

    fun deleteItem(id: String) = viewModelScope.launch { itemRepository.deleteItem(id) }

    fun createCategory(name: String) = viewModelScope.launch { categoryRepository.createCategory(name) }

    fun updateCategory(category: Category, name: String) = viewModelScope.launch {
        categoryRepository.updateCategory(category.copy(name = name))
    }

    fun deleteCategory(category: Category) = viewModelScope.launch { categoryRepository.deleteCategory(category) }
}

internal fun parsePriceInCents(value: String): Long? {
    if ('-' in value) return null
    val trimmed = value.trim().filter { it.isDigit() || it == ',' || it == '.' }
    if (trimmed.isEmpty()) return null
    val lastSeparator = maxOf(trimmed.lastIndexOf(','), trimmed.lastIndexOf('.'))
    val decimalDigits = if (lastSeparator >= 0) trimmed.length - lastSeparator - 1 else 0
    val normalized = if (lastSeparator >= 0 && decimalDigits in 1..2) {
        trimmed.substring(0, lastSeparator).filter(Char::isDigit) + "." + trimmed.substring(lastSeparator + 1)
    } else trimmed.filter(Char::isDigit)
    return runCatching {
        val amount = BigDecimal(normalized).setScale(2, RoundingMode.UNNECESSARY)
        if (amount.signum() < 0) null else amount.movePointRight(2).longValueExact()
    }.getOrNull()
}

internal fun formatPriceInput(value: String): String {
    val digits = value.filter(Char::isDigit).take(12)
    if (digits.isEmpty()) return ""
    val padded = digits.padStart(3, '0')
    val integer = padded.dropLast(2).trimStart('0').ifEmpty { "0" }
    val symbols = DecimalFormatSymbols.getInstance()
    val grouped = integer.reversed().chunked(3).joinToString(symbols.groupingSeparator.toString()).reversed()
    return grouped + symbols.decimalSeparator + padded.takeLast(2)
}
