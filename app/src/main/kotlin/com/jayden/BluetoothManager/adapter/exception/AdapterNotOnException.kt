package com.jayden.BluetoothManager.adapter.exception

import android.bluetooth.BluetoothAdapter

/**
 * Thrown when a method or property requires a state other than [BluetoothAdapter.STATE_OFF]
 */
class AdapterNotOnException(
    override val message: String? = null,
    override val state: Int? = BluetoothAdapter.STATE_OFF
) : AdapterException() {
}