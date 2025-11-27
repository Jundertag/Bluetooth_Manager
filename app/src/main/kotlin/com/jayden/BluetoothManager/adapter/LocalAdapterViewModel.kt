package com.jayden.BluetoothManager.adapter

import androidx.lifecycle.ViewModel
import com.jayden.BluetoothManager.device.DeviceCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalAdapterViewModel(
    private val adapter: LocalAdapter
) : ViewModel() {

    private val _boundDevices = MutableStateFlow(mutableListOf<DeviceCompat>())
    val boundDevices = _boundDevices.asStateFlow()
    fun start() {

    }


}