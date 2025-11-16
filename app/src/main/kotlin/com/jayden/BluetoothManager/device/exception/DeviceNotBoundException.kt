package com.jayden.BluetoothManager.device.exception

import com.jayden.BluetoothManager.device.DeviceCompat

class DeviceNotBoundException(
    override val message: String? = null,
    override val device: DeviceCompat?
) : DeviceException() {

}