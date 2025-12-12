package com.jayden.bluetooth.data.device

sealed class DeviceEvent {
    data class Found(val device: DeviceCompat) : DeviceEvent()
    data class ConnectionState(val state: DeviceCompat.ConnectionState) : DeviceEvent()
    data class Alias(val alias: String?) : DeviceEvent()
    data class Name(val name: String?) : DeviceEvent()
    data class Error(val msg: String? = null, val `throw`: Throwable? = null) : DeviceEvent()

    sealed class A2dpDeviceEvent {
        data class PlayState(val state: A2dpDeviceCompat.PlayState) : A2dpDeviceEvent()
        data class ConnectionState(val state: DeviceCompat.ConnectionState) : A2dpDeviceEvent()
        data class Error(val msg: String? = null, val `throw`: Throwable? = null) : A2dpDeviceEvent()
    }

    sealed class HeadsetDeviceEvent {
        data class Found(val device: DeviceCompat) : HeadsetDeviceEvent()
        data class AudioState(val state: HeadsetDeviceCompat.AudioState) : HeadsetDeviceEvent()
        data class ConnectionState(val state: DeviceCompat.ConnectionState) : HeadsetDeviceEvent()
        data class Error(val msg: String? = null, val `throw`: Throwable? = null) : HeadsetDeviceEvent()
    }
}