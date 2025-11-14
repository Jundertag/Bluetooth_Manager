package com.jayden.bluetooth.data.model

data class BluetoothAdapterState(
    val state: BluetoothState = BluetoothState.STATE_OFF,
    val discovering: Boolean = false,
    val name: String = "",
    val scanMode: BluetoothScanMode = BluetoothScanMode.SCAN_MODE_NONE,
)