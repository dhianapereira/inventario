package io.github.dhianapereira.inventario.ui.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.dhianapereira.inventario.R
import io.github.dhianapereira.inventario.model.Category
import io.github.dhianapereira.inventario.model.ClosureReason
import io.github.dhianapereira.inventario.model.Item
import io.github.dhianapereira.inventario.model.ItemClosure
import io.github.dhianapereira.inventario.model.ItemUpdate
import io.github.dhianapereira.inventario.model.ItemCurrency
import io.github.dhianapereira.inventario.model.isClosureDateValid
import io.github.dhianapereira.inventario.model.isUpdateDateValid
import io.github.dhianapereira.inventario.model.maintenanceCost
import io.github.dhianapereira.inventario.model.timelineOrdered
import io.github.dhianapereira.inventario.ui.components.IndustrialBottomSheet
import io.github.dhianapereira.inventario.ui.components.IndustrialField
import io.github.dhianapereira.inventario.ui.components.IndustrialSheetOption
import io.github.dhianapereira.inventario.ui.components.IndustrialSheetAction
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Currency

@Composable
internal fun ItemDetailPage(
    item: Item,
    category: Category?,
    updates: List<ItemUpdate>,
    closure: ItemClosure?,
    onBack: () -> Unit,
    onEditItem: () -> Unit,
    onDeleteItem: () -> Unit,
    onSaveUpdate: (ItemUpdate?, String, String, String) -> Boolean,
    onDeleteUpdate: (ItemUpdate) -> Unit,
    onSaveClosure: (String, ClosureReason, String, String) -> Boolean,
    onDeleteClosure: (ItemClosure) -> Unit,
) {
    var editingUpdate by remember { mutableStateOf<ItemUpdate?>(null) }
    var showUpdateForm by remember { mutableStateOf(false) }
    var showClosureForm by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showResumeConfirmation by remember { mutableStateOf(false) }

    when {
        showUpdateForm -> {
            UpdateFormPage(
                item = item,
                update = editingUpdate,
                closure = closure,
                onBack = { showUpdateForm = false },
                onSave = { description, date, cost ->
                    if (onSaveUpdate(editingUpdate, description, date, cost)) showUpdateForm = false
                },
                onDelete = editingUpdate?.let { update ->
                    { onDeleteUpdate(update); showUpdateForm = false }
                },
            )
            return
        }
        showClosureForm -> {
            ClosureFormPage(
                item = item,
                closure = closure,
                updates = updates,
                onBack = { showClosureForm = false },
                onSave = { date, reason, note, recoveredValue ->
                    if (onSaveClosure(date, reason, note, recoveredValue)) showClosureForm = false
                },
            )
            return
        }
    }

    BackHandler(onBack = onBack)
    Box(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(Modifier.fillMaxSize().widthIn(max = 680.dp).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            DetailHeader(onBack, onEditItem) { showActions = true }
            val events = buildList<TimelineEvent> {
                add(TimelineEvent.Arrival(item))
                timelineOrdered(updates).forEach { add(TimelineEvent.Update(it)) }
                closure?.let { add(TimelineEvent.Closure(it)) }
            }.sortedBy { it.date }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "item-details") {
                    Column {
                        Text(item.name.uppercase(), style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            (category?.name ?: stringResource(R.string.unknown_category)).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        item.description?.takeIf { it.isNotBlank() }?.let { description ->
                            Spacer(Modifier.height(12.dp))
                            Text(description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(22.dp))
                        ItemSummary(item, maintenanceCost(updates), closure)
                        Spacer(Modifier.height(22.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                        Spacer(Modifier.height(14.dp))
                        Text(stringResource(R.string.timeline).uppercase(), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(2.dp))
                    }
                }
                items(events, key = TimelineEvent::key) { event ->
                    TimelineCard(event, item.currency) {
                        when (event) {
                            is TimelineEvent.Update -> { editingUpdate = event.value; showUpdateForm = true }
                            is TimelineEvent.Closure -> showClosureForm = true
                            is TimelineEvent.Arrival -> Unit
                        }
                    }
                }
                if (closure == null) {
                    item(key = "add-update") {
                        Button(
                            onClick = { editingUpdate = null; showUpdateForm = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RectangleShape,
                        ) { Text("+  " + stringResource(R.string.add_update).uppercase()) }
                    }
                }
            }
        }
    }
    if (showActions) {
        IndustrialBottomSheet(stringResource(R.string.item_actions), { showActions = false }) {
            IndustrialSheetAction(stringResource(R.string.edit_item), {
                showActions = false
                onEditItem()
            })
            if (closure == null) {
                IndustrialSheetAction(stringResource(R.string.end_use), {
                    showActions = false
                    showClosureForm = true
                })
            } else {
                IndustrialSheetAction(stringResource(R.string.reopen_use), {
                    showActions = false
                    showResumeConfirmation = true
                }, primary = true)
            }
            IndustrialSheetAction(stringResource(R.string.delete_item), {
                showActions = false
                showDeleteConfirmation = true
            }, destructive = true)
        }
    }
    if (showDeleteConfirmation) {
        IndustrialBottomSheet(stringResource(R.string.delete_item), { showDeleteConfirmation = false }) {
            Text(stringResource(R.string.delete_item_confirmation), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            IndustrialSheetAction(stringResource(R.string.cancel), { showDeleteConfirmation = false })
            IndustrialSheetAction(stringResource(R.string.delete), {
                showDeleteConfirmation = false
                onDeleteItem()
            }, destructive = true)
        }
    }
    if (showResumeConfirmation && closure != null) {
        IndustrialBottomSheet(stringResource(R.string.reopen_use), { showResumeConfirmation = false }) {
            Text(stringResource(R.string.resume_cycle_confirmation), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            IndustrialSheetAction(stringResource(R.string.cancel), { showResumeConfirmation = false })
            IndustrialSheetAction(stringResource(R.string.reopen_use), {
                showResumeConfirmation = false
                onDeleteClosure(closure)
            }, primary = true)
        }
    }
}

@Composable
private fun DetailHeader(onBack: () -> Unit, onEdit: () -> Unit, onMore: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = onEdit,
            modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
        ) { Icon(Icons.Outlined.Edit, stringResource(R.string.edit_item)) }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onMore,
            modifier = Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
        ) { Icon(Icons.Outlined.MoreHoriz, stringResource(R.string.more)) }
    }
}

@Composable
private fun ItemSummary(item: Item, maintenanceCost: Long, closure: ItemClosure?) {
    Column(Modifier.fillMaxWidth()) {
        SummaryRow(stringResource(R.string.arrival_date), formatDate(item.arrivalDate))
        SummaryRow(stringResource(R.string.purchase_price), currency(item.purchasePriceInCents, item.currency))
        SummaryRow(
            stringResource(R.string.time_in_use),
            stringResource(
                R.string.days_in_use,
                ChronoUnit.DAYS.between(item.arrivalDate, closure?.date ?: LocalDate.now()),
            ),
        )
        SummaryRow(stringResource(R.string.maintenance_cost), currency(maintenanceCost, item.currency))
        SummaryRow(stringResource(R.string.total_cost), currency(item.purchasePriceInCents + maintenanceCost, item.currency))
        closure?.recoveredValueInCents?.let { recovered ->
            SummaryRow(stringResource(R.string.sale_value), currency(recovered, item.currency))
            SummaryRow(stringResource(R.string.net_cost), currency(item.purchasePriceInCents + maintenanceCost - recovered, item.currency))
        }
        closure?.let {
            SummaryRow(stringResource(R.string.status), stringResource(R.string.use_ended))
            SummaryRow(stringResource(R.string.end_date), formatDate(it.date))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label.uppercase(), style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private sealed interface TimelineEvent {
    val date: LocalDate
    val key: String
    data class Arrival(val value: Item) : TimelineEvent { override val date = value.arrivalDate; override val key = "arrival" }
    data class Update(val value: ItemUpdate) : TimelineEvent { override val date = value.date; override val key = value.id }
    data class Closure(val value: ItemClosure) : TimelineEvent { override val date = value.date; override val key = "closure" }
}

@Composable
private fun TimelineCard(event: TimelineEvent, currency: ItemCurrency, onClick: () -> Unit) {
    val clickable = event !is TimelineEvent.Arrival
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier.width(28.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.weight(1f).width(1.dp).background(MaterialTheme.colorScheme.outline))
            Box(
                Modifier.size(12.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            Box(Modifier.weight(1f).width(1.dp).background(MaterialTheme.colorScheme.outline))
        }
        Card(
            modifier = Modifier.weight(1f).let { if (clickable) it.clickable(onClick = onClick) else it },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(formatDate(event.date), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(5.dp))
            when (event) {
                is TimelineEvent.Arrival -> {
                    Text(stringResource(R.string.item_arrival).uppercase(), style = MaterialTheme.typography.titleMedium)
                    Text(currency(event.value.purchasePriceInCents, currency), style = MaterialTheme.typography.bodyMedium)
                }
                is TimelineEvent.Update -> {
                    Text(event.value.description.uppercase(), style = MaterialTheme.typography.titleMedium)
                    event.value.costInCents?.let { Text(currency(it, currency), style = MaterialTheme.typography.bodyMedium) }
                }
                is TimelineEvent.Closure -> {
                    Text(stringResource(R.string.use_ended).uppercase(), style = MaterialTheme.typography.titleMedium)
                    Text(event.value.reason.label().uppercase(), style = MaterialTheme.typography.bodyMedium)
                    event.value.recoveredValueInCents?.let { Text(currency(it, currency), style = MaterialTheme.typography.bodyMedium) }
                    event.value.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
    }
}

@Composable
private fun UpdateFormPage(
    item: Item,
    update: ItemUpdate?,
    closure: ItemClosure?,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var description by remember { mutableStateOf(update?.description.orEmpty()) }
    var date by remember { mutableStateOf((update?.date ?: LocalDate.now()).toString()) }
    var cost by remember { mutableStateOf(update?.costInCents?.toString().orEmpty()) }
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val validDate = parsedDate != null && isUpdateDateValid(item, closure, parsedDate)
    val valid = description.isNotBlank() && validDate && (cost.isBlank() || parseCurrencyDigits(cost) != null)
    LifecycleFormPage(stringResource(if (update == null) R.string.new_update else R.string.edit_update), onBack, valid, { onSave(description, date, cost) }) {
        FormLabel(stringResource(R.string.update_description))
        IndustrialField(description, { description = it }, singleLine = false)
        FormLabel(stringResource(R.string.date))
        IndustrialField(date, { date = it }, icon = Icons.Outlined.CalendarMonth, isError = !validDate)
        if (!validDate) FormError(stringResource(R.string.invalid_lifecycle_date))
        FormLabel(stringResource(R.string.amount_optional))
        IndustrialField(
            cost,
            { cost = sanitizeCurrencyDigits(it) },
            icon = Icons.Outlined.AttachMoney,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = cost.isNotBlank() && parseCurrencyDigits(cost) == null,
            visualTransformation = CurrencyVisualTransformation,
            placeholder = currencyInputPlaceholder(),
        )
        onDelete?.let { DeleteAction(stringResource(R.string.delete_update), it) }
    }
}

@Composable
private fun ClosureFormPage(
    item: Item,
    closure: ItemClosure?,
    updates: List<ItemUpdate>,
    onBack: () -> Unit,
    onSave: (String, ClosureReason, String, String) -> Unit,
) {
    var date by remember { mutableStateOf((closure?.date ?: LocalDate.now()).toString()) }
    var reason by remember { mutableStateOf(closure?.reason ?: ClosureReason.STOPPED_WORKING) }
    var note by remember { mutableStateOf(closure?.note.orEmpty()) }
    var recoveredValue by remember {
        mutableStateOf(closure?.recoveredValueInCents?.toString().orEmpty())
    }
    var showReasons by remember { mutableStateOf(false) }
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val validDate = parsedDate != null && isClosureDateValid(item, updates, parsedDate)
    val validRecoveredValue = reason != ClosureReason.SOLD || recoveredValue.isBlank() || parseCurrencyDigits(recoveredValue) != null
    LifecycleFormPage(
        stringResource(R.string.end_use),
        onBack,
        validDate && validRecoveredValue,
        { onSave(date, reason, note, recoveredValue) },
    ) {
        FormLabel(stringResource(R.string.date))
        IndustrialField(date, { date = it }, icon = Icons.Outlined.CalendarMonth, isError = !validDate)
        if (!validDate) FormError(stringResource(R.string.invalid_closure_date))
        FormLabel(stringResource(R.string.closure_reason))
        IndustrialField(
            reason.label().uppercase(), {}, icon = Icons.Outlined.EventBusy, readOnly = true,
            onClick = { showReasons = true },
        )
        if (reason == ClosureReason.SOLD) {
            FormLabel(stringResource(R.string.sale_value_optional))
            IndustrialField(
                recoveredValue,
                { recoveredValue = sanitizeCurrencyDigits(it) },
                icon = Icons.Outlined.AttachMoney,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !validRecoveredValue,
                visualTransformation = CurrencyVisualTransformation,
                placeholder = currencyInputPlaceholder(),
            )
        }
        FormLabel(stringResource(R.string.reason_note_optional))
        IndustrialField(note, { note = it }, singleLine = false)
    }
    if (showReasons) {
        IndustrialBottomSheet(stringResource(R.string.select_closure_reason), { showReasons = false }) {
            ClosureReason.entries.forEach { option ->
                IndustrialSheetOption(option.label(), option == reason) { reason = option; showReasons = false }
            }
        }
    }
}

@Composable
private fun LifecycleFormPage(
    title: String,
    onBack: () -> Unit,
    valid: Boolean,
    onSave: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    BackHandler(onBack = onBack)
    Box(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(Modifier.fillMaxSize().widthIn(max = 680.dp).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }
                Text(title.uppercase(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                IconButton(
                    onClick = onSave,
                    enabled = valid,
                    modifier = Modifier.background(if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                ) { Icon(Icons.Outlined.Check, stringResource(R.string.save), tint = if (valid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

@Composable private fun FormLabel(value: String) = Text(value.uppercase(), style = MaterialTheme.typography.labelMedium)
@Composable private fun FormError(value: String) = Text(value, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

@Composable
private fun DeleteAction(label: String, onClick: () -> Unit) {
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RectangleShape) {
        Text(label.uppercase(), color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ClosureReason.label(): String = stringResource(
    when (this) {
        ClosureReason.STOPPED_WORKING -> R.string.reason_stopped_working
        ClosureReason.DONATED -> R.string.reason_donated
        ClosureReason.SOLD -> R.string.reason_sold
        ClosureReason.DISCARDED_OR_RECYCLED -> R.string.reason_discarded
        ClosureReason.LOST -> R.string.reason_lost
        ClosureReason.STOLEN -> R.string.reason_stolen
        ClosureReason.REPLACED -> R.string.reason_replaced
        ClosureReason.OTHER -> R.string.reason_other
    },
)

private fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
private fun currency(cents: Long, currency: ItemCurrency): String =
    NumberFormat.getCurrencyInstance().apply { this.currency = Currency.getInstance(currency.code) }.format(cents / 100.0)
