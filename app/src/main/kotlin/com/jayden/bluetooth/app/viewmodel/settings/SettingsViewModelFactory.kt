package com.jayden.bluetooth.app.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jayden.bluetooth.ApplicationGraph

class SettingsViewModelFactory(
    private val appGraph: ApplicationGraph
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(appGraph.settingsDataStore) as T
    }
}