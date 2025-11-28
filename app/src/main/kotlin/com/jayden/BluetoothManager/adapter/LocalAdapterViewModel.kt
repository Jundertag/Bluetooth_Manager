package com.jayden.BluetoothManager.adapter

import android.util.Log
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

    private val _adapterName = MutableStateFlow("")
    val adapterName = _adapterName.asStateFlow()

    fun start() {
        Log.d(TAG, "start()")
        _boundDevices.value = adapter.pairedDevices.toMutableList()
        _adapterName.update {
            try {
                adapter.name
            } catch (e: SecurityException) {
                "<no-bluetooth-connect-perms>"
            }
        }
    }

    companion object {
        private const val TAG = "LocalAdapterViewModel"
    }
}