package io.github.dhianapereira.inventario.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dhianapereira.inventario.data.item.ItemRepository
import io.github.dhianapereira.inventario.data.category.CategoryRepository
import io.github.dhianapereira.inventario.data.itemclosure.ItemClosureRepository
import io.github.dhianapereira.inventario.data.itemupdate.ItemUpdateRepository
import io.github.dhianapereira.inventario.model.ClosureReason
import io.github.dhianapereira.inventario.model.ItemClosure
import io.github.dhianapereira.inventario.model.ItemUpdate
import io.github.dhianapereira.inventario.model.ItemCurrency
import io.github.dhianapereira.inventario.model.isClosureDateValid
import io.github.dhianapereira.inventario.model.isUpdateDateValid
import io.github.dhianapereira.inventario.model.normalizedOptional
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

data class HomeUiState(
    val items: List<Item> = emptyList(),
    val categories: List<Category> = emptyList(),
    val updates: List<ItemUpdate> = emptyList(),
    val closures: List<ItemClosure> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val itemUpdateRepository: ItemUpdateRepository,
    private val itemClosureRepository: ItemClosureRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        itemRepository.observeItems(),
        categoryRepository.observeCategories(),
        itemUpdateRepository.observeAll(),
        itemClosureRepository.observeAll(),
        ::HomeUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun saveItem(
        id: String?,
        name: String,
        categoryId: String,
        arrivalDate: LocalDate,
        price: String,
        currency: ItemCurrency,
        description: String,
    ): Boolean {
        if (name.isBlank() || categoryId.isBlank()) return false
        val cents = parseCurrencyDigits(price) ?: return false
        viewModelScope.launch {
            runCatching {
                if (id == null) {
                    itemRepository.createItem(name, categoryId, arrivalDate, cents, currency, description)
                } else {
                    itemRepository.updateItem(
                        Item(id, name, categoryId, arrivalDate, cents, currency, description.trim().ifEmpty { null }),
                    )
                }
            }
        }
        return true
    }

    fun deleteItem(id: String) = viewModelScope.launch { itemRepository.deleteItem(id) }

    fun saveUpdate(
        existing: ItemUpdate?,
        itemId: String,
        description: String,
        date: LocalDate,
        cost: String,
    ): Boolean {
        if (description.isBlank()) return false
        val cents = if (cost.isBlank()) null else parseCurrencyDigits(cost) ?: return false
        val item = uiState.value.items.find { it.id == itemId } ?: return false
        val closure = uiState.value.closures.find { it.itemId == itemId }
        if (!isUpdateDateValid(item, closure, date)) return false
        viewModelScope.launch {
            if (existing == null) {
                itemUpdateRepository.create(itemId, date, description, cents)
            } else {
                itemUpdateRepository.update(existing.copy(date = date, description = description, costInCents = cents))
            }
        }
        return true
    }

    fun deleteUpdate(update: ItemUpdate) = viewModelScope.launch { itemUpdateRepository.delete(update) }

    fun saveClosure(
        itemId: String,
        date: LocalDate,
        reason: ClosureReason,
        note: String,
        recoveredValue: String,
    ): Boolean {
        val recoveredValueInCents = if (reason == ClosureReason.SOLD && recoveredValue.isNotBlank()) {
            parseCurrencyDigits(recoveredValue) ?: return false
        } else {
            null
        }
        val item = uiState.value.items.find { it.id == itemId } ?: return false
        val updates = uiState.value.updates.filter { it.itemId == itemId }
        if (!isClosureDateValid(item, updates, date)) return false
        viewModelScope.launch {
            itemClosureRepository.save(
                ItemClosure(itemId, date, reason, note.normalizedOptional(), recoveredValueInCents),
            )
        }
        return true
    }

    fun deleteClosure(closure: ItemClosure) = viewModelScope.launch { itemClosureRepository.delete(closure) }

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

internal fun sanitizeCurrencyDigits(value: String): String = value.filter(Char::isDigit).take(12)

internal fun parseCurrencyDigits(value: String): Long? =
    sanitizeCurrencyDigits(value).takeIf(String::isNotEmpty)?.toLongOrNull()

internal fun currencyInputPlaceholder(): String = formatPriceInput("0")

internal object CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = formatPriceInput(text.text)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset == 0 && text.isEmpty()) 0 else formatted.length

            override fun transformedToOriginal(offset: Int): Int = text.length
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
