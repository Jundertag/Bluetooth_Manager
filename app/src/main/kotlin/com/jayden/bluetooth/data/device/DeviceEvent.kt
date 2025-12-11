package com.jayden.bluetooth.data.device

sealed class DeviceEvent {
    data class Found(val device: DeviceCompat) : DeviceEvent()
    data class Error(val msg: String? = null, val `throw`: Throwable? = null) : DeviceEvent()
}