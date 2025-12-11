package com.jayden.bluetooth.data.device.exception

import com.jayden.bluetooth.data.device.DeviceCompat

open class DeviceException(
    override val message: String? = null,
    open val device: DeviceCompat? = null
) : Exception() {
    open fun device(): DeviceCompat? = device
}