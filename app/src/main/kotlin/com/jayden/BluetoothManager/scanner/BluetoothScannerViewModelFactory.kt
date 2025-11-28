package com.jayden.BluetoothManager.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jayden.BluetoothManager.ApplicationGraph

class BluetoothScannerViewModelFactory(
    private val appGraph: ApplicationGraph
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BluetoothScannerViewModel(
            appGraph.localAdapter
        ) as T
    }
}