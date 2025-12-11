package com.jayden.bluetooth.data.device.exception

import com.jayden.bluetooth.data.device.DeviceCompat

class DeviceNotReceivedException(
    override val device: DeviceCompat? = null,
    override val message: String? = null,
    override val cause: Throwable? = null
) : DeviceException() {
}