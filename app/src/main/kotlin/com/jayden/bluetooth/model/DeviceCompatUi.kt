package com.jayden.bluetooth.model

data class DeviceCompatUi(
    val alias: String? = null,
    val name: String = "<no-name>",
    val address: String,
    val rssi: String = "",
)