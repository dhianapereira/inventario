package io.github.dhianapereira.inventario.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import io.github.dhianapereira.inventario.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustrialDateField(
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    isError: Boolean = false,
) {
    var showPicker by remember { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = remember(value, locale) {
        value.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale))
    }

    IndustrialField(
        value = formattedDate,
        onValueChange = {},
        icon = Icons.Outlined.CalendarMonth,
        readOnly = true,
        isError = isError,
        onClick = { showPicker = true },
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = value
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onValueChange(
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate(),
                            )
                        }
                        showPicker = false
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPicker = false },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(
                state = pickerState,
                showModeToggle = false,
            )
        }
    }
}
