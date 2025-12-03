package com.jayden.bluetooth.device

data class DeviceCompatUi(
    val name: String,
    val address: String,
    val rssi: String = "",
)