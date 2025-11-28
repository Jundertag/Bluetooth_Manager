package com.jayden.BluetoothManager.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jayden.BluetoothManager.ApplicationGraph

class BoundDevicesViewModelFactory(
    private val appGraph: ApplicationGraph
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BoundDevicesViewModel(
            appGraph.localAdapter
        ) as T
    }
}