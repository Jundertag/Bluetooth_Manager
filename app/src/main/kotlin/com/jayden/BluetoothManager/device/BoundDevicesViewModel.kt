package com.jayden.BluetoothManager.device

import androidx.lifecycle.ViewModel
import com.jayden.BluetoothManager.adapter.LocalAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BoundDevicesViewModel(
    private val adapter: LocalAdapter
) : ViewModel() {
    private val _boundDevices = MutableStateFlow(mutableListOf<DeviceCompat>())
    val boundDevices = _boundDevices.asStateFlow()

    fun start() {
        _boundDevices.update {
            val newList = mutableListOf<DeviceCompat>()
            adapter.pairedDevices.forEach { device ->
                newList.add(device)
            }
            newList
        }
    }
}