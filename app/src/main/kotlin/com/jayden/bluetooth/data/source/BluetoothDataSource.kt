package com.jayden.bluetooth.data.source

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import com.jayden.bluetooth.data.model.BluetoothAdapterState
import com.jayden.bluetooth.data.model.BluetoothScanMode
import com.jayden.bluetooth.data.model.BluetoothState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class BluetoothDataSource(
    private val context: Context,
    private val manager: BluetoothManager,
) {
    private val adapter: BluetoothAdapter = manager.adapter

    private val _adapterState = MutableStateFlow(BluetoothAdapterState())
    val adapterState = _adapterState.asStateFlow()


}