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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.Category
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        settingsContent = settingsContent,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onSaveItem: (String?, String, String, String, String) -> Boolean,
    onDeleteItem: (String) -> Unit,
    onCreateCategory: (String) -> Unit,
    onUpdateCategory: (Category, String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    settingsContent: @Composable (onBack: () -> Unit) -> Unit,
) {
    var editingItem by remember { mutableStateOf<Item?>(null) }
    var showItemForm by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(HomeSection.INVENTORY) }
    var search by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    val visibleItems = state.items.filter { item ->
        (selectedCategoryId == null || item.categoryId == selectedCategoryId) &&
            item.name.contains(search, ignoreCase = true)
    }

    if (showItemForm) {
        ItemFormPage(
            item = editingItem,
            categories = state.categories,
            onBack = { showItemForm = false },
            onSave = { id, name, categoryId, date, price ->
                if (onSaveItem(id, name, categoryId, date, price)) showItemForm = false
            },
            onDelete = { id -> onDeleteItem(id); showItemForm = false },
        )
        return
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
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                )
                state.categories.forEach { category ->
                    CategoryFilter(category.name, selectedCategoryId == category.id) { selectedCategoryId = category.id }
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
                                category = state.categories.find { it.id == item.categoryId },
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
private fun ItemCard(item: Item, category: Category?, onEdit: () -> Unit) {
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ItemFormPage(
    item: Item?,
    categories: List<Category>,
    onBack: () -> Unit,
    onSave: (String?, String, String, String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var name by remember { mutableStateOf(item?.name.orEmpty()) }
    var categoryId by remember { mutableStateOf(item?.categoryId ?: categories.firstOrNull()?.id.orEmpty()) }
    var date by remember { mutableStateOf(item?.arrivalDate?.toString() ?: LocalDate.now().toString()) }
    var price by remember {
        mutableStateOf(item?.let { formatPriceInput(it.purchasePriceInCents.toString()) }.orEmpty())
    }
    var showCategorySheet by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == categoryId }
    val valid = name.isNotBlank() && categoryId.isNotBlank() &&
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
            onAction = { onSave(item?.id, name, categoryId, date, price) },
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
                { price = formatPriceInput(it) },
                icon = Icons.Outlined.AttachMoney,
                isError = price.isNotEmpty() && parsePriceInCents(price) == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            if (price.isNotEmpty() && parsePriceInCents(price) == null) ErrorText(stringResource(R.string.invalid_price))
            if (item != null) {
                OutlinedButton(
                    onClick = { onDelete(item.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                ) { Text(stringResource(R.string.delete_item).uppercase()) }
            }
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
}

@Composable
private fun CategoriesContent(
    categories: List<Category>,
    modifier: Modifier = Modifier,
    onCreate: (String) -> Unit,
    onUpdate: (Category, String) -> Unit,
    onDelete: (Category) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Category?>(null) }
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
                onDelete(category)
                editing = null
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
        Button(
            onClick = { onSave(name) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = androidx.compose.ui.graphics.RectangleShape,
        ) { Text(stringResource(R.string.save).uppercase()) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = androidx.compose.ui.graphics.RectangleShape,
        ) { Text(stringResource(R.string.cancel).uppercase()) }
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = androidx.compose.ui.graphics.RectangleShape,
        ) {
            Text(stringResource(R.string.delete).uppercase(), color = MaterialTheme.colorScheme.error)
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
