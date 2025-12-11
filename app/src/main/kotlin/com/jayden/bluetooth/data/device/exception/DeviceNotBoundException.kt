package com.jayden.bluetooth.data.device.exception

import com.jayden.bluetooth.data.device.DeviceCompat

class DeviceNotBoundException(
    override val message: String? = null,
    override val device: DeviceCompat?
) : DeviceException() {

}