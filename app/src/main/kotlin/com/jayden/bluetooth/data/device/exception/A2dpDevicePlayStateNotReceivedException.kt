package com.jayden.bluetooth.data.device.exception

import com.jayden.bluetooth.data.device.DeviceCompat

class A2dpDevicePlayStateNotReceivedException(
    override val message: String? = null,
    override val cause: Throwable? = null,
    override val device: DeviceCompat? = null
) : A2dpDeviceException() {
}