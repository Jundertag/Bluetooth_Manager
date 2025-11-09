package com.jayden.bluetooth.data.model

data class BluetoothAdapterState(
    val modemState: BluetoothLocalState = BluetoothLocalState.STATE_OFF,
    val bondState: BluetoothBondState = BluetoothBondState.BOND_NONE,
    val scanMode: BluetoothLocalScanMode = BluetoothLocalScanMode.SCAN_MODE_NONE,
    val name: String = "",
    val discovering: Boolean = false,

)