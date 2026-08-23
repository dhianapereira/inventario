package io.github.dhianapereira.inventario.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.dhianapereira.inventario.R
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
) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.MAIN) }
    BackHandler { if (page == SettingsPage.MAIN) onBack() else page = SettingsPage.MAIN }
    when (page) {
        SettingsPage.MAIN -> SettingsPage(stringResource(R.string.settings), onBack) {
            SettingsRow(Icons.Outlined.Tune, stringResource(R.string.preferences), stringResource(R.string.preferences_description)) { page = SettingsPage.PREFERENCES }
            SettingsRow(Icons.Outlined.Folder, stringResource(R.string.data_and_backup), stringResource(R.string.backup_description)) { page = SettingsPage.DATA }
            SettingsRow(Icons.Outlined.Gavel, stringResource(R.string.legal), stringResource(R.string.legal_description)) { page = SettingsPage.LEGAL }
            SettingsRow(Icons.Outlined.Info, stringResource(R.string.about), stringResource(R.string.about_description)) { page = SettingsPage.ABOUT }
        }
        SettingsPage.PREFERENCES -> PreferencesPage(theme, language, { page = SettingsPage.MAIN }, onThemeSelected, onLanguageSelected)
        SettingsPage.DATA -> SettingsPage(stringResource(R.string.data_and_backup), { page = SettingsPage.MAIN }) {
            SettingsRow(Icons.Outlined.Folder, stringResource(R.string.export_backup), stringResource(R.string.coming_soon))
            SettingsRow(Icons.Outlined.Folder, stringResource(R.string.restore_backup), stringResource(R.string.coming_soon))
        }
        SettingsPage.LEGAL -> SettingsPage(stringResource(R.string.legal), { page = SettingsPage.MAIN }) {
            SettingsRow(Icons.Outlined.Description, stringResource(R.string.privacy_policy), stringResource(R.string.coming_soon))
            SettingsRow(Icons.Outlined.Description, stringResource(R.string.terms_of_use), stringResource(R.string.coming_soon))
        }
        SettingsPage.ABOUT -> AboutPage { page = SettingsPage.MAIN }
    }
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
private fun SettingsPage(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
            }
            Text(title.uppercase(), style = MaterialTheme.typography.headlineLarge)
        }
        Spacer(Modifier.height(24.dp))
        content()
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        options.forEach { option ->
            Row(
                Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                RadioButton(option == selected, { onSelect(option) })
                Text(label(option).uppercase())
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun AppTheme.label() = stringResource(when (this) { AppTheme.SYSTEM -> R.string.system_theme; AppTheme.LIGHT -> R.string.light_theme; AppTheme.DARK -> R.string.dark_theme })
@Composable private fun AppLanguage.label() = stringResource(when (this) { AppLanguage.PORTUGUESE -> R.string.portuguese; AppLanguage.ENGLISH -> R.string.english })
