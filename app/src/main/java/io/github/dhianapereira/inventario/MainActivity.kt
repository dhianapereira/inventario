package io.github.dhianapereira.inventario

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.dhianapereira.inventario.model.AppLanguage
import io.github.dhianapereira.inventario.model.AppTheme
import io.github.dhianapereira.inventario.ui.home.HomeRoute
import io.github.dhianapereira.inventario.ui.settings.SettingsRoute
import io.github.dhianapereira.inventario.ui.theme.InventarioTheme
import io.github.dhianapereira.inventario.ui.theme.ThemeViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by themeViewModel.theme.collectAsStateWithLifecycle()
            val darkTheme = when (theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }
            val language = AppLanguage.fromCode(LocalConfiguration.current.locales[0].language)
            var showSettings by rememberSaveable { mutableStateOf(false) }
            InventarioTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (showSettings) {
                        SettingsRoute(
                            theme = theme,
                            language = language,
                            onBack = { showSettings = false },
                            onThemeSelected = themeViewModel::setTheme,
                            onLanguageSelected = { selected ->
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(selected.tag),
                                )
                            },
                        )
                    } else {
                        HomeRoute(onMoreClick = { showSettings = true })
                    }
                }
            }
        }
    }
}
