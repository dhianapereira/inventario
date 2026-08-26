package io.github.dhianapereira.inventario.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dhianapereira.inventario.R
import io.github.dhianapereira.inventario.ui.components.IndustrialField
import io.github.dhianapereira.inventario.ui.components.IndustrialBottomSheet
import io.github.dhianapereira.inventario.ui.components.IndustrialSheetOption
import io.github.dhianapereira.inventario.ui.components.IndustrialSheetAction
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.Category
import io.github.dhianapereira.inventario.model.ClosureReason
import io.github.dhianapereira.inventario.model.ItemClosure
import io.github.dhianapereira.inventario.model.ItemUpdate
import io.github.dhianapereira.inventario.model.ItemCurrency
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency

@Composable
fun HomeRoute(
    settingsContent: @Composable (onBack: () -> Unit) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onSaveItem = viewModel::saveItem,
        onDeleteItem = viewModel::deleteItem,
        onCreateCategory = viewModel::createCategory,
        onUpdateCategory = viewModel::updateCategory,
        onDeleteCategory = viewModel::deleteCategory,
        onSaveUpdate = viewModel::saveUpdate,
        onDeleteUpdate = viewModel::deleteUpdate,
        onSaveClosure = viewModel::saveClosure,
        onDeleteClosure = viewModel::deleteClosure,
        settingsContent = settingsContent,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onSaveItem: (String?, String, String, String, String, ItemCurrency, String) -> Boolean,
    onDeleteItem: (String) -> Unit,
    onCreateCategory: (String) -> Unit,
    onUpdateCategory: (Category, String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onSaveUpdate: (ItemUpdate?, String, String, String, String) -> Boolean,
    onDeleteUpdate: (ItemUpdate) -> Unit,
    onSaveClosure: (String, String, ClosureReason, String, String) -> Boolean,
    onDeleteClosure: (ItemClosure) -> Unit,
    settingsContent: @Composable (onBack: () -> Unit) -> Unit,
) {
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var showItemForm by remember { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf(HomeSection.INVENTORY) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var statusFilter by rememberSaveable { mutableStateOf(ItemStatusFilter.ACTIVE) }
    val closedItemIds = state.closures.mapTo(mutableSetOf(), ItemClosure::itemId)
    val visibleItems = state.items.filter { item ->
        (selectedCategoryId == null || item.categoryId == selectedCategoryId) &&
            item.name.contains(search, ignoreCase = true) &&
            when (statusFilter) {
                ItemStatusFilter.ACTIVE -> item.id !in closedItemIds
                ItemStatusFilter.FINISHED -> item.id in closedItemIds
                ItemStatusFilter.ALL -> true
            }
    }

    if (showItemForm) {
        ItemFormPage(
            item = editingItem,
            categories = state.categories,
            onBack = { showItemForm = false },
            onSave = { id, name, categoryId, date, price, currency, description ->
                if (onSaveItem(id, name, categoryId, date, price, currency, description)) showItemForm = false
            },
        )
        return
    }
    selectedItemId?.let { itemId ->
        state.items.find { it.id == itemId }?.let { item ->
            ItemDetailPage(
                item = item,
                category = state.categories.find { it.id == item.categoryId },
                updates = state.updates.filter { it.itemId == itemId },
                closure = state.closures.find { it.itemId == itemId },
                onBack = { selectedItemId = null },
                onEditItem = { editingItem = item; showItemForm = true },
                onDeleteItem = {
                    onDeleteItem(itemId)
                    selectedItemId = null
                },
                onSaveUpdate = { update, description, date, cost ->
                    onSaveUpdate(update, itemId, description, date, cost)
                },
                onDeleteUpdate = onDeleteUpdate,
                onSaveClosure = { date, reason, note, recoveredValue ->
                    onSaveClosure(itemId, date, reason, note, recoveredValue)
                },
                onDeleteClosure = onDeleteClosure,
            )
            return
        }
    }
    androidx.activity.compose.BackHandler(enabled = section != HomeSection.INVENTORY) {
        section = HomeSection.INVENTORY
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            IndustrialBottomBar(
                selected = section,
                onInventoryClick = { section = HomeSection.INVENTORY },
                onCategoriesClick = { section = HomeSection.CATEGORIES },
                onMoreClick = { section = HomeSection.MORE },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter,
        ) {
            if (section == HomeSection.CATEGORIES) {
                CategoriesContent(
                    categories = state.categories,
                    usedCategoryIds = state.items.mapTo(mutableSetOf(), Item::categoryId),
                    modifier = Modifier.widthIn(max = 680.dp),
                    onCreate = onCreateCategory,
                    onUpdate = onUpdateCategory,
                    onDelete = onDeleteCategory,
                )
                return@Box
            }
            if (section == HomeSection.MORE) {
                settingsContent { section = HomeSection.INVENTORY }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IndustrialField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Search,
                    placeholder = stringResource(R.string.search_items).uppercase(),
                )
                val filterActive = selectedCategoryId != null
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(if (filterActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .clickable { showFilterSheet = true },
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.filter_categories),
                        tint = if (filterActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatusFilterTag(
                    label = stringResource(R.string.active_items),
                    selected = statusFilter == ItemStatusFilter.ACTIVE,
                    modifier = Modifier.weight(1f),
                ) { statusFilter = ItemStatusFilter.ACTIVE }
                StatusFilterTag(
                    label = stringResource(R.string.finished_items),
                    selected = statusFilter == ItemStatusFilter.FINISHED,
                    modifier = Modifier.weight(1f),
                ) { statusFilter = ItemStatusFilter.FINISHED }
                StatusFilterTag(
                    label = stringResource(R.string.all),
                    selected = statusFilter == ItemStatusFilter.ALL,
                    modifier = Modifier.weight(1f),
                ) { statusFilter = ItemStatusFilter.ALL }
            }
            Spacer(Modifier.height(14.dp))
            if (visibleItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        stringResource(
                            when (statusFilter) {
                                ItemStatusFilter.ACTIVE -> R.string.no_active_items
                                ItemStatusFilter.FINISHED -> R.string.no_finished_items
                                ItemStatusFilter.ALL -> R.string.no_items
                            },
                        ).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(
                            if (statusFilter == ItemStatusFilter.ACTIVE && closedItemIds.isNotEmpty()) {
                                R.string.no_active_items_description
                            } else {
                                R.string.no_items_description
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
                                category = state.categories.find { it.id == item.categoryId },
                                onOpen = { selectedItemId = item.id },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        IndustrialBottomSheet(
            title = stringResource(R.string.filter_categories),
            onDismiss = { showFilterSheet = false },
        ) {
            IndustrialSheetOption(stringResource(R.string.all), selectedCategoryId == null) {
                selectedCategoryId = null
                showFilterSheet = false
            }
            state.categories.forEach { category ->
                IndustrialSheetOption(category.name, selectedCategoryId == category.id) {
                    selectedCategoryId = category.id
                    showFilterSheet = false
                }
            }
        }
    }

}

@Composable
private fun StatusFilterTag(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(background)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp),
            color = content,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ItemCard(item: Item, category: Category?, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
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
                        (category?.name ?: stringResource(R.string.unknown_category)).uppercase(),
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
                    Text(formatCurrency(item.purchasePriceInCents, item.currency))
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun IndustrialBottomBar(
    selected: HomeSection,
    onInventoryClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomDestination(
            Icons.Outlined.Inventory2,
            stringResource(R.string.inventory),
            selected = selected == HomeSection.INVENTORY,
            onClick = onInventoryClick,
        )
        BottomDestination(
            Icons.Outlined.Category,
            stringResource(R.string.categories),
            selected = selected == HomeSection.CATEGORIES,
            onClick = onCategoriesClick,
        )
        BottomDestination(
            Icons.Outlined.MoreHoriz,
            stringResource(R.string.more),
            selected = selected == HomeSection.MORE,
            onClick = onMoreClick,
        )
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
        modifier = Modifier
            .width(96.dp)
            .background(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        val color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp), tint = color)
        Spacer(Modifier.height(4.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

private enum class HomeSection { INVENTORY, CATEGORIES, MORE }
private enum class ItemStatusFilter { ACTIVE, FINISHED, ALL }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ItemFormPage(
    item: Item?,
    categories: List<Category>,
    onBack: () -> Unit,
    onSave: (String?, String, String, String, String, ItemCurrency, String) -> Unit,
) {
    var name by remember { mutableStateOf(item?.name.orEmpty()) }
    var categoryId by remember { mutableStateOf(item?.categoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var date by remember { mutableStateOf(item?.arrivalDate?.toString() ?: LocalDate.now().toString()) }
    var price by remember {
        mutableStateOf(item?.purchasePriceInCents?.toString().orEmpty())
    }
    var showCategorySheet by remember { mutableStateOf(false) }
    var currency by remember { mutableStateOf(item?.currency ?: ItemCurrency.BRL) }
    var showCurrencySheet by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(item?.description.orEmpty()) }
    val selectedCategory = categories.find { it.id == categoryId }
    val valid = name.isNotBlank() && categoryId.isNotBlank() &&
        runCatching { LocalDate.parse(date) }.isSuccess && parseCurrencyDigits(price) != null

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
            onAction = { onSave(item?.id, name, categoryId, date, price, currency, description) },
        )
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            FieldLabel(stringResource(R.string.name))
            IndustrialField(name, { name = it }, isError = name.isNotEmpty() && name.isBlank())
            FieldLabel(stringResource(R.string.item_category))
            IndustrialField(
                value = (selectedCategory?.name ?: stringResource(R.string.select_category)).uppercase(),
                onValueChange = {},
                icon = Icons.Outlined.Sell,
                readOnly = true,
                isError = categories.isEmpty(),
                onClick = { showCategorySheet = true },
            )
            if (categories.isEmpty()) ErrorText(stringResource(R.string.category_required))
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
                { price = sanitizeCurrencyDigits(it) },
                icon = Icons.Outlined.AttachMoney,
                isError = price.isNotEmpty() && parseCurrencyDigits(price) == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CurrencyVisualTransformation,
                placeholder = currencyInputPlaceholder(),
            )
            if (price.isNotEmpty() && parseCurrencyDigits(price) == null) ErrorText(stringResource(R.string.invalid_price))
            FieldLabel(stringResource(R.string.currency))
            IndustrialField(
                value = currency.code,
                onValueChange = {},
                icon = Icons.Outlined.AttachMoney,
                readOnly = true,
                onClick = { showCurrencySheet = true },
            )
            FieldLabel(stringResource(R.string.item_description_optional))
            IndustrialField(
                value = description,
                onValueChange = { description = it },
                singleLine = false,
                placeholder = stringResource(R.string.item_description_hint),
            )
        }
        }
    }
    if (showCategorySheet) {
        IndustrialBottomSheet(
            title = stringResource(R.string.select_category),
            onDismiss = { showCategorySheet = false },
        ) {
            if (categories.isEmpty()) {
                Text(
                    stringResource(R.string.no_categories_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                )
            }
            categories.forEach { category ->
                IndustrialSheetOption(category.name, category.id == categoryId) {
                    categoryId = category.id
                    showCategorySheet = false
                }
            }
        }
    }
    if (showCurrencySheet) {
        IndustrialBottomSheet(
            title = stringResource(R.string.select_currency),
            onDismiss = { showCurrencySheet = false },
        ) {
            ItemCurrency.entries.forEach { option ->
                IndustrialSheetOption(option.code, option == currency) {
                    currency = option
                    showCurrencySheet = false
                }
            }
        }
    }
}

@Composable
private fun CategoriesContent(
    categories: List<Category>,
    usedCategoryIds: Set<String>,
    modifier: Modifier = Modifier,
    onCreate: (String) -> Unit,
    onUpdate: (Category, String) -> Unit,
    onDelete: (Category) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<Category?>(null) }
    Column(
        modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.categories).uppercase(), style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(24.dp))
        FieldLabel(stringResource(R.string.category_name))
        Spacer(Modifier.height(8.dp))
        IndustrialField(name, { name = it })
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = androidx.compose.ui.graphics.RectangleShape,
            onClick = {
                onCreate(name)
                name = ""
            },
        ) {
            Text(
                "+  " + stringResource(R.string.add).uppercase(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.all_categories).uppercase(), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories, key = Category::id) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { editing = category },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(category.name.uppercase(), style = MaterialTheme.typography.titleMedium)
                        Text("›", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
    editing?.let { category ->
        CategoryEditSheet(
            category = category,
            onDismiss = { editing = null },
            onSave = { updatedName ->
                onUpdate(category, updatedName)
                editing = null
            },
            onDelete = {
                editing = null
                deleting = category
            },
        )
    }
    deleting?.let { category ->
        CategoryDeleteSheet(
            category = category,
            inUse = category.id in usedCategoryIds,
            onDismiss = { deleting = null },
            onConfirm = {
                onDelete(category)
                deleting = null
            },
        )
    }
}

@Composable
private fun CategoryEditSheet(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember(category.id) { mutableStateOf(category.name) }
    IndustrialBottomSheet(
        title = stringResource(R.string.edit_category),
        onDismiss = onDismiss,
    ) {
        FieldLabel(stringResource(R.string.category_name))
        Spacer(Modifier.height(8.dp))
        IndustrialField(name, { name = it })
        Spacer(Modifier.height(16.dp))
        IndustrialSheetAction(
            label = stringResource(R.string.save),
            onClick = { onSave(name) },
            primary = true,
            enabled = name.isNotBlank(),
        )
        IndustrialSheetAction(stringResource(R.string.cancel), onDismiss)
        IndustrialSheetAction(
            label = stringResource(R.string.delete_category),
            destructive = true,
            onClick = onDelete,
        )
    }
}

@Composable
private fun CategoryDeleteSheet(
    category: Category,
    inUse: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    IndustrialBottomSheet(
        title = stringResource(R.string.delete_category),
        onDismiss = onDismiss,
    ) {
        Text(
            stringResource(
                if (inUse) R.string.category_in_use_message else R.string.delete_category_confirmation,
                category.name,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(16.dp))
        if (inUse) {
            IndustrialSheetAction(stringResource(R.string.close), onDismiss, primary = true)
        } else {
            IndustrialSheetAction(stringResource(R.string.cancel), onDismiss)
            IndustrialSheetAction(
                label = stringResource(R.string.delete),
                destructive = true,
                onClick = onConfirm,
            )
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

private fun formatCurrency(cents: Long, currency: ItemCurrency): String =
    NumberFormat.getCurrencyInstance().apply { this.currency = Currency.getInstance(currency.code) }.format(cents / 100.0)
