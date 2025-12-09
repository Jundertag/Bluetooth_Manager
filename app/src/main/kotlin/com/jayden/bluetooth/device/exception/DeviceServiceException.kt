package com.jayden.bluetooth.device.exception

class DeviceServiceException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : DeviceException() {
}