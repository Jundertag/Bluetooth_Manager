package com.jayden.BluetoothManager.device.exception

import com.jayden.BluetoothManager.device.DeviceCompat

open class DeviceException(
    override val message: String? = null,
    open val device: DeviceCompat? = null
) : Exception() {
    open fun device(): DeviceCompat? = device
}