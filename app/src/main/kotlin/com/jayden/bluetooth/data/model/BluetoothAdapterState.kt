package com.jayden.bluetooth.data.model

data class BluetoothAdapterState(
    val state: BluetoothState,
    val discovering: Boolean,
    val name: String,
    val scanMode: BluetoothScanMode,
)