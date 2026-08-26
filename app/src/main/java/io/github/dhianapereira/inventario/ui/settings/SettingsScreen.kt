package io.github.dhianapereira.inventario.ui.settings

import androidx.activity.compose.BackHandler
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import io.github.dhianapereira.inventario.R
import io.github.dhianapereira.inventario.ui.components.IndustrialBottomSheet
import io.github.dhianapereira.inventario.ui.components.IndustrialSheetOption
import io.github.dhianapereira.inventario.ui.components.IndustrialSheetAction
import io.github.dhianapereira.inventario.model.AppLanguage
import io.github.dhianapereira.inventario.model.AppTheme

private enum class SettingsPage { MAIN, PREFERENCES, DATA, LEGAL, ABOUT }

@Composable
fun SettingsRoute(
    theme: AppTheme,
    language: AppLanguage,
    onBack: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val backupState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf(SettingsPage.MAIN) }
    var pendingRestoreUri by rememberSaveable { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.toString()?.let(viewModel::export) }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> pendingRestoreUri = uri?.toString() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it.message(context)) }
    }
    BackHandler { if (page == SettingsPage.MAIN) onBack() else page = SettingsPage.MAIN }
    Box {
        when (page) {
            SettingsPage.MAIN -> SettingsPage(stringResource(R.string.settings), null) {
                SettingsRow(Icons.Outlined.Tune, stringResource(R.string.preferences), stringResource(R.string.preferences_description)) { page = SettingsPage.PREFERENCES }
                SettingsRow(Icons.Outlined.Folder, stringResource(R.string.data_and_backup), stringResource(R.string.backup_description)) { page = SettingsPage.DATA }
                SettingsRow(Icons.Outlined.Gavel, stringResource(R.string.legal), stringResource(R.string.legal_description)) { page = SettingsPage.LEGAL }
                SettingsRow(Icons.Outlined.Info, stringResource(R.string.about), stringResource(R.string.about_description)) { page = SettingsPage.ABOUT }
            }
            SettingsPage.PREFERENCES -> PreferencesPage(theme, language, { page = SettingsPage.MAIN }, onThemeSelected, onLanguageSelected)
            SettingsPage.DATA -> SettingsPage(stringResource(R.string.data_and_backup), { page = SettingsPage.MAIN }) {
                SettingsRow(Icons.Outlined.Upload, stringResource(R.string.export_backup), stringResource(R.string.export_backup_description)) {
                    exportLauncher.launch("inventario-backup-${LocalDate.now()}.json")
                }
                SettingsRow(Icons.Outlined.Download, stringResource(R.string.restore_backup), stringResource(R.string.restore_backup_description)) {
                    restoreLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
                }
            }
            SettingsPage.LEGAL -> SettingsPage(stringResource(R.string.legal), { page = SettingsPage.MAIN }) {
                SettingsRow(Icons.Outlined.Description, stringResource(R.string.privacy_policy), stringResource(R.string.coming_soon))
                SettingsRow(Icons.Outlined.Description, stringResource(R.string.terms_of_use), stringResource(R.string.coming_soon))
            }
            SettingsPage.ABOUT -> AboutPage { page = SettingsPage.MAIN }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
    pendingRestoreUri?.let { uri ->
        IndustrialBottomSheet(stringResource(R.string.confirm_restore_title), { pendingRestoreUri = null }) {
            Text(stringResource(R.string.confirm_restore_description), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
            IndustrialSheetAction(stringResource(R.string.restore_backup), {
                pendingRestoreUri = null
                viewModel.restore(uri)
            }, destructive = true)
            IndustrialSheetAction(stringResource(R.string.cancel), { pendingRestoreUri = null })
        }
    }
    backupState.operation?.let { operation ->
        IndustrialBottomSheet(
            stringResource(if (operation == BackupOperation.EXPORT) R.string.exporting_backup else R.string.restoring_backup),
            {},
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Text(stringResource(R.string.keep_app_open), Modifier.padding(start = 16.dp))
            }
            Spacer(Modifier.height(20.dp))
            IndustrialSheetAction(stringResource(R.string.cancel), viewModel::cancel)
        }
    }
}

private fun BackupEvent.message(context: Context): String = when (this) {
    is BackupEvent.Completed -> context.getString(
        if (operation == BackupOperation.EXPORT) R.string.export_success else R.string.restore_success,
        summary.categories, summary.items, summary.updates,
    )
    is BackupEvent.Failed -> context.getString(when (reason) {
        BackupFailureReason.INVALID_FILE -> R.string.invalid_backup
        BackupFailureReason.UNSUPPORTED_VERSION -> R.string.unsupported_backup
        BackupFailureReason.TOO_LARGE -> R.string.backup_too_large
        BackupFailureReason.EMPTY -> R.string.empty_backup
        BackupFailureReason.FILE_ACCESS -> R.string.file_access_error
        BackupFailureReason.UNKNOWN -> R.string.backup_unknown_error
    })
    BackupEvent.Cancelled -> context.getString(R.string.backup_cancelled)
}

@Composable
private fun PreferencesPage(
    theme: AppTheme,
    language: AppLanguage,
    onBack: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    var dialog by remember { mutableStateOf<String?>(null) }
    SettingsPage(stringResource(R.string.preferences), onBack) {
        SectionTitle(stringResource(R.string.appearance))
        SettingsRow(Icons.Outlined.DarkMode, stringResource(R.string.theme), theme.label()) { dialog = "theme" }
        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.language))
        SettingsRow(Icons.Outlined.Language, stringResource(R.string.language), language.label()) { dialog = "language" }
    }
    if (dialog == "theme") SelectionSheet(
        stringResource(R.string.choose_theme),
        AppTheme.entries,
        theme,
        { it.label() },
        { onThemeSelected(it); dialog = null },
        { dialog = null },
    )
    if (dialog == "language") SelectionSheet(
        stringResource(R.string.choose_language),
        AppLanguage.entries,
        language,
        { it.label() },
        { onLanguageSelected(it); dialog = null },
        { dialog = null },
    )
}

@Composable
private fun AboutPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val version = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
    SettingsPage(stringResource(R.string.about), onBack) {
        Text(stringResource(R.string.about_app_text), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        SettingsRow(Icons.Outlined.Person, stringResource(R.string.developed_by), "Dhiana Pereira")
        SettingsRow(Icons.Outlined.Info, stringResource(R.string.version), version)
    }
}

@Composable
private fun SettingsPage(title: String, onBack: (() -> Unit)?, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = androidx.compose.ui.Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                }
                Text(
                    title.uppercase(),
                    style = if (onBack == null) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                )
            }
            Spacer(Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, value: String? = null, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).let { if (onClick != null) it.clickable(onClick = onClick) else it },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(icon, null)
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(title.uppercase(), style = MaterialTheme.typography.titleMedium)
                value?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (onClick != null) Icon(Icons.Outlined.ChevronRight, null)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun <T> SelectionSheet(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    IndustrialBottomSheet(title = title, onDismiss = onDismiss) {
        options.forEach { option ->
            IndustrialSheetOption(label(option), option == selected) { onSelect(option) }
        }
    }
}

@Composable private fun AppTheme.label() = stringResource(when (this) { AppTheme.SYSTEM -> R.string.system_theme; AppTheme.LIGHT -> R.string.light_theme; AppTheme.DARK -> R.string.dark_theme })
@Composable private fun AppLanguage.label() = stringResource(when (this) { AppLanguage.PORTUGUESE -> R.string.portuguese; AppLanguage.ENGLISH -> R.string.english })
