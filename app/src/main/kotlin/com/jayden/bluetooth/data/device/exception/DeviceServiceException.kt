package com.jayden.bluetooth.data.device.exception

class DeviceServiceException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : DeviceException() {
}