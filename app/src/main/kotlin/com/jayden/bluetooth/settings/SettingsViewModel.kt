package com.jayden.bluetooth.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jayden.bluetooth.datastore.proto.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    val theme: StateFlow<AppSettings.UI.Theme> = dataStore.themeFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings.UI.Theme.UNSPECIFIED
    )

    fun setTheme(theme: AppSettings.UI.Theme) {
        viewModelScope.launch {
            dataStore.updateTheme(theme)
        }
    }
}