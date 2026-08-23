package io.github.dhianapereira.inventario.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.dhianapereira.inventario.model.AppTheme
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.preferencesDataStore by preferencesDataStore("user_preferences")

@Singleton
class ThemePreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val theme = context.preferencesDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { AppTheme.fromStorage(it[ThemeKey]) }

    suspend fun setTheme(theme: AppTheme) {
        context.preferencesDataStore.edit { it[ThemeKey] = theme.name }
    }

    private companion object {
        val ThemeKey = stringPreferencesKey("app_theme")
    }
}
