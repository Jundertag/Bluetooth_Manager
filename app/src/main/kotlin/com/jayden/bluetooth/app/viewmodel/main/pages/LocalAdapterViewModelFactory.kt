package com.jayden.bluetooth.app.viewmodel.main.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jayden.bluetooth.ApplicationGraph

class LocalAdapterViewModelFactory(
    private val appGraph: ApplicationGraph
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocalAdapterViewModel(
            appGraph.localAdapter
        ) as T
    }
}