package com.jayden.bluetooth.device.exception

import com.jayden.bluetooth.device.DeviceCompat

class DeviceNotReceivedException(
    override val device: DeviceCompat? = null,
    override val message: String? = null,
    override val cause: Throwable? = null
) : DeviceException() {
}