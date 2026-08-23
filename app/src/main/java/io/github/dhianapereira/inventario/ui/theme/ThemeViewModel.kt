package io.github.dhianapereira.inventario.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dhianapereira.inventario.data.preferences.ThemePreferencesRepository
import io.github.dhianapereira.inventario.model.AppTheme
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: ThemePreferencesRepository,
) : ViewModel() {
    val theme = repository.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.SYSTEM)

    fun setTheme(theme: AppTheme) = viewModelScope.launch { repository.setTheme(theme) }
}
