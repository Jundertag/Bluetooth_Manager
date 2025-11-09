package com.jayden.bluetooth.data.model

data class BluetoothDeviceModel(
    val address: String,
    val addressType: BluetoothAddressType,
    val alias: String?,
    val deviceClass: BluetoothDeviceClass,
    val bondState: BluetoothBondState,
    val connectionState: BluetoothDeviceConnectionState,
    val name: String,
    val type: BluetoothDeviceType,
    val uuids: List<String>,
)