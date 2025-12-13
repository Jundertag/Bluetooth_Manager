package com.jayden.bluetooth.data.device.exception

import com.jayden.bluetooth.data.device.DeviceCompat

class DeviceAudioStateNotReceivedException(
    override val message: String? = null,
    override val cause: Throwable? = null,
    override val device: DeviceCompat? = null
) : DeviceException() {
}