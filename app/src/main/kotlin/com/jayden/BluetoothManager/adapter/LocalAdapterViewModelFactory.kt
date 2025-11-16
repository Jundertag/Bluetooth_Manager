package com.jayden.BluetoothManager.adapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jayden.BluetoothManager.ApplicationGraph

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