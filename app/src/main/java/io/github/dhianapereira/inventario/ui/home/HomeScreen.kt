package io.github.dhianapereira.inventario.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dhianapereira.inventario.R
import io.github.dhianapereira.inventario.ui.components.IndustrialField
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.ItemType
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeRoute(
    onMoreClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onSaveItem = viewModel::saveItem,
        onDeleteItem = viewModel::deleteItem,
        onCreateType = viewModel::createType,
        onUpdateType = viewModel::updateType,
        onDeleteType = viewModel::deleteType,
        onMoreClick = onMoreClick,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onSaveItem: (String?, String, String, String, String) -> Boolean,
    onDeleteItem: (String) -> Unit,
    onCreateType: (String) -> Unit,
    onUpdateType: (ItemType, String) -> Unit,
    onDeleteType: (ItemType) -> Unit,
    onMoreClick: () -> Unit,
) {
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var showItemForm by remember { mutableStateOf(false) }
    var showCategories by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var selectedTypeId by remember { mutableStateOf<String?>(null) }
    val visibleItems = state.items.filter { item ->
        (selectedTypeId == null || item.typeId == selectedTypeId) &&
            item.name.contains(search, ignoreCase = true)
    }

    if (showItemForm) {
        ItemFormPage(
            item = editingItem,
            types = state.types,
            onBack = { showItemForm = false },
            onSave = { id, name, typeId, date, price ->
                if (onSaveItem(id, name, typeId, date, price)) showItemForm = false
            },
            onDelete = { id -> onDeleteItem(id); showItemForm = false },
        )
        return
    }
    androidx.activity.compose.BackHandler(enabled = showCategories) { showCategories = false }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            IndustrialBottomBar(
                categoriesSelected = showCategories,
                onInventoryClick = { showCategories = false },
                onTypesClick = { showCategories = true },
                onMoreClick = onMoreClick,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter,
        ) {
            if (showCategories) {
                TypesContent(
                    types = state.types,
                    modifier = Modifier.widthIn(max = 680.dp),
                    onCreate = onCreateType,
                    onUpdate = onUpdateType,
                    onDelete = onDeleteType,
                )
                return@Box
            }
            Column(Modifier.widthIn(max = 680.dp).fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.app_name).uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .clickable { editingItem = null; showItemForm = true },
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
            Spacer(Modifier.height(20.dp))
            IndustrialField(
                value = search,
                onValueChange = { search = it },
                icon = Icons.Outlined.Search,
                placeholder = stringResource(R.string.search_items).uppercase(),
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CategoryFilter(
                    label = stringResource(R.string.all),
                    selected = selectedTypeId == null,
                    onClick = { selectedTypeId = null },
                )
                state.types.forEach { type ->
                    CategoryFilter(type.name, selectedTypeId == type.id) { selectedTypeId = type.id }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (visibleItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(R.string.no_items).uppercase(), style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.no_items_description), style = MaterialTheme.typography.bodyMedium)
                }
                } else {
                    LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(visibleItems, key = Item::id) { item ->
                            ItemCard(
                                item = item,
                                type = state.types.find { it.id == item.typeId },
                                onEdit = { editingItem = item; showItemForm = true },
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun ItemCard(item: Item, type: ItemType?, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                    Text(item.name.uppercase(), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        (type?.name ?: stringResource(R.string.unknown_type)).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(
                            R.string.arrived_on,
                            item.arrivalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        ).uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(NumberFormat.getCurrencyInstance().format(item.purchasePriceInCents / 100.0))
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(item.id.takeLast(4).uppercase(), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun CategoryFilter(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    Text(
        text = label.uppercase(),
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(background)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        color = content,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun IndustrialBottomBar(
    categoriesSelected: Boolean,
    onInventoryClick: () -> Unit,
    onTypesClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomDestination(
            Icons.Outlined.Inventory2,
            stringResource(R.string.inventory),
            selected = !categoriesSelected,
            onClick = onInventoryClick,
        )
        BottomDestination(
            Icons.Outlined.Category,
            stringResource(R.string.categories),
            selected = categoriesSelected,
            onClick = onTypesClick,
        )
        BottomDestination(Icons.Outlined.MoreHoriz, stringResource(R.string.more), onClick = onMoreClick)
    }
}

@Composable
private fun BottomDestination(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.width(80.dp).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        if (selected) Spacer(Modifier.height(3.dp).fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ItemFormPage(
    item: Item?,
    types: List<ItemType>,
    onBack: () -> Unit,
    onSave: (String?, String, String, String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var name by remember { mutableStateOf(item?.name.orEmpty()) }
    var typeId by remember { mutableStateOf(item?.typeId ?: types.firstOrNull()?.id.orEmpty()) }
    var date by remember { mutableStateOf(item?.arrivalDate?.toString() ?: LocalDate.now().toString()) }
    var price by remember {
        mutableStateOf(item?.let { formatPriceInput(it.purchasePriceInCents.toString()) }.orEmpty())
    }
    var showTypeSheet by remember { mutableStateOf(false) }
    val selectedType = types.find { it.id == typeId }
    val valid = name.isNotBlank() && typeId.isNotBlank() &&
        runCatching { LocalDate.parse(date) }.isSuccess && parsePriceInCents(price) != null

    androidx.activity.compose.BackHandler(onBack = onBack)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = androidx.compose.ui.Alignment.TopCenter,
    ) {
        Column(Modifier.widthIn(max = 680.dp).fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        EditorialHeader(
            title = stringResource(if (item == null) R.string.new_item else R.string.edit_item),
            onBack = onBack,
            actionEnabled = valid,
            onAction = { onSave(item?.id, name, typeId, date, price) },
        )
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            FieldLabel(stringResource(R.string.name))
            IndustrialField(name, { name = it }, isError = name.isNotEmpty() && name.isBlank())
            FieldLabel(stringResource(R.string.item_type))
            IndustrialField(
                value = (selectedType?.name ?: stringResource(R.string.select_type)).uppercase(),
                onValueChange = {},
                icon = Icons.Outlined.Sell,
                readOnly = true,
                isError = types.isEmpty(),
                onClick = { showTypeSheet = true },
            )
            if (types.isEmpty()) ErrorText(stringResource(R.string.type_required))
            FieldLabel(stringResource(R.string.arrival_date))
            IndustrialField(
                date,
                { date = it },
                icon = Icons.Outlined.CalendarMonth,
                isError = runCatching { LocalDate.parse(date) }.isFailure,
            )
            if (runCatching { LocalDate.parse(date) }.isFailure) ErrorText(stringResource(R.string.invalid_date))
            FieldLabel(stringResource(R.string.purchase_price))
            IndustrialField(
                price,
                { price = formatPriceInput(it) },
                icon = Icons.Outlined.AttachMoney,
                isError = price.isNotEmpty() && parsePriceInCents(price) == null,
            )
            if (price.isNotEmpty() && parsePriceInCents(price) == null) ErrorText(stringResource(R.string.invalid_price))
            if (item != null) {
                OutlinedButton(
                    onClick = { onDelete(item.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.delete_item).uppercase()) }
            }
        }
        }
    }
    if (showTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTypeSheet = false },
            shape = androidx.compose.ui.graphics.RectangleShape,
            dragHandle = null,
        ) {
            Text(
                stringResource(R.string.select_type).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            types.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { typeId = type.id; showTypeSheet = false }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(type.name.uppercase(), style = MaterialTheme.typography.titleMedium)
                    if (type.id == typeId) Icon(Icons.Outlined.Check, null)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypesContent(
    types: List<ItemType>,
    modifier: Modifier = Modifier,
    onCreate: (String) -> Unit,
    onUpdate: (ItemType, String) -> Unit,
    onDelete: (ItemType) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<ItemType?>(null) }
    Column(
        modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.item_types).uppercase(), style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(24.dp))
        FieldLabel(stringResource(R.string.type_name))
        Spacer(Modifier.height(8.dp))
        IndustrialField(name, { name = it })
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = androidx.compose.ui.graphics.RectangleShape,
            onClick = {
                editing?.let { onUpdate(it, name) } ?: onCreate(name)
                name = ""; editing = null
            },
        ) {
            Text(
                (if (editing == null) "+  " else "") +
                    stringResource(if (editing == null) R.string.add else R.string.save).uppercase(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.all_types).uppercase(), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(types, key = ItemType::id) { type ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { editing = type; name = type.name },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(type.name.uppercase(), style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { onDelete(type) }) { Text(stringResource(R.string.delete).uppercase()) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorialHeader(
    title: String,
    onBack: () -> Unit,
    actionEnabled: Boolean = false,
    onAction: () -> Unit = {},
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }
        Text(title.uppercase(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        IconButton(
            onClick = onAction,
            enabled = actionEnabled,
            modifier = Modifier.background(
                if (actionEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Icon(
                Icons.Outlined.Check,
                stringResource(R.string.save),
                tint = if (actionEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium)
}

@Composable
private fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}
