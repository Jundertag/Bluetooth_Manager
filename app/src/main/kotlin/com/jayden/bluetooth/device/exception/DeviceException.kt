package com.jayden.bluetooth.device.exception

import com.jayden.bluetooth.device.DeviceCompat

open class DeviceException(
    override val message: String? = null,
    open val device: DeviceCompat? = null
) : Exception() {
    open fun device(): DeviceCompat? = device
}