package com.jayden.bluetooth.adapter.exception

import android.bluetooth.BluetoothAdapter

class AdapterNotBoundException(
    override val message: String? = null,
    override val state: Int? = BluetoothAdapter.STATE_OFF
) : AdapterException() {
}