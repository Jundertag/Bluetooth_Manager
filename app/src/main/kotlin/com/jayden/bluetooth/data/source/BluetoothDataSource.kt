package com.jayden.bluetooth.data.source

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import com.jayden.bluetooth.data.model.BluetoothState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class BluetoothDataSource(
    private val appContext: Context,
    private val manager: BluetoothManager,
) {
    private val adapter: BluetoothAdapter = manager.adapter

    private val _state: MutableStateFlow<BluetoothState> = MutableStateFlow(BluetoothState.STATE_OFF)
    val state = _state.asStateFlow()

    fun start(): Boolean {
        return appContext.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                appContext.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                appContext.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    }
}