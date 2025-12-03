package com.jayden.bluetooth.device.exception

import com.jayden.bluetooth.device.DeviceCompat

class DeviceNotBoundException(
    override val message: String? = null,
    override val device: DeviceCompat?
) : DeviceException() {

}