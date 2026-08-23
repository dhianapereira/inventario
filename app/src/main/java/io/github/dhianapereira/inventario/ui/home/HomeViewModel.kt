package io.github.dhianapereira.inventario.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dhianapereira.inventario.data.item.ItemRepository
import io.github.dhianapereira.inventario.data.type.ItemTypeRepository
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.ItemType
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<Item> = emptyList(),
    val types: List<ItemType> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val typeRepository: ItemTypeRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        itemRepository.observeItems(),
        typeRepository.observeTypes(),
        ::HomeUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun saveItem(id: String?, name: String, typeId: String, date: String, price: String) {
        val arrivalDate = LocalDate.parse(date)
        val cents = BigDecimal(price.replace(',', '.')).movePointRight(2).longValueExact()
        viewModelScope.launch {
            if (id == null) {
                itemRepository.createItem(name, typeId, arrivalDate, cents)
            } else {
                itemRepository.updateItem(Item(id, name, typeId, arrivalDate, cents))
            }
        }
    }

    fun deleteItem(id: String) = viewModelScope.launch { itemRepository.deleteItem(id) }

    fun createType(name: String) = viewModelScope.launch { typeRepository.createType(name) }

    fun updateType(type: ItemType, name: String) = viewModelScope.launch {
        typeRepository.updateType(type.copy(name = name))
    }

    fun deleteType(type: ItemType) = viewModelScope.launch { typeRepository.deleteType(type) }
}
